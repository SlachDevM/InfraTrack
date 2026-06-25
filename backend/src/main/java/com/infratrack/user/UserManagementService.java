package com.infratrack.user;

import com.infratrack.auth.ActivationService;
import com.infratrack.service.EmailService;
import com.infratrack.user.dto.UpdateUserRequest;
import com.infratrack.user.dto.UserManagementResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Slf4j
public class UserManagementService {

    private final UserRepository userRepository;
    private final ActivationService activationService;
    private final EmailService emailService;

    public UserManagementService(
            UserRepository userRepository,
            ActivationService activationService,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.activationService = activationService;
        this.emailService = emailService;
    }

    /**
     * Lists all users for administrator viewing.
     * Authorization must be enforced at the controller level.
     *
     * @return list of all users as UserManagementResponse DTOs
     */
    public List<UserManagementResponse> listAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserManagementResponse)
                .toList();
    }

    /**
     * Gets a single user by ID for detailed management.
     *
     * @param userId the user's ID
     * @return UserManagementResponse DTO
     */
    public UserManagementResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return toUserManagementResponse(user);
    }

    /**
     * Restricts user invitation to administrators.
     * Administrators can invite operational roles but not another administrator.
     *
     * @param adminId the ID of the administrator making the invitation
     * @param targetRole the role to invite
     * @throws ResponseStatusException if caller is not an administrator or tries to invite an administrator
     */
    public void validateAdminInvitationPermission(Long adminId, UserRole targetRole) {
        User admin = userRepository.findById(adminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Administrator not found"));

        if (!admin.getRole().isAdministrator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can invite users");
        }

        if (targetRole == UserRole.ADMINISTRATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot invite administrator users through this endpoint");
        }
    }

    /**
     * Updates a user's name and email. Email changes trigger notifications.
     * Only administrators can update users.
     *
     * @param userId the user to update
     * @param request the update request (name and email)
     * @param requestingAdminId the ID of the admin making the request
     * @return updated user as UserManagementResponse DTO
     */
    @Transactional
    public UserManagementResponse updateUser(Long userId, UpdateUserRequest request, Long requestingAdminId) {
        User admin = userRepository.findById(requestingAdminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not found"));

        if (!admin.getRole().isAdministrator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can update users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Validate input
        if ((request.getName() == null || request.getName().isBlank()) &&
                (request.getEmail() == null || request.getEmail().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one field (name or email) must be provided");
        }

        boolean emailChanged = false;
        String oldEmail = user.getEmail();

        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }

        // Update email if provided and different
        if (request.getEmail() != null && !request.getEmail().isBlank() && !request.getEmail().equals(oldEmail)) {
            // Check for duplicate email
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already in use");
            }

            user.setEmail(request.getEmail());
            emailChanged = true;
        }

        user.setUpdatedAt(System.currentTimeMillis());
        User updatedUser = userRepository.save(user);

        // Handle email change notifications
        if (emailChanged) {
            handleEmailChangeNotifications(updatedUser, oldEmail);
        }

        return toUserManagementResponse(updatedUser);
    }

    /**
     * Handles email change notifications for a user.
     * If user is PENDING_ACTIVATION, generates a new activation link.
     * If user is ACTIVE, sends a notification to both old and new email addresses.
     *
     * @param user the updated user with new email
     * @param oldEmail the previous email address
     */
    private void handleEmailChangeNotifications(User user, String oldEmail) {
        UserStatus status = computeStatus(user);

        if (status == UserStatus.PENDING_ACTIVATION) {
            activationService.resendActivationForUser(user);
            log.info("New activation link sent to user {} at new email address", user.getId());
        } else if (status == UserStatus.ACTIVE) {
            // Send notification to old email (optional but recommended)
            emailService.sendEmailChangeNotification(oldEmail, user.getEmail(), user.getName());
            log.info("Email change notification sent for user {}", user.getId());
        }
    }

    /**
     * Deactivates a user. Prevents the user from logging in.
     * Admins cannot deactivate their own account.
     *
     * For a PENDING_ACTIVATION user: invalidates activation tokens → status becomes DISABLED
     * For an ACTIVE user: sets enabled=false → status becomes DISABLED
     *
     * @param userId the user to deactivate
     * @param requestingAdminId the ID of the admin making the request
     */
    @Transactional
    public void deactivateUser(Long userId, Long requestingAdminId) {
        User admin = userRepository.findById(requestingAdminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not found"));

        if (!admin.getRole().isAdministrator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can deactivate users");
        }

        if (userId.equals(requestingAdminId)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "You cannot deactivate your own account");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        // Check current status to determine if we need to invalidate tokens
        UserStatus currentStatus = computeStatus(user);

        if (currentStatus == UserStatus.PENDING_ACTIVATION) {
            activationService.invalidateUnusedTokensForUser(userId);
            log.info("Invalidated activation tokens for deactivated pending user {}", userId);
        }

        // Set enabled=false to prevent login
        // Status will be computed as:
        // - If PENDING was just deactivated: no valid tokens now → DISABLED
        // - If ACTIVE is deactivated: no tokens → DISABLED
        user.setEnabled(false);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);

        log.info("User {} deactivated by admin {}. New status: {}", userId, requestingAdminId, computeStatus(user));
    }

    /**
     * Reactivates a user.
     *
     * For a DISABLED user who was previously ACTIVE:
     * - Sets enabled=true → status becomes ACTIVE
     *
     * For a DISABLED user who was previously PENDING (deactivated before ever activating):
     * - Do NOT set enabled=true
     * - Instead, reject and suggest using resend-activation
     * - This preserves the pending activation flow
     *
     * For an ACTIVE user:
     * - Throws error (already active)
     *
     * @param userId the user to reactivate
     * @param requestingAdminId the ID of the admin making the request
     */
    @Transactional
    public void reactivateUser(Long userId, Long requestingAdminId) {
        User admin = userRepository.findById(requestingAdminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not found"));

        if (!admin.getRole().isAdministrator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can reactivate users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserStatus currentStatus = computeStatus(user);

        if (currentStatus == UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User is already active");
        }

        if (currentStatus == UserStatus.PENDING_ACTIVATION) {
            // User never activated (still has valid token)
            // Do not silently activate them
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Cannot reactivate a pending user without activation. Use resend-activation instead.");
        }

        // currentStatus == DISABLED (enabled=false and no valid tokens)
        // This could be from:
        // 1. Admin deactivated an active user
        // 2. Admin deactivated a pending user (before reactivating)
        // 
        // For case 1: set enabled=true to restore to ACTIVE
        // For case 2: need to check if user has a password (was ever activated)
        //
        // If user has empty password, they never activated, so we need resend-activation
        // If user has a password, they were previously activated, so we can reactivate them

        if (!user.hasActivatedAccount()) {
            // User never set a password (never completed activation)
            // This is a deactivated-pending user
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "This user never completed activation. Use resend-activation instead.");
        }

        // User has a password, so they were previously active
        // Safe to reactivate
        user.setEnabled(true);
        user.setUpdatedAt(System.currentTimeMillis());
        userRepository.save(user);
        log.info("User {} reactivated by admin {}", userId, requestingAdminId);
    }

    /**
     * Resends activation link to a pending user.
     * Only allowed for users with PENDING_ACTIVATION status.
     *
     * @param userId the user to resend activation link to
     * @param requestingAdminId the ID of the admin making the request
     */
    @Transactional
    public void resendActivationLink(Long userId, Long requestingAdminId) {
        User admin = userRepository.findById(requestingAdminId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Admin not found"));

        if (!admin.getRole().isAdministrator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can resend activation links");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        UserStatus currentStatus = computeStatus(user);
        boolean isNeverActivated = !user.hasActivatedAccount();

        // Allow resend for:
        // 1. PENDING_ACTIVATION users (normal case)
        // 2. DISABLED users who never activated (deactivated-pending users)
        if (currentStatus == UserStatus.PENDING_ACTIVATION) {
            // Normal pending user - resend activation
        } else if (currentStatus == UserStatus.DISABLED && isNeverActivated) {
            // Deactivated-pending user (never set password) - allow resend
        } else {
            // ACTIVE users or DISABLED users who already activated
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Activation link can only be resent for pending or never-activated users");
        }

        activationService.resendActivationForUser(user);

        log.info("Activation link resent for user {} by admin {}", userId, requestingAdminId);
    }

    /**
     * Computes the user's activation status based on enabled flag and activation tokens.
     *
     * @param user the user to compute status for
     * @return UserStatus
     */
    public UserStatus computeStatus(User user) {
        if (user.getEnabled()) {
            return UserStatus.ACTIVE;
        }

        // Check if user has a valid (unused and not expired) activation token
        boolean hasPendingToken = activationService.hasValidActivationToken(user.getId());

        return hasPendingToken ? UserStatus.PENDING_ACTIVATION : UserStatus.DISABLED;
    }

    /**
     * Converts a User entity to a UserManagementResponse DTO.
     *
     * @param user the user to convert
     * @return UserManagementResponse DTO
     */
    private UserManagementResponse toUserManagementResponse(User user) {
        UserStatus status = computeStatus(user);
        return new UserManagementResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                status,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
