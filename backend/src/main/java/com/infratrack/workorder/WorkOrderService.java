package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.organization.policy.approval.ApprovalPolicyService;
import com.infratrack.organization.policy.notification.NotificationPolicyService;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserService;
import com.infratrack.workorder.dto.AssignWorkOrderRequest;
import com.infratrack.workorder.dto.CreateWorkOrderRequest;
import com.infratrack.workorder.dto.WorkOrderResponse;
import com.infratrack.workorder.dto.WorkOrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Creates, assigns and completes work orders (UC-008) following operational decision outcomes.
 */
@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final OperationalDecisionRepository operationalDecisionRepository;
    private final WorkOrderAuthorizationService authorizationService;
    private final WorkOrderHistoryRecorder historyRecorder;
    private final UserService userService;
    private final UserNameLookup userNameLookup;
    private final OperationalEventNotificationService operationalEventNotificationService;
    private final NotificationPolicyService notificationPolicyService;
    private final ApprovalPolicyService approvalPolicyService;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            WorkOrderAuthorizationService authorizationService,
            WorkOrderHistoryRecorder historyRecorder,
            UserService userService,
            UserNameLookup userNameLookup,
            OperationalEventNotificationService operationalEventNotificationService,
            NotificationPolicyService notificationPolicyService,
            ApprovalPolicyService approvalPolicyService) {
        this.workOrderRepository = workOrderRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.authorizationService = authorizationService;
        this.historyRecorder = historyRecorder;
        this.userService = userService;
        this.userNameLookup = userNameLookup;
        this.operationalEventNotificationService = operationalEventNotificationService;
        this.notificationPolicyService = notificationPolicyService;
        this.approvalPolicyService = approvalPolicyService;
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderSummaryResponse> listPage(Pageable pageable) {
        Page<WorkOrder> page = workOrderRepository.findAllByOrderByCreatedAtDesc(pageable);
        Map<Long, String> userNames = userNameLookup.resolveNames(
                page.getContent().stream()
                        .map(WorkOrder::getAssignedToUserId)
                        .filter(Objects::nonNull)
                        .toList());
        return page.map(workOrder -> WorkOrderSummaryResponse.from(workOrder, userNames));
    }

    @Transactional(readOnly = true)
    public Page<WorkOrderSummaryResponse> listEligibleForAssignmentPage(Long userId, Pageable pageable) {
        User coordinator = authorizationService.requireOperationalCoordinatorForAssignment(userId);
        Long coordinatorDepartmentId = coordinator.getDepartment() != null
                ? coordinator.getDepartment().getId()
                : null;
        Page<WorkOrder> page = workOrderRepository.findEligibleForAssignment(
                coordinatorDepartmentId,
                pageable);
        return page.map(workOrder -> WorkOrderSummaryResponse.from(workOrder, Map.of()));
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getById(Long id) {
        return toResponse(findWorkOrderOrThrow(id));
    }

    @Transactional
    public WorkOrderResponse createWorkOrder(CreateWorkOrderRequest request, Long userId) {
        requireManagerOperationalDecisionEnabled();
        User coordinator = authorizationService.requireOperationalCoordinator(userId);
        OperationalDecision decision = findOperationalDecisionOrThrow(request.getOperationalDecisionId());
        WorkType workType = requirePhysicalWorkOutcome(decision);
        requireNoExistingWorkOrder(decision.getId());

        String description = normalizeDescription(request.getDescription());
        WorkOrderPriority priority = validatePriority(request.getPriority());
        LocalDateTime createdAtBusinessDate = validateCreatedAtBusinessDate(
                request.getCreatedAtBusinessDate(),
                decision
        );

        Asset asset = decision.getAsset();
        authorizationService.requireCoordinatorOwnDepartment(coordinator, asset);
        WorkOrder workOrder = workOrderRepository.save(new WorkOrder(
                decision,
                asset,
                workType,
                description,
                priority,
                coordinator.getId(),
                createdAtBusinessDate
        ));

        historyRecorder.recordWorkOrderCreated(asset, coordinator.getId(), createdAtBusinessDate.toLocalDate());

        return WorkOrderResponse.from(workOrder);
    }

    @Transactional
    public WorkOrderResponse assignWorkOrder(Long workOrderId, AssignWorkOrderRequest request, Long userId) {
        User coordinator = authorizationService.requireOperationalCoordinatorForAssignment(userId);
        WorkOrder workOrder = findWorkOrderOrThrow(workOrderId);
        authorizationService.requireCoordinatorOwnDepartment(coordinator, workOrder.getAsset());
        requireCreatedStatus(workOrder);
        LocalDateTime assignedAt = validateAssignedAt(request.getAssignedAt(), workOrder);
        User assignee = authorizationService.requireEligibleAssignee(
                request.getAssignedToUserId(),
                workOrder.getWorkType(),
                workOrder.getAsset());

        workOrder.assign(assignee.getId(), coordinator.getId(), assignedAt);
        workOrderRepository.save(workOrder);

        historyRecorder.recordWorkOrderAssigned(workOrder.getAsset(), coordinator.getId(), assignedAt.toLocalDate());

        if (notificationPolicyService.getPolicy().shouldNotifyWorkOrderAssignment()) {
            operationalEventNotificationService.notifyWorkOrderAssigned(assignee.getId());
        }

        return WorkOrderResponse.from(workOrder, assignee);
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        User assignedToUser = workOrder.getAssignedToUserId() != null
                ? userService.getById(workOrder.getAssignedToUserId())
                : null;
        return WorkOrderResponse.from(workOrder, assignedToUser);
    }

    private void requireCreatedStatus(WorkOrder workOrder) {
        if (workOrder.getStatus() != WorkOrderStatus.CREATED) {
            throw new ConflictException(
                    "Work order has already been assigned");
        }
    }

    private WorkOrder findWorkOrderOrThrow(Long id) {
        return workOrderRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Work order not found"));
    }

    private OperationalDecision findOperationalDecisionOrThrow(Long operationalDecisionId) {
        if (operationalDecisionId == null) {
            throw new BusinessValidationException("Operational decision is required");
        }
        return operationalDecisionRepository.findDetailedById(operationalDecisionId)
                .orElseThrow(() -> new BusinessValidationException("Operational decision not found"));
    }

    private LocalDateTime validateAssignedAt(LocalDateTime assignedAt, WorkOrder workOrder) {
        if (assignedAt == null) {
            throw new BusinessValidationException("Assignment date and time are required");
        }
        if (assignedAt.isBefore(workOrder.getCreatedAtBusinessDate())) {
            throw new BusinessValidationException(
                    "Assignment date and time cannot be before the work order was created");
        }
        if (assignedAt.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException(
                    "Assignment date and time cannot be in the future");
        }
        return assignedAt;
    }

    private WorkType requirePhysicalWorkOutcome(OperationalDecision decision) {
        OperationalDecisionOutcome outcome = decision.getOutcome();
        if (outcome == OperationalDecisionOutcome.INTERNAL_MAINTENANCE) {
            return WorkType.INTERNAL_MAINTENANCE;
        }
        if (outcome == OperationalDecisionOutcome.CONTRACTOR_WORK) {
            return WorkType.CONTRACTOR_WORK;
        }
        throw new BusinessValidationException(
                "Work orders can only be created for internal maintenance or contractor work decisions");
    }

    private void requireNoExistingWorkOrder(Long operationalDecisionId) {
        if (workOrderRepository.existsByOperationalDecisionId(operationalDecisionId)) {
            throw new ConflictException(
                    "A work order has already been created for this operational decision");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessValidationException("Work order description is required");
        }
        return description.trim();
    }

    private WorkOrderPriority validatePriority(WorkOrderPriority priority) {
        if (priority == null) {
            throw new BusinessValidationException("Work order priority is required");
        }
        return priority;
    }

    private LocalDateTime validateCreatedAtBusinessDate(LocalDateTime createdAtBusinessDate, OperationalDecision decision) {
        if (createdAtBusinessDate == null) {
            throw new BusinessValidationException("Work order creation date and time are required");
        }
        if (createdAtBusinessDate.isBefore(decision.getDecidedAt())) {
            throw new BusinessValidationException(
                    "Work order creation date and time cannot be before the operational decision");
        }
        if (createdAtBusinessDate.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException(
                    "Work order creation date and time cannot be in the future");
        }
        return createdAtBusinessDate;
    }

    private void requireManagerOperationalDecisionEnabled() {
        if (!approvalPolicyService.getPolicy().requiresManagerOperationalDecision()) {
            throw new BusinessValidationException(
                    "Work orders require manager operational decisions under the current approval policy");
        }
    }
}
