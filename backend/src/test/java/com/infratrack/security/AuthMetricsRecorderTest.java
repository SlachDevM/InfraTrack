package com.infratrack.security;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthMetricsRecorderTest {

    private SimpleMeterRegistry meterRegistry;
    private AuthMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        recorder = new AuthMetricsRecorder(meterRegistry);
    }

    @Test
    void recordInvalidJwt_incrementsCounter() {
        recorder.recordInvalidJwt();
        recorder.recordInvalidJwt();

        assertThat(meterRegistry.get("mobile.auth.jwt.invalid").counter().count()).isEqualTo(2.0);
    }

    @Test
    void recordDisabledUserJwt_incrementsCounter() {
        recorder.recordDisabledUserJwt();

        assertThat(meterRegistry.get("mobile.auth.jwt.disabled_user").counter().count()).isEqualTo(1.0);
    }

    @Test
    void recordMissingJwt_incrementsCounter() {
        recorder.recordMissingJwt();

        assertThat(meterRegistry.get("mobile.auth.jwt.missing").counter().count()).isEqualTo(1.0);
    }
}
