package com.infratrack.security;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Micrometer counters for JWT authentication outcomes (V2.5-STAB-3).
 */
@Component
public class AuthMetricsRecorder {

    private final Counter invalidJwt;
    private final Counter disabledUserJwt;
    private final Counter missingJwt;

    public AuthMetricsRecorder(MeterRegistry meterRegistry) {
        this.invalidJwt = Counter.builder("mobile.auth.jwt.invalid")
                .description("JWT rejected due to invalid signature, malformed token, or expiry")
                .register(meterRegistry);
        this.disabledUserJwt = Counter.builder("mobile.auth.jwt.disabled_user")
                .description("Valid JWT rejected because the account is disabled")
                .register(meterRegistry);
        this.missingJwt = Counter.builder("mobile.auth.jwt.missing")
                .description("Protected endpoint accessed without authentication")
                .register(meterRegistry);
    }

    public void recordInvalidJwt() {
        invalidJwt.increment();
    }

    public void recordDisabledUserJwt() {
        disabledUserJwt.increment();
    }

    public void recordMissingJwt() {
        missingJwt.increment();
    }
}
