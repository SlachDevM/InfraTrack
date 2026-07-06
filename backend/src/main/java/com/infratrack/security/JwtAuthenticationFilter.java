package com.infratrack.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final UserAccountStatusService userAccountStatusService;
    private final AuthMetricsRecorder authMetricsRecorder;

    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            UserAccountStatusService userAccountStatusService,
            AuthMetricsRecorder authMetricsRecorder) {
        this.tokenProvider = tokenProvider;
        this.userAccountStatusService = userAccountStatusService;
        this.authMetricsRecorder = authMetricsRecorder;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String jwt = getJwtFromRequest(request);

            if (jwt != null) {
                if (!tokenProvider.validateToken(jwt)) {
                    authMetricsRecorder.recordInvalidJwt();
                } else {
                    String email = tokenProvider.getEmailFromToken(jwt);
                    Long userId = tokenProvider.getUserIdFromToken(jwt);

                    if (!userAccountStatusService.isEnabled(userId)) {
                        authMetricsRecorder.recordDisabledUserJwt();
                        SecurityContextHolder.clearContext();
                        response.sendError(HttpStatus.UNAUTHORIZED.value());
                        return;
                    }

                    JwtAuthenticationToken authentication = new JwtAuthenticationToken(userId, email, true);
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (Exception ex) {
            authMetricsRecorder.recordInvalidJwt();
            logger.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
