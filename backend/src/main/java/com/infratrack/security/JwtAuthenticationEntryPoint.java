package com.infratrack.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Records missing-authentication metrics for protected API endpoints (V2.5-STAB-3).
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final AuthMetricsRecorder authMetricsRecorder;

    public JwtAuthenticationEntryPoint(AuthMetricsRecorder authMetricsRecorder) {
        this.authMetricsRecorder = authMetricsRecorder;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {
        authMetricsRecorder.recordMissingJwt();
        response.sendError(HttpStatus.UNAUTHORIZED.value());
    }
}
