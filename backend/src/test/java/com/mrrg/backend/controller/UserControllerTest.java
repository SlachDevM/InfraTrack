package com.mrrg.backend.controller;

import com.mrrg.backend.dto.FcmTokenRequest;
import com.mrrg.backend.model.User;
import com.mrrg.backend.model.UserRole;
import com.mrrg.backend.security.JwtAuthenticationToken;
import com.mrrg.backend.service.UserService;
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

    @InjectMocks
    private UserController userController;

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
    void updateFcmToken_shouldReturnBadRequest_whenTokenIsNull() {
        FcmTokenRequest request = new FcmTokenRequest();
        request.setToken(null);

        JwtAuthenticationToken auth = new JwtAuthenticationToken(1L, "worker@test.com", true);

        ResponseEntity<Void> response = userController.updateFcmToken(request, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).updateFcmToken(anyLong(), anyString());
    }

    @Test
    void updateFcmToken_shouldReturnBadRequest_whenTokenIsBlank() {
        FcmTokenRequest request = new FcmTokenRequest("   ");

        JwtAuthenticationToken auth = new JwtAuthenticationToken(1L, "worker@test.com", true);

        ResponseEntity<Void> response = userController.updateFcmToken(request, auth);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        verify(userService, never()).updateFcmToken(anyLong(), anyString());
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
