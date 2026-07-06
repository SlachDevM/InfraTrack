package com.infratrack.security;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationEntryPointTest {

    private SimpleMeterRegistry meterRegistry;
    private JwtAuthenticationEntryPoint entryPoint;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        entryPoint = new JwtAuthenticationEntryPoint(new AuthMetricsRecorder(meterRegistry));
    }

    @Test
    void commence_recordsMissingJwtMetricAndReturnsUnauthorized() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/mobile/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("missing"));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(meterRegistry.get("mobile.auth.jwt.missing").counter().count()).isEqualTo(1.0);
    }
}
