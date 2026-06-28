package com.infratrack.config;

import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.File;

@Component
@Slf4j
public class FirebaseStartupDiagnostics {

    private final ObjectProvider<FirebaseMessaging> firebaseMessagingProvider;

    @Value("${firebase.service-account-path:}")
    private String serviceAccountPath;

    public FirebaseStartupDiagnostics(ObjectProvider<FirebaseMessaging> firebaseMessagingProvider) {
        this.firebaseMessagingProvider = firebaseMessagingProvider;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void logFirebaseStatus() {
        if (serviceAccountPath == null || serviceAccountPath.isBlank()) {
            log.info("Firebase credentials not configured. FCM push notifications are disabled.");
            return;
        }

        File credentialsFile = new File(serviceAccountPath);
        if (!credentialsFile.isFile()) {
            log.warn(
                    "Firebase credentials file not found at {}. FCM push notifications are disabled.",
                    serviceAccountPath
            );
            return;
        }

        FirebaseMessaging firebaseMessaging = firebaseMessagingProvider.getIfAvailable();
        if (firebaseMessaging != null) {
            log.info("Firebase messaging enabled.");
        } else {
            log.warn(
                    "Firebase credentials file exists at {} but messaging is disabled. Check startup logs for details.",
                    serviceAccountPath
            );
        }
    }
}
