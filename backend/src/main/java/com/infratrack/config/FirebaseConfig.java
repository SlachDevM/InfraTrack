package com.infratrack.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Bean
    @Conditional(FirebaseCredentialsAvailableCondition.class)
    public FirebaseMessaging firebaseMessaging(
            @Value("${firebase.service-account-path}") String serviceAccountPath
    ) {
        try (FileInputStream serviceAccountStream = new FileInputStream(serviceAccountPath)) {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(serviceAccountStream)
                    .createScoped("https://www.googleapis.com/auth/cloud-platform");

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(credentials)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
            }

            return FirebaseMessaging.getInstance();
        } catch (IOException exception) {
            log.error(
                    "Failed to load Firebase credentials from {}: {}. FCM push notifications are disabled.",
                    serviceAccountPath,
                    exception.getMessage()
            );
            return null;
        }
    }
}
