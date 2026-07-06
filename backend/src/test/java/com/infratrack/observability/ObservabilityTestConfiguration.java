package com.infratrack.observability;

import com.infratrack.security.AuthMetricsRecorder;
import com.infratrack.security.JwtAuthenticationEntryPoint;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class ObservabilityTestConfiguration {

    @Bean
    MeterRegistry meterRegistry() {
        return new SimpleMeterRegistry();
    }

    @Bean
    AuthMetricsRecorder authMetricsRecorder(MeterRegistry meterRegistry) {
        return new AuthMetricsRecorder(meterRegistry);
    }

    @Bean
    JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint(AuthMetricsRecorder authMetricsRecorder) {
        return new JwtAuthenticationEntryPoint(authMetricsRecorder);
    }

    @Bean
    MobileEndpointMetricsRecorder mobileEndpointMetricsRecorder(MeterRegistry meterRegistry) {
        return new MobileEndpointMetricsRecorder(meterRegistry);
    }
}
