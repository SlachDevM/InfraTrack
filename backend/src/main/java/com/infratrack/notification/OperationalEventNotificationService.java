package com.infratrack.notification;

import com.infratrack.department.Department;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

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
    private final UserRepository userRepository;

    public OperationalEventNotificationService(
            NotificationService notificationService,
            UserRepository userRepository) {
        this.notificationService = notificationService;
        this.userRepository = userRepository;
    }

    public void notifyInspectionAssigned(Long assignedToUserId) {
        sendSafely(assignedToUserId, INSPECTION_ASSIGNED_TITLE, INSPECTION_ASSIGNED_MESSAGE);
    }

    public void notifyWorkOrderAssigned(Long assignedToUserId) {
        sendSafely(assignedToUserId, WORK_ORDER_ASSIGNED_TITLE, WORK_ORDER_ASSIGNED_MESSAGE);
    }

    public void notifyMaintenanceCompleted(Department department) {
        notifyRoleInDepartment(
                department,
                UserRole.OPERATIONAL_COORDINATOR,
                MAINTENANCE_COMPLETED_TITLE,
                MAINTENANCE_COMPLETED_MESSAGE);
    }

    public void notifyCompletionReviewRequired(Department department) {
        notifyRoleInDepartment(
                department,
                UserRole.MANAGER,
                COMPLETION_REVIEW_REQUIRED_TITLE,
                COMPLETION_REVIEW_REQUIRED_MESSAGE);
    }

    private void notifyRoleInDepartment(
            Department department,
            UserRole role,
            String title,
            String message) {
        if (department == null || department.getId() == null) {
            return;
        }

        List<User> recipients = userRepository.findByRoleAndDepartmentId(role, department.getId());
        for (User recipient : recipients) {
            sendSafely(recipient.getId(), title, message);
        }
    }

    private void sendSafely(Long userId, String title, String message) {
        try {
            notificationService.create(userId, title, message);
        } catch (Exception ex) {
            log.warn("Failed to send notification '{}' to user {}: {}", title, userId, ex.getMessage());
        }
    }
}
