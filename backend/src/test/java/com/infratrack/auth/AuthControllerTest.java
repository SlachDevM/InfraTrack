package com.infratrack.auth;

import com.infratrack.auth.dto.ActivateAccountRequest;
import com.infratrack.auth.dto.LoginRequest;
import com.infratrack.auth.dto.LoginResponse;
import com.infratrack.auth.dto.RegisterRequest;
import com.infratrack.user.UserRole;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private LoginRateLimiter loginRateLimiter;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Test
    void registerEndpointEnabled_inDevelopmentProfile() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("dev");
        assertThat(property.isEnabled()).isTrue();
    }

    @Test
    void registerEndpointEnabled_inDevelopmentProfileExplicit() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("development");
        assertThat(property.isEnabled()).isTrue();
    }

    @Test
    void registerEndpointEnabled_inLocalProfile() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("local");
        assertThat(property.isEnabled()).isTrue();
    }

    @Test
    void registerEndpointDisabled_inProductionProfile() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("prod");
        assertThat(property.isEnabled()).isFalse();
    }

    @Test
    void registerEndpointDisabled_inProductionProfileExplicit() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("production");
        assertThat(property.isEnabled()).isFalse();
    }

    @Test
    void register_throwsForbidden_whenEndpointDisabledInProduction() {
        AuthController.RegisterEndpointProperty disabledProperty = new AuthController.RegisterEndpointProperty("prod");
        AuthController controller = new AuthController(authService, loginRateLimiter, disabledProperty);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");

        assertThatThrownBy(() -> controller.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("403")
                .hasMessageContaining("disabled");

        verify(authService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void register_callsAuthService_whenEndpointEnabledInDevelopment() {
        AuthController.RegisterEndpointProperty enabledProperty = new AuthController.RegisterEndpointProperty("dev");
        AuthController controller = new AuthController(authService, loginRateLimiter, enabledProperty);

        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password");
        request.setName("Test User");

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setToken("jwt-token");
        expectedResponse.setUserId(1L);
        expectedResponse.setEmail("test@example.com");
        expectedResponse.setRole(UserRole.FIELD_EMPLOYEE);

        when(authService.register(request)).thenReturn(expectedResponse);

        var response = controller.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(authService).register(request);
    }

    @Test
    void login_callsAuthServiceAndReturnsResponse() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("dev");
        AuthController controller = new AuthController(authService, loginRateLimiter, property);

        LoginRequest request = new LoginRequest("test@example.com", "password");
        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setToken("jwt-token");
        expectedResponse.setUserId(1L);
        expectedResponse.setEmail("test@example.com");
        expectedResponse.setRole(UserRole.FIELD_EMPLOYEE);

        when(authService.login(request)).thenReturn(expectedResponse);
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");

        var response = controller.login(request, httpServletRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(loginRateLimiter).checkAllowed("127.0.0.1", "test@example.com");
        verify(authService).login(request);
    }

    @Test
    void login_shouldReturn429WhenRateLimitExceeded() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("dev");
        AuthController controller = new AuthController(authService, loginRateLimiter, property);

        LoginRequest request = new LoginRequest("test@example.com", "password");
        when(httpServletRequest.getRemoteAddr()).thenReturn("127.0.0.1");
        doThrow(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, LoginRateLimiter.RATE_LIMIT_MESSAGE))
                .when(loginRateLimiter)
                .checkAllowed("127.0.0.1", "test@example.com");

        assertThatThrownBy(() -> controller.login(request, httpServletRequest))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(exception -> {
                    ResponseStatusException responseStatusException = (ResponseStatusException) exception;
                    assertThat(responseStatusException.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
                    assertThat(responseStatusException.getReason()).isEqualTo(LoginRateLimiter.RATE_LIMIT_MESSAGE);
                });

        verify(authService, never()).login(any(LoginRequest.class));
    }

    @Test
    void resolveClientIp_shouldUseFirstForwardedForAddress() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getHeader("X-Forwarded-For")).thenReturn("203.0.113.10, 192.168.1.1");

        assertThat(AuthController.resolveClientIp(request)).isEqualTo("203.0.113.10");
    }

    @Test
    void activateAccount_callsAuthServiceAndReturnsResponse() {
        AuthController.RegisterEndpointProperty property = new AuthController.RegisterEndpointProperty("dev");
        AuthController controller = new AuthController(authService, loginRateLimiter, property);

        ActivateAccountRequest request = new ActivateAccountRequest();
        request.setToken("activation-token-12345");
        request.setPassword("newpassword");

        LoginResponse expectedResponse = new LoginResponse();
        expectedResponse.setToken("jwt-token");
        expectedResponse.setUserId(1L);
        expectedResponse.setEmail("test@example.com");
        expectedResponse.setRole(UserRole.FIELD_EMPLOYEE);

        when(authService.activateAccount("activation-token-12345", "newpassword")).thenReturn(expectedResponse);

        var response = controller.activateAccount(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResponse);
        verify(authService).activateAccount("activation-token-12345", "newpassword");
    }
}
