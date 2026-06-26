package com.infratrack.maintenanceactivity;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.completionreview.CompletionReview;
import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.maintenanceactivity.dto.CompleteMaintenanceActivityRequest;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class MaintenanceActivityService {

    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final WorkOrderRepository workOrderRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;
    private final CompletionReviewRepository completionReviewRepository;
    private final OperationalEventNotificationService operationalEventNotificationService;

    public MaintenanceActivityService(
            MaintenanceActivityRepository maintenanceActivityRepository,
            WorkOrderRepository workOrderRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService,
            CompletionReviewRepository completionReviewRepository,
            OperationalEventNotificationService operationalEventNotificationService) {
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.workOrderRepository = workOrderRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
        this.completionReviewRepository = completionReviewRepository;
        this.operationalEventNotificationService = operationalEventNotificationService;
    }

    @Transactional(readOnly = true)
    public List<MaintenanceActivityResponse> listAll() {
        return maintenanceActivityRepository.findAllByOrderByCompletedAtDesc().stream()
                .map(this::toResponse)
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
        LocalDateTime completedAt = validateCompletedAt(request.getCompletedAt(), workOrder);

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

        operationalEventNotificationService.notifyMaintenanceCompleted(asset.getDepartment());
        if (workOrder.getWorkType() == WorkType.CONTRACTOR_WORK) {
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
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order not found"));
    }

    private void requireAssignedStatus(WorkOrder workOrder) {
        if (workOrder.getStatus() != WorkOrderStatus.ASSIGNED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Work order must be assigned before maintenance can be completed");
        }
    }

    private void requireAssignedUser(WorkOrder workOrder, Long userId) {
        if (workOrder.getAssignedToUserId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Work order has no assigned user");
        }
        if (!workOrder.getAssignedToUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the assigned worker may complete maintenance for this work order");
        }
    }

    private void requireExecutorRole(User actor, WorkType workType) {
        if (workType == WorkType.INTERNAL_MAINTENANCE && !actor.getRole().isFieldEmployee()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Internal maintenance work orders must be completed by a field employee");
        }
        if (workType == WorkType.CONTRACTOR_WORK && !actor.getRole().isContractor()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Contractor work orders must be completed by a contractor");
        }
    }

    private void requireNoExistingMaintenanceActivity(Long workOrderId) {
        if (maintenanceActivityRepository.existsByWorkOrderId(workOrderId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Maintenance has already been completed for this work order");
        }
    }

    private String normalizeCompletionNotes(String completionNotes) {
        if (completionNotes == null || completionNotes.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion notes are required");
        }
        return completionNotes.trim();
    }

    private LocalDateTime validateCompletedAt(LocalDateTime completedAt, WorkOrder workOrder) {
        if (completedAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion date and time are required");
        }
        if (workOrder.getAssignedAt() != null && completedAt.isBefore(workOrder.getAssignedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completion date and time cannot be before the work order was assigned");
        }
        if (completedAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completion date and time cannot be in the future");
        }
        return completedAt;
    }
}
