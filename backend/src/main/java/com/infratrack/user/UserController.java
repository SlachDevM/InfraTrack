package com.infratrack.user;

import com.infratrack.auth.ActivationService;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.user.dto.CreateEmployeeRequest;
import com.infratrack.user.dto.FcmTokenRequest;
import com.infratrack.user.dto.UpdateUserRequest;
import com.infratrack.user.dto.UserManagementResponse;
import com.infratrack.user.dto.UserProfileResponse;
import com.infratrack.user.dto.UserSummary;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management, profile and role-based directory endpoints")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
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

    @GetMapping
    @Operation(summary = "List users", description = "Returns all users. Administrator only.")
    @ApiResponse(responseCode = "200", description = "User list")
    @ApiResponse(responseCode = "403", description = "Not an administrator")
    public ResponseEntity<List<UserManagementResponse>> listUsers(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isAdministrator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userManagementService.listAllUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Administrator only.")
    @ApiResponse(responseCode = "200", description = "User details")
    @ApiResponse(responseCode = "403", description = "Not an administrator")
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
    @Operation(
            summary = "Invite employee",
            description = "Creates a pending user and sends an activation link. Administrator only.")
    @ApiResponse(responseCode = "201", description = "Invitation created")
    public ResponseEntity<UserProfileResponse> createEmployeeInvitation(
            @Valid @RequestBody CreateEmployeeRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();

        userManagementService.validateAdminInvitationPermission(userId, request.getRole());

        User invitedUser = activationService.createEmployeeInvitation(
                userId,
                request.getName(),
                request.getEmail(),
                request.getRole(),
                request.getDepartmentId());

        return ResponseEntity.status(HttpStatus.CREATED).body(UserProfileResponse.from(invitedUser));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates name and/or email. Administrator only.")
    @ApiResponse(responseCode = "200", description = "Updated user")
    public ResponseEntity<UserManagementResponse> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        UserManagementResponse updatedUser = userManagementService.updateUser(id, request, adminId);
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Administrator only.")
    @ApiResponse(responseCode = "204", description = "User deactivated")
    public ResponseEntity<Void> deactivateUser(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        userManagementService.deactivateUser(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/reactivate")
    @Operation(summary = "Reactivate user", description = "Administrator only.")
    @ApiResponse(responseCode = "204", description = "User reactivated")
    public ResponseEntity<Void> reactivateUser(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        userManagementService.reactivateUser(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/resend-activation")
    @Operation(summary = "Resend activation link", description = "Administrator only; pending users only.")
    @ApiResponse(responseCode = "204", description = "Activation link resent")
    public ResponseEntity<Void> resendActivationLink(
            @PathVariable Long id,
            Authentication authentication) {
        Long adminId = ((JwtAuthenticationToken) authentication).getUserId();
        userManagementService.resendActivationLink(id, adminId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponse(responseCode = "200", description = "Authenticated user profile")
    public ResponseEntity<UserProfileResponse> getCurrentUser(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        User user = userService.getById(userId);
        return ResponseEntity.ok(UserProfileResponse.from(user));
    }

    @GetMapping("/workers")
    @Operation(
            summary = "List workers",
            description = "Returns field employees and contractors for assignment. "
                    + "When departmentId and role are provided, returns only active workers "
                    + "in that department with the specified role. "
                    + "Administrator, Manager or Operational Coordinator only.")
    @ApiResponse(responseCode = "200", description = "Worker summaries")
    @ApiResponse(responseCode = "403", description = "Insufficient role")
    public ResponseEntity<List<UserSummary>> getWorkers(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) UserRole role,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isAdministrator(userId)
                && !userService.isManager(userId)
                && !userService.isOperationalCoordinator(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (departmentId != null || role != null) {
            return ResponseEntity.ok(userService.getEligibleWorkersForAssignment(userId, departmentId, role));
        }
        return ResponseEntity.ok(userService.getWorkers());
    }

    @GetMapping("/managers")
    @Operation(summary = "List managers", description = "Manager role only.")
    @ApiResponse(responseCode = "200", description = "Manager summaries")
    @ApiResponse(responseCode = "403", description = "Not a manager")
    public ResponseEntity<List<UserSummary>> getManagers(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        if (!userService.isManager(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(userService.getManagers());
    }

    @PutMapping("/me/fcm-token")
    @Operation(summary = "Update FCM token", description = "Stores the device token for push notifications.")
    @ApiResponse(responseCode = "204", description = "Token updated")
    @ApiResponse(responseCode = "400", description = "Token missing or blank")
    public ResponseEntity<Void> updateFcmToken(
            @Valid @RequestBody FcmTokenRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        userService.updateFcmToken(userId, request.getToken());
        return ResponseEntity.noContent().build();
    }
}
