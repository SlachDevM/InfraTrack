package com.infratrack.organization.policy.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Selects the active notification policy for the organization.
 *
 * <p>Default is {@link NotificationPolicyMode#DEFAULT} to preserve existing behaviour.
 */
@Service
public class NotificationPolicyService {

    private final NotificationPolicyMode mode;

    private final NotificationPolicy defaultPolicy = new DefaultNotificationPolicy();
    private final NotificationPolicy quietPolicy = new QuietNotificationPolicy();

    public NotificationPolicyService(
            @Value("${APP_POLICIES_NOTIFICATION_MODE:${app.policies.notification.mode:DEFAULT}}")
            String configuredMode) {
        this.mode = parseMode(configuredMode);
    }

    public NotificationPolicy getPolicy() {
        return switch (mode) {
            case DEFAULT -> defaultPolicy;
            case QUIET -> quietPolicy;
        };
    }

    NotificationPolicyMode getMode() {
        return mode;
    }

    private static NotificationPolicyMode parseMode(String configuredMode) {
        if (configuredMode == null || configuredMode.isBlank()) {
            return NotificationPolicyMode.DEFAULT;
        }
        try {
            return NotificationPolicyMode.valueOf(configuredMode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Invalid notification policy mode: " + configuredMode
                            + " (expected DEFAULT or QUIET)");
        }
    }
}
