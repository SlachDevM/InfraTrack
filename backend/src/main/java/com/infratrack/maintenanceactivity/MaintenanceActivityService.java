package com.infratrack.maintenanceactivity;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.completionreview.CompletionReview;
import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.completionreview.CompletionReviewAuthorizationService;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.maintenanceactivity.dto.CompleteMaintenanceActivityRequest;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.organization.policy.approval.ApprovalPolicyService;
import com.infratrack.organization.policy.notification.NotificationPolicyService;
import com.infratrack.time.WorkflowClock;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Records maintenance performed against assigned work orders (UC-009).
 */
@Service
public class MaintenanceActivityService {

    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;
    private final CompletionReviewRepository completionReviewRepository;
    private final OperationalEventNotificationService operationalEventNotificationService;
    private final NotificationPolicyService notificationPolicyService;
    private final ApprovalPolicyService approvalPolicyService;
    private final CompletionReviewAuthorizationService completionReviewAuthorizationService;
    private final WorkflowClock workflowClock;
    private final MaintenanceActivityAuthorizationService maintenanceActivityAuthorizationService;

    public MaintenanceActivityService(
            MaintenanceActivityRepository maintenanceActivityRepository,
            WorkOrderRepository workOrderRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService,
            CompletionReviewRepository completionReviewRepository,
            OperationalEventNotificationService operationalEventNotificationService,
            NotificationPolicyService notificationPolicyService,
            ApprovalPolicyService approvalPolicyService,
            CompletionReviewAuthorizationService completionReviewAuthorizationService,
            WorkflowClock workflowClock,
            MaintenanceActivityAuthorizationService maintenanceActivityAuthorizationService) {
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.workOrderRepository = workOrderRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
        this.completionReviewRepository = completionReviewRepository;
        this.operationalEventNotificationService = operationalEventNotificationService;
        this.notificationPolicyService = notificationPolicyService;
        this.approvalPolicyService = approvalPolicyService;
        this.completionReviewAuthorizationService = completionReviewAuthorizationService;
        this.workflowClock = workflowClock;
        this.maintenanceActivityAuthorizationService = maintenanceActivityAuthorizationService;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceActivityResponse> listForUser(Long userId) {
        User user = userService.getById(userId);
        List<MaintenanceActivity> activities = findVisibleActivities(user);
        return activities.stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaintenanceActivityResponse getById(Long maintenanceActivityId, Long userId) {
        User user = userService.getById(userId);
        MaintenanceActivity maintenanceActivity = findMaintenanceActivityOrThrow(maintenanceActivityId);
        maintenanceActivityAuthorizationService.requireCanViewMaintenanceActivity(user, maintenanceActivity);
        return toResponse(maintenanceActivity);
    }

    @Transactional(readOnly = true)
    public List<MaintenanceActivityResponse> listEligibleForCompletionReview(Long userId) {
        User manager = completionReviewAuthorizationService.requireManager(userId);
        Long managerDepartmentId = manager.getDepartment() != null
                ? manager.getDepartment().getId()
                : null;
        return maintenanceActivityRepository.findEligibleForCompletionReview(
                        manager.getId(),
                        managerDepartmentId,
                        LocalDateTime.now())
                .stream()
                .map(maintenanceActivity -> MaintenanceActivityResponse.from(maintenanceActivity, null))
                .toList();
    }

    @Transactional
    public MaintenanceActivityResponse completeMaintenance(
            Long workOrderId,
            CompleteMaintenanceActivityRequest request,
            Long userId) {
        User actor = userService.getById(userId);
        WorkOrder workOrder = findWorkOrderOrThrow(workOrderId);
        requireAssignedStatus(workOrder);
        requireAssignedUser(workOrder, userId);
        requireExecutorRole(actor, workOrder.getWorkType());
        requireNoExistingMaintenanceActivity(workOrderId);

        String completionNotes = normalizeCompletionNotes(request.getCompletionNotes());
        LocalDateTime completedAt = workflowClock.now();

        Asset asset = workOrder.getAsset();
        MaintenanceActivity maintenanceActivity = maintenanceActivityRepository.save(new MaintenanceActivity(
                workOrder,
                asset,
                actor.getId(),
                completionNotes,
                completedAt
        ));

        workOrder.complete();
        workOrderRepository.save(workOrder);

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.MAINTENANCE_COMPLETED,
                actor.getId(),
                completedAt.toLocalDate()
        ));

        if (notificationPolicyService.getPolicy().shouldNotifyMaintenanceCompleted()) {
            operationalEventNotificationService.notifyMaintenanceCompleted(asset.getDepartment());
        }
        if (approvalPolicyService.getPolicy().requiresCompletionReview(workOrder.getWorkType())
                && notificationPolicyService.getPolicy().shouldNotifyCompletionReview(workOrder.getWorkType())) {
            operationalEventNotificationService.notifyCompletionReviewRequired(asset.getDepartment());
        }

        return MaintenanceActivityResponse.from(maintenanceActivity);
    }

    private MaintenanceActivityResponse toResponse(MaintenanceActivity maintenanceActivity) {
        CompletionReviewDecision completionReviewDecision = completionReviewRepository
                .findByMaintenanceActivityId(maintenanceActivity.getId())
                .map(CompletionReview::getDecision)
                .orElse(null);
        return MaintenanceActivityResponse.from(maintenanceActivity, completionReviewDecision);
    }

    private WorkOrder findWorkOrderOrThrow(Long workOrderId) {
        return workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new NotFoundException("Work order not found"));
    }

    private void requireAssignedStatus(WorkOrder workOrder) {
        if (workOrder.getStatus() != WorkOrderStatus.ASSIGNED) {
            throw new ConflictException(
                    "Work order must be assigned before maintenance can be completed");
        }
    }

    private void requireAssignedUser(WorkOrder workOrder, Long userId) {
        if (workOrder.getAssignedToUserId() == null) {
            throw new ConflictException("Work order has no assigned user");
        }
        if (!workOrder.getAssignedToUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "Only the assigned worker may complete maintenance for this work order");
        }
    }

    private void requireExecutorRole(User actor, WorkType workType) {
        if (workType == WorkType.INTERNAL_MAINTENANCE && !actor.getRole().isFieldEmployee()) {
            throw new ForbiddenOperationException(
                    "Internal maintenance work orders must be completed by a field employee");
        }
        if (workType == WorkType.CONTRACTOR_WORK && !actor.getRole().isContractor()) {
            throw new ForbiddenOperationException(
                    "Contractor work orders must be completed by a contractor");
        }
    }

    private void requireNoExistingMaintenanceActivity(Long workOrderId) {
        if (maintenanceActivityRepository.existsByWorkOrderId(workOrderId)) {
            throw new ConflictException(
                    "Maintenance has already been completed for this work order");
        }
    }

    private String normalizeCompletionNotes(String completionNotes) {
        if (completionNotes == null || completionNotes.isBlank()) {
            throw new BusinessValidationException("Completion notes are required");
        }
        return completionNotes.trim();
    }

    private List<MaintenanceActivity> findVisibleActivities(User user) {
        if (user.getRole() == null) {
            throw listForbidden();
        }
        if (user.getRole().isAdministrator()) {
            return maintenanceActivityRepository.findAllByOrderByCompletedAtDesc();
        }
        if (user.getRole().isManager()) {
            Long managerDepartmentId = user.getDepartment() != null
                    ? user.getDepartment().getId()
                    : null;
            return maintenanceActivityRepository.findAllVisibleToManager(
                    user.getId(),
                    managerDepartmentId,
                    LocalDateTime.now());
        }
        if (user.getRole().isOperationalCoordinator()) {
            if (user.getDepartment() == null) {
                return List.of();
            }
            return maintenanceActivityRepository.findAllByAsset_Department_IdOrderByCompletedAtDesc(
                    user.getDepartment().getId());
        }
        if (user.getRole().isFieldEmployee() || user.getRole().isContractor()) {
            return maintenanceActivityRepository.findAllVisibleToAssignee(user.getId());
        }
        throw listForbidden();
    }

    private MaintenanceActivity findMaintenanceActivityOrThrow(Long maintenanceActivityId) {
        return maintenanceActivityRepository.findDetailedById(maintenanceActivityId)
                .orElseThrow(() -> new NotFoundException("Maintenance activity not found"));
    }

    private static ForbiddenOperationException listForbidden() {
        return new ForbiddenOperationException("You are not authorized to view maintenance activities.");
    }

}
