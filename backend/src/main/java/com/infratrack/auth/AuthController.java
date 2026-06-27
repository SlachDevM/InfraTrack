package com.infratrack.auth;

import com.infratrack.auth.dto.ActivateAccountRequest;
import com.infratrack.auth.dto.LoginRequest;
import com.infratrack.auth.dto.LoginResponse;
import com.infratrack.auth.dto.RegisterRequest;
import com.infratrack.config.openapi.StandardApiResponses;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login, registration (dev only), and account activation")
public class AuthController {

    private final AuthService authService;
    private final boolean registerEndpointEnabled;

    public AuthController(AuthService authService, RegisterEndpointProperty registerEndpointProperty) {
        this.authService = authService;
        this.registerEndpointEnabled = registerEndpointProperty.isEnabled();
    }

    @PostMapping("/register")
    @Operation(
            summary = "Register user (development only)",
            description = "Public self-registration. Disabled in production; use administrator invitation instead.")
    @ApiResponse(responseCode = "200", description = "Registration successful; returns JWT")
    @ApiResponse(responseCode = "403", description = "Public registration disabled")
    @StandardApiResponses
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        if (!registerEndpointEnabled) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.FORBIDDEN,
                    "Public registration is disabled. Contact an administrator to request account activation."
            );
        }
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates with email and password and returns a JWT bearer token.")
    @ApiResponse(responseCode = "200", description = "Login successful")
    @ApiResponse(responseCode = "401", description = "Invalid credentials")
    @StandardApiResponses
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/activate-account")
    @Operation(
            summary = "Activate account",
            description = "Activates a pending user account with the invitation token and chosen password.")
    @ApiResponse(responseCode = "200", description = "Account activated; returns JWT")
    @ApiResponse(responseCode = "404", description = "Invalid activation token")
    @ApiResponse(responseCode = "410", description = "Token expired or already used")
    @StandardApiResponses
    public ResponseEntity<LoginResponse> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        return ResponseEntity.ok(authService.activateAccount(request.getToken(), request.getPassword()));
    }

    /**
     * Determines if public registration endpoint is enabled based on active profile.
     * Registration is only enabled in development profiles (dev, development, local).
     */
    @org.springframework.stereotype.Component
    public static class RegisterEndpointProperty {
        private final String activeProfile;

        public RegisterEndpointProperty(@org.springframework.beans.factory.annotation.Value("${spring.profiles.active:dev}") String activeProfile) {
            this.activeProfile = activeProfile;
        }

        public boolean isEnabled() {
            return "dev".equalsIgnoreCase(activeProfile)
                    || "development".equalsIgnoreCase(activeProfile)
                    || "local".equalsIgnoreCase(activeProfile);
        }
    }
}
