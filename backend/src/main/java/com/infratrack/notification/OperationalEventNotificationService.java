package com.infratrack.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Sends operational notifications triggered by business events (UC-013).
 * Delivery is best-effort and must not affect the triggering business transaction.
 */
@Service
public class OperationalEventNotificationService {

    private static final Logger log = LoggerFactory.getLogger(OperationalEventNotificationService.class);

    public static final String INSPECTION_ASSIGNED_TITLE = "Inspection Assigned";
    public static final String INSPECTION_ASSIGNED_MESSAGE = "You have been assigned an inspection.";
    public static final String WORK_ORDER_ASSIGNED_TITLE = "Work Order Assigned";
    public static final String WORK_ORDER_ASSIGNED_MESSAGE = "You have been assigned a work order.";
    public static final String MAINTENANCE_COMPLETED_TITLE = "Maintenance Completed";
    public static final String MAINTENANCE_COMPLETED_MESSAGE = "Maintenance has been completed.";
    public static final String COMPLETION_REVIEW_REQUIRED_TITLE = "Completion Review Required";
    public static final String COMPLETION_REVIEW_REQUIRED_MESSAGE = "Contractor maintenance requires completion review.";

    private final NotificationService notificationService;

    public OperationalEventNotificationService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public void notifyInspectionAssigned(Long assignedToUserId) {
        sendSafely(assignedToUserId, INSPECTION_ASSIGNED_TITLE, INSPECTION_ASSIGNED_MESSAGE);
    }

    public void notifyWorkOrderAssigned(Long assignedToUserId) {
        sendSafely(assignedToUserId, WORK_ORDER_ASSIGNED_TITLE, WORK_ORDER_ASSIGNED_MESSAGE);
    }

    /**
     * Deferred until a reliable manager recipient can be resolved without inventing
     * department-manager relationships.
     */
    public void notifyMaintenanceCompleted(Long managerUserId) {
        sendSafely(managerUserId, MAINTENANCE_COMPLETED_TITLE, MAINTENANCE_COMPLETED_MESSAGE);
    }

    /**
     * Deferred until a reliable manager recipient can be resolved without inventing
     * department-manager relationships.
     */
    public void notifyCompletionReviewRequired(Long managerUserId) {
        sendSafely(managerUserId, COMPLETION_REVIEW_REQUIRED_TITLE, COMPLETION_REVIEW_REQUIRED_MESSAGE);
    }

    private void sendSafely(Long userId, String title, String message) {
        try {
            notificationService.create(userId, title, message);
        } catch (Exception ex) {
            log.warn("Failed to send notification '{}' to user {}: {}", title, userId, ex.getMessage());
        }
    }
}
