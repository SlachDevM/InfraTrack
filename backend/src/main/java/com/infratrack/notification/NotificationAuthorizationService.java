package com.infratrack.notification;

import com.infratrack.exception.ForbiddenOperationException;
import org.springframework.stereotype.Service;

/**
 * Enforces ownership rules for notification read-state mutations.
 */
@Service
public class NotificationAuthorizationService {

    public void requireNotificationOwner(Long authenticatedUserId, Notification notification) {
        if (!notification.getUserId().equals(authenticatedUserId)) {
            throw new ForbiddenOperationException("You may only mark your own notifications as read.");
        }
    }
}
