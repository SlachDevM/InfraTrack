package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.workorder.dto.AssignWorkOrderRequest;
import com.infratrack.workorder.dto.CreateWorkOrderRequest;
import com.infratrack.workorder.dto.WorkOrderResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class WorkOrderService {

    private final WorkOrderRepository workOrderRepository;
    private final OperationalDecisionRepository operationalDecisionRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;

    public WorkOrderService(
            WorkOrderRepository workOrderRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService) {
        this.workOrderRepository = workOrderRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<WorkOrderResponse> listAll() {
        return workOrderRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getById(Long id) {
        return toResponse(findWorkOrderOrThrow(id));
    }

    @Transactional
    public WorkOrderResponse createWorkOrder(CreateWorkOrderRequest request, Long userId) {
        User coordinator = requireOperationalCoordinator(userId);
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
        WorkOrder workOrder = workOrderRepository.save(new WorkOrder(
                decision,
                asset,
                workType,
                description,
                priority,
                coordinator.getId(),
                createdAtBusinessDate
        ));

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.WORK_ORDER_CREATED,
                coordinator.getId(),
                createdAtBusinessDate.toLocalDate()
        ));

        return WorkOrderResponse.from(workOrder);
    }

    @Transactional
    public WorkOrderResponse assignWorkOrder(Long workOrderId, AssignWorkOrderRequest request, Long userId) {
        User coordinator = requireOperationalCoordinatorForAssignment(userId);
        WorkOrder workOrder = findWorkOrderOrThrow(workOrderId);
        requireCreatedStatus(workOrder);
        LocalDateTime assignedAt = validateAssignedAt(request.getAssignedAt(), workOrder);
        User assignee = findEligibleAssigneeOrThrow(request.getAssignedToUserId(), workOrder.getWorkType());

        workOrder.assign(assignee.getId(), coordinator.getId(), assignedAt);
        workOrderRepository.save(workOrder);

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                workOrder.getAsset(),
                AssetHistoryEventType.WORK_ORDER_ASSIGNED,
                coordinator.getId(),
                assignedAt.toLocalDate()
        ));

        return WorkOrderResponse.from(workOrder, assignee);
    }

    private WorkOrderResponse toResponse(WorkOrder workOrder) {
        User assignedToUser = workOrder.getAssignedToUserId() != null
                ? userService.getById(workOrder.getAssignedToUserId())
                : null;
        return WorkOrderResponse.from(workOrder, assignedToUser);
    }

    private User requireOperationalCoordinatorForAssignment(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isOperationalCoordinator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only operational coordinators can assign work orders");
        }
        return user;
    }

    private void requireCreatedStatus(WorkOrder workOrder) {
        if (workOrder.getStatus() != WorkOrderStatus.CREATED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Work order has already been assigned");
        }
    }

    private User findEligibleAssigneeOrThrow(Long assignedToUserId, WorkType workType) {
        if (assignedToUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned user is required");
        }
        User user = userService.getById(assignedToUserId);
        if (workType == WorkType.INTERNAL_MAINTENANCE && !user.getRole().isFieldEmployee()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Internal maintenance work orders must be assigned to a field employee");
        }
        if (workType == WorkType.CONTRACTOR_WORK && !user.getRole().isContractor()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Contractor work orders must be assigned to a contractor");
        }
        return user;
    }

    private LocalDateTime validateAssignedAt(LocalDateTime assignedAt, WorkOrder workOrder) {
        if (assignedAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assignment date and time are required");
        }
        if (assignedAt.isBefore(workOrder.getCreatedAtBusinessDate())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assignment date and time cannot be before the work order was created");
        }
        if (assignedAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Assignment date and time cannot be in the future");
        }
        return assignedAt;
    }

    private User requireOperationalCoordinator(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isOperationalCoordinator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only operational coordinators can create work orders");
        }
        return user;
    }

    private WorkOrder findWorkOrderOrThrow(Long id) {
        return workOrderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Work order not found"));
    }

    private OperationalDecision findOperationalDecisionOrThrow(Long operationalDecisionId) {
        if (operationalDecisionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operational decision is required");
        }
        return operationalDecisionRepository.findById(operationalDecisionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Operational decision not found"));
    }

    private WorkType requirePhysicalWorkOutcome(OperationalDecision decision) {
        OperationalDecisionOutcome outcome = decision.getOutcome();
        if (outcome == OperationalDecisionOutcome.INTERNAL_MAINTENANCE) {
            return WorkType.INTERNAL_MAINTENANCE;
        }
        if (outcome == OperationalDecisionOutcome.CONTRACTOR_WORK) {
            return WorkType.CONTRACTOR_WORK;
        }
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Work orders can only be created for internal maintenance or contractor work decisions");
    }

    private void requireNoExistingWorkOrder(Long operationalDecisionId) {
        if (workOrderRepository.existsByOperationalDecisionId(operationalDecisionId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A work order has already been created for this operational decision");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work order description is required");
        }
        return description.trim();
    }

    private WorkOrderPriority validatePriority(WorkOrderPriority priority) {
        if (priority == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work order priority is required");
        }
        return priority;
    }

    private LocalDateTime validateCreatedAtBusinessDate(LocalDateTime createdAtBusinessDate, OperationalDecision decision) {
        if (createdAtBusinessDate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Work order creation date and time are required");
        }
        if (createdAtBusinessDate.isBefore(decision.getDecidedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Work order creation date and time cannot be before the operational decision");
        }
        if (createdAtBusinessDate.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Work order creation date and time cannot be in the future");
        }
        return createdAtBusinessDate;
    }
}
