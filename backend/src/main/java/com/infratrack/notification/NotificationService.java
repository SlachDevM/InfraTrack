package com.infratrack.notification;

import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final FirebaseNotificationService firebaseNotificationService;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            FirebaseNotificationService firebaseNotificationService
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.firebaseNotificationService = firebaseNotificationService;
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository
                .countByUserIdAndIsReadFalse(userId);
    }

    public Notification markAsRead(Long notificationId) {
        Notification notification =
                notificationRepository.findById(notificationId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        notification.setIsRead(true);

        return notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> notifications =
                getUnreadNotifications(userId);

        notifications.forEach(n -> n.setIsRead(true));

        notificationRepository.saveAll(notifications);
    }

    public Notification create(
            Long userId,
            String title,
            String message
    ) {
        Notification notification =
                new Notification(userId, title, message);

        Notification savedNotification = notificationRepository.save(notification);

        try {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) {
                Map<String, String> data = new HashMap<>();
                data.put("notificationId", String.valueOf(savedNotification.getId()));

                firebaseNotificationService.sendToUser(user, title, message, data);
            }
        } catch (Exception e) {
            // Log error but do not throw - notification must be persisted even if FCM fails
        }

        return savedNotification;
    }
}