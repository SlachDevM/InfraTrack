package com.infratrack.config;

import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebaseStartupDiagnosticsTest {

    @Mock
    private ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Test
    void logFirebaseStatus_shouldNotThrowWhenCredentialsPathIsBlank() {
        FirebaseStartupDiagnostics diagnostics = new FirebaseStartupDiagnostics(firebaseMessagingProvider);
        ReflectionTestUtils.setField(diagnostics, "serviceAccountPath", "   ");

        assertThatCode(diagnostics::logFirebaseStatus).doesNotThrowAnyException();
    }

    @Test
    void logFirebaseStatus_shouldNotThrowWhenCredentialsFileIsMissing() {
        FirebaseStartupDiagnostics diagnostics = new FirebaseStartupDiagnostics(firebaseMessagingProvider);
        ReflectionTestUtils.setField(
                diagnostics,
                "serviceAccountPath",
                "/tmp/infratrack-missing-firebase-credentials.json"
        );

        assertThatCode(diagnostics::logFirebaseStatus).doesNotThrowAnyException();
    }

    @Test
    void logFirebaseStatus_shouldNotThrowWhenMessagingBeanIsPresent() throws IOException {
        when(firebaseMessagingProvider.getIfAvailable()).thenReturn(mock(FirebaseMessaging.class));

        File credentialsFile = File.createTempFile("firebase-credentials", ".json");
        credentialsFile.deleteOnExit();

        FirebaseStartupDiagnostics diagnostics = new FirebaseStartupDiagnostics(firebaseMessagingProvider);
        ReflectionTestUtils.setField(diagnostics, "serviceAccountPath", credentialsFile.getAbsolutePath());

        assertThatCode(diagnostics::logFirebaseStatus).doesNotThrowAnyException();
    }
}
