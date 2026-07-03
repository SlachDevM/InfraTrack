package com.infratrack.organization.policy.notification;

/**
 * Reduced-notification policy for larger organizations.
 *
 * <p>Preserves notifications that require direct action by an assignee or reviewer.
 * Suppresses informational notifications to managers and operational coordinators
 * that do not require an immediate individual response.
 */
public class QuietNotificationPolicy extends DefaultNotificationPolicy {

    @Override
    public boolean shouldNotifyMaintenanceCompleted() {
        return false;
    }
}
