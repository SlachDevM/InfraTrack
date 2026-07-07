package com.infratrack.user;

import com.infratrack.auth.ActivationService;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.dto.FcmTokenRequest;
import com.infratrack.user.dto.UserProfileResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.security.JwtAuthenticationToken;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ActivationService activationService;

    @Mock
    private UserManagementService userManagementService;

    @InjectMocks
    private UserController userController;

    @Test
    void getCurrentUser_shouldReturnUserProfileResponse_forAuthenticatedUser() {
        User user = new User("employee@test.com", "password", "John Doe", UserRole.FIELD_EMPLOYEE);
        user.setId(1L);

        JwtAuthenticationToken auth = new JwtAuthenticationToken(1L, "employee@test.com", true);

        when(userService.getById(1L)).thenReturn(user);

        ResponseEntity<UserProfileResponse> response = userController.getCurrentUser(auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isEqualTo(1L);
        assertThat(response.getBody().getName()).isEqualTo("John Doe");
        assertThat(response.getBody().getEmail()).isEqualTo("employee@test.com");
        assertThat(response.getBody().getRole()).isEqualTo(UserRole.FIELD_EMPLOYEE);

        verify(userService).getById(1L);
    }

    @Test
    void getCurrentUser_shouldNotExposeSensitiveData() {
        User user = new User("manager@test.com", "hashed-password", "Manager Name", UserRole.MANAGER);
        user.setId(2L);
        user.setFcmToken("firebase-token-secret");

        JwtAuthenticationToken auth = new JwtAuthenticationToken(2L, "manager@test.com", true);

        when(userService.getById(2L)).thenReturn(user);

        ResponseEntity<UserProfileResponse> response = userController.getCurrentUser(auth);

        UserProfileResponse body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.getId()).isEqualTo(2L);
        assertThat(body.getName()).isEqualTo("Manager Name");
        assertThat(body.getEmail()).isEqualTo("manager@test.com");
        assertThat(body.getRole()).isEqualTo(UserRole.MANAGER);
    }

    @Test
    void getCurrentUser_shouldWorkForAllUserRoles() {
        UserRole[] roles = {
                UserRole.ADMINISTRATOR,
                UserRole.MANAGER,
                UserRole.OPERATIONAL_COORDINATOR,
                UserRole.FIELD_EMPLOYEE,
                UserRole.CONTRACTOR
        };

        for (UserRole role : roles) {
            User user = new User("user@test.com", "password", "Test User", role);
            user.setId(1L);

            JwtAuthenticationToken auth = new JwtAuthenticationToken(1L, "user@test.com", true);

            when(userService.getById(1L)).thenReturn(user);

            ResponseEntity<UserProfileResponse> response = userController.getCurrentUser(auth);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isNotNull();
            assertThat(response.getBody().getRole()).isEqualTo(role);

            verify(userService, atLeastOnce()).getById(1L);
        }
    }

    @Test
    void listUsers_shouldThrowForbiddenOperationException_forNonAdministrator() {
        JwtAuthenticationToken auth = new JwtAuthenticationToken(2L, "manager@test.com", true);
        when(userService.isAdministrator(2L)).thenReturn(false);

        assertThatThrownBy(() -> userController.listUsers(auth))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only administrators can list users.");

        verify(userManagementService, never()).listAllUsers();
    }

    @Test
    void getManagers_shouldThrowForbiddenOperationException_forNonManager() {
        JwtAuthenticationToken auth = new JwtAuthenticationToken(20L, "worker@test.com", true);
        when(userService.isManager(20L)).thenReturn(false);

        assertThatThrownBy(() -> userController.getManagers(auth))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only managers can list managers.");
    }

    @Test
    void updateFcmToken_shouldUpdateTokenAndReturnNoContent() {
        FcmTokenRequest request = new FcmTokenRequest("new-fcm-token-abc123xyz");
        JwtAuthenticationToken auth = new JwtAuthenticationToken(1L, "worker@test.com", true);

        when(userService.updateFcmToken(1L, "new-fcm-token-abc123xyz")).thenReturn(new User());

        ResponseEntity<Void> response = userController.updateFcmToken(request, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(userService).updateFcmToken(1L, "new-fcm-token-abc123xyz");
    }

    @Test
    void updateFcmToken_shouldAcceptValidToken() {
        FcmTokenRequest request = new FcmTokenRequest("fcm-token-from-firebase-12345");
        JwtAuthenticationToken auth = new JwtAuthenticationToken(1L, "worker@test.com", true);

        when(userService.updateFcmToken(1L, "fcm-token-from-firebase-12345")).thenReturn(new User());

        ResponseEntity<Void> response = userController.updateFcmToken(request, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();

        verify(userService).updateFcmToken(1L, "fcm-token-from-firebase-12345");
    }
}
