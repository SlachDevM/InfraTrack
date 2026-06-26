package com.infratrack.user;

import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.auth.ActivationService;
import com.infratrack.user.dto.CreateEmployeeRequest;
import com.infratrack.user.dto.FcmTokenRequest;
import com.infratrack.user.dto.UpdateUserRequest;
import com.infratrack.user.dto.UserManagementResponse;
import com.infratrack.user.dto.UserProfileResponse;
import com.infratrack.user.dto.UserSummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {

    private final UserService userService;
    private final ActivationService activationService;
    private final UserManagementService userManagementService;

    public UserController(
            UserService userService,
            ActivationService activationService,
            UserManagementService userManagementService) {
        this.userService = userService;
        this.activationService = activationService;
        this.userManagementService = userManagementService;
    }

    /**
     * Lists all users. Only administrators can access.
     */
    @GetMapping
    public ResponseEntity<List<UserManagementResponse>> listUsers(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isAdministrator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userManagementService.listAllUsers());
    }

    /**
     * Gets a single user by ID. Only administrators can access.
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserManagementResponse> getUser(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isAdministrator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userManagementService.getUserById(id));
    }

    @PostMapping("/invitations")
    public ResponseEntity<UserProfileResponse> createEmployeeInvitation(
            @RequestBody CreateEmployeeRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();

        userManagementService.validateAdminInvitationPermission(userId, request.getRole());

        User invitedUser = activationService.createEmployeeInvitation(
                userId,
                request.getName(),
                request.getEmail(),
                request.getRole(),
                request.getDepartmentId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(UserProfileResponse.from(invitedUser));
    }

    /**
     * Updates a user's name and/or email. Only administrators can update users.
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserManagementResponse> updateUser(
            @PathVariable Long id,
            @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        UserManagementResponse updatedUser = userManagementService.updateUser(id, request, adminId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Deactivates a user. Only administrators can deactivate users.
     */
    @PostMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        userManagementService.deactivateUser(id, adminId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reactivates a user. Only administrators can reactivate users.
     */
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivateUser(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        userManagementService.reactivateUser(id, adminId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Resends activation link to a pending user. Only administrators can resend activation links.
     */
    @PostMapping("/{id}/resend-activation")
    public ResponseEntity<Void> resendActivationLink(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        userManagementService.resendActivationLink(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        User user = userService.getById(userId);
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @GetMapping("/workers")
    public ResponseEntity<List<UserSummary>> getWorkers(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isAdministrator(userId)
                && !userService.isManager(userId)
                && !userService.isOperationalCoordinator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getWorkers());
    }

    @PutMapping("/me/fcm-token")
    public ResponseEntity<Void> updateFcmToken(
            @RequestBody FcmTokenRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();

        if (request.getToken() == null || request.getToken().isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        userService.updateFcmToken(userId, request.getToken());
        return ResponseEntity.noContent().build();
    }
}
