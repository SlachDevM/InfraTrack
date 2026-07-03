package com.infratrack.organization.policy.notification;

import org.springframework.stereotype.Service;

/**
 * Selects the active notification policy for the organization.
 *
 * <p>No configuration exists yet; the default policy preserves existing behaviour.
 */
@Service
public class NotificationPolicyService {

    private final NotificationPolicy defaultPolicy = new DefaultNotificationPolicy();

    public NotificationPolicy getPolicy() {
        return defaultPolicy;
    }
}
