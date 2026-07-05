package com.infratrack.auth;

import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
import com.infratrack.mail.EmailService;
import com.infratrack.security.UserAccountStatusService;
import com.infratrack.user.EmailNormalizer;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Creates pending user accounts and processes account activation.
 */
@Service
@Slf4j
public class ActivationService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AccountActivationTokenRepository tokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final UserAccountStatusService userAccountStatusService;

    @Value("${app.activation-token-expiration-hours:24}")
    private long tokenExpirationHours;

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32;

    public ActivationService(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            AccountActivationTokenRepository tokenRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder,
            UserAccountStatusService userAccountStatusService) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.tokenRepository = tokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.userAccountStatusService = userAccountStatusService;
    }

    /**
     * Creates a user invitation and sends an activation email.
     * Only administrators can invite users.
     *
     * @param creatorId the ID of the manager/admin creating the invitation
     * @param name the employee's name
     * @param email the employee's email
     * @param requestedRole the requested role (validated based on creator permissions)
     * @return the created (but inactive) user
     */
    @Transactional
    public User createEmployeeInvitation(
            Long creatorId,
            String name,
            String email,
            UserRole requestedRole,
            Long departmentId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));

        // Validate creator permissions
        validateCreatorPermissions(creator, requestedRole);

        // Check for duplicate email
        String normalizedEmail = EmailNormalizer.normalize(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        // Validate inputs
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Name cannot be blank");
        }
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email cannot be blank");
        }

        // Create inactive user
        User newUser = new User(normalizedEmail, "", name, requestedRole);
        newUser.setEnabled(false);
        if (departmentId != null) {
            Department department = departmentRepository.findById(departmentId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Department not found"));
            newUser.setDepartment(department);
        }
        User savedUser = userRepository.save(newUser);

        createAndSendActivationToken(savedUser);

        log.info("Employee invitation created for email: {} by user: {}", email, creator.getId());
        return savedUser;
    }

    /**
     * Returns whether the user has a valid (unused and not expired) activation token.
     */
    public boolean hasValidActivationToken(Long userId) {
        return tokenRepository.hasValidTokenByUserId(userId, System.currentTimeMillis());
    }

    /**
     * Marks all unused activation tokens for the user as used.
     */
    @Transactional
    public void invalidateUnusedTokensForUser(Long userId) {
        var unusedTokens = tokenRepository.findUnusedByUserId(userId);
        unusedTokens.forEach(token -> token.setUsedAt(System.currentTimeMillis()));
        if (!unusedTokens.isEmpty()) {
            tokenRepository.saveAll(unusedTokens);
        }
    }

    /**
     * Creates a new activation token for the user and sends the activation email.
     */
    @Transactional
    public void createAndSendActivationToken(User user) {
        String token = generateSecureToken();
        long expirationTime = System.currentTimeMillis() + (tokenExpirationHours * 60 * 60 * 1000);
        AccountActivationToken activationToken = new AccountActivationToken(token, user, expirationTime);
        tokenRepository.save(activationToken);
        emailService.sendActivationEmail(user.getEmail(), token, user.getName());
    }

    /**
     * Invalidates existing unused tokens and sends a new activation link to the user.
     */
    @Transactional
    public void resendActivationForUser(User user) {
        invalidateUnusedTokensForUser(user.getId());
        createAndSendActivationToken(user);
    }

    /**
     * Activates a user account with a valid token and password.
     *
     * @param token the activation token
     * @param password the user's chosen password
     * @return the activated user
     */
    @Transactional
    public User activateAccount(String token, String password) {
        // Validate inputs
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token is required");
        }
        if (password == null || password.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password is required");
        }

        // Find token
        AccountActivationToken activationToken = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token"));

        // Validate token
        if (activationToken.isExpired()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Activation token has expired");
        }
        if (activationToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.GONE, "Activation token has already been used");
        }

        // Activate user
        User user = activationToken.getUser();
        user.setPassword(passwordEncoder.encode(password));
        user.setEnabled(true);
        user.setUpdatedAt(System.currentTimeMillis());
        User activatedUser = userRepository.save(user);
        userAccountStatusService.evict(user.getId());

        // Mark token as used
        activationToken.setUsedAt(System.currentTimeMillis());
        tokenRepository.save(activationToken);

        log.info("Account activated for user: {}", user.getId());
        return activatedUser;
    }

    private void validateCreatorPermissions(User creator, UserRole requestedRole) {
        if (!creator.getRole().isAdministrator()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only administrators can create user invitations");
        }

        if (requestedRole == UserRole.ADMINISTRATOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot create administrator accounts via invitation");
        }
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(randomBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
