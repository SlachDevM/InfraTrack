package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.workorder.dto.CreateWorkOrderRequest;
import com.infratrack.workorder.dto.WorkOrderResponse;
import com.infratrack.model.User;
import com.infratrack.service.UserService;
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
                .map(WorkOrderResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkOrderResponse getById(Long id) {
        return WorkOrderResponse.from(findWorkOrderOrThrow(id));
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
