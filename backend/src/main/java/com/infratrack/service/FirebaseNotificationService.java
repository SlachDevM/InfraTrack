package com.infratrack.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import com.infratrack.user.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Slf4j
public class FirebaseNotificationService {

    private final Optional<FirebaseMessaging> firebaseMessaging;

    public FirebaseNotificationService(
            @Autowired(required = false) FirebaseMessaging firebaseMessaging
    ) {
        this.firebaseMessaging = Optional.ofNullable(firebaseMessaging);
    }

    /**
     * Sends a push notification to a user via Firebase Cloud Messaging.
     * If the user has no FCM token, the notification is silently skipped.
     * If Firebase fails, the error is logged but does not throw an exception,
     * allowing business operations to continue.
     *
     * @param user the user to send the notification to
     * @param title the notification title
     * @param body the notification body
     * @param data optional data payload (notificationId, etc.)
     */
    public void sendToUser(User user, String title, String body, Map<String, String> data) {
        if (!firebaseMessaging.isPresent()) {
            log.debug("Firebase not configured. Skipping push notification.");
            return;
        }

        if (user == null || user.getFcmToken() == null || user.getFcmToken().isBlank()) {
            log.debug("User {} has no FCM token. Skipping push notification.", user != null ? user.getId() : "null");
            return;
        }

        try {
            Message message = buildMessage(user.getFcmToken(), title, body, data);
            String messageId = firebaseMessaging.get().send(message);
            log.info("Push notification sent to user {} with message ID: {}", user.getId(), messageId);
        } catch (Exception e) {
            log.error("Failed to send push notification to user {}: {}", user.getId(), e.getMessage(), e);
        }
    }

    private Message buildMessage(String fcmToken, String title, String body, Map<String, String> data) {
        Notification notification = Notification.builder()
                .setTitle(title)
                .setBody(body)
                .build();

        Map<String, String> dataPayload = data != null ? new HashMap<>(data) : new HashMap<>();

        return Message.builder()
                .setToken(fcmToken)
                .setNotification(notification)
                .putAllData(dataPayload)
                .build();
    }
}
