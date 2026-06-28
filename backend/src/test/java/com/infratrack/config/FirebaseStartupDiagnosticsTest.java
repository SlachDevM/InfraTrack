package com.infratrack.config;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class FirebaseStartupDiagnosticsTest {

    @Test
    void logFirebaseStatus_shouldNotThrowWhenCredentialsPathIsBlank() {
        FirebaseStartupDiagnostics diagnostics = new FirebaseStartupDiagnostics();
        ReflectionTestUtils.setField(diagnostics, "serviceAccountPath", "   ");

        assertThatCode(diagnostics::logFirebaseStatus).doesNotThrowAnyException();
    }

    @Test
    void logFirebaseStatus_shouldNotThrowWhenCredentialsFileIsMissing() {
        FirebaseStartupDiagnostics diagnostics = new FirebaseStartupDiagnostics();
        ReflectionTestUtils.setField(
                diagnostics,
                "serviceAccountPath",
                "/tmp/infratrack-missing-firebase-credentials.json"
        );

        assertThatCode(diagnostics::logFirebaseStatus).doesNotThrowAnyException();
    }

    @Test
    void logFirebaseStatus_shouldNotThrowWhenMessagingBeanIsPresent() {
        FirebaseStartupDiagnostics diagnostics = new FirebaseStartupDiagnostics();
        ReflectionTestUtils.setField(diagnostics, "serviceAccountPath", "");
        ReflectionTestUtils.setField(diagnostics, "firebaseMessaging", mock(FirebaseMessaging.class));

        assertThatCode(diagnostics::logFirebaseStatus).doesNotThrowAnyException();
    }
}
