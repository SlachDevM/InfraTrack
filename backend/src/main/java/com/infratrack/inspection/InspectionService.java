package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.inspection.dto.InspectionSummaryResponse;
import com.infratrack.user.UserNameLookup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Assigns and completes inspections (UC-003, UC-004) and records asset history events.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final BusinessTriggerRepository businessTriggerRepository;
    private final InspectionAuthorizationService authorizationService;
    private final InspectionHistoryRecorder historyRecorder;
    private final UserService userService;
    private final UserNameLookup userNameLookup;
    private final OperationalEventNotificationService operationalEventNotificationService;

    public InspectionService(
            InspectionRepository inspectionRepository,
            BusinessTriggerRepository businessTriggerRepository,
            InspectionAuthorizationService authorizationService,
            InspectionHistoryRecorder historyRecorder,
            UserService userService,
            UserNameLookup userNameLookup,
            OperationalEventNotificationService operationalEventNotificationService) {
        this.inspectionRepository = inspectionRepository;
        this.businessTriggerRepository = businessTriggerRepository;
        this.authorizationService = authorizationService;
        this.historyRecorder = historyRecorder;
        this.userService = userService;
        this.userNameLookup = userNameLookup;
        this.operationalEventNotificationService = operationalEventNotificationService;
    }

    @Transactional(readOnly = true)
    public Page<InspectionSummaryResponse> listPage(Pageable pageable) {
        Page<Inspection> page = inspectionRepository.findAllByOrderByCreatedAtDesc(pageable);
        Map<Long, String> userNames = userNameLookup.resolveNames(
                page.getContent().stream()
                        .map(Inspection::getAssignedToUserId)
                        .filter(Objects::nonNull)
                        .toList());
        return page.map(inspection -> InspectionSummaryResponse.from(inspection, userNames));
    }

    @Transactional(readOnly = true)
    public InspectionResponse getById(Long id) {
        return toResponse(findInspectionOrThrow(id));
    }

    @Transactional
    public InspectionResponse assignInspection(AssignInspectionRequest request, Long userId) {
        User coordinator = userService.getById(userId);
        authorizationService.requireCanAssignInspections(coordinator);

        BusinessTrigger businessTrigger = findBusinessTriggerOrThrow(request.getBusinessTriggerId());
        Asset asset = businessTrigger.getAsset();
        requireOwnDepartment(coordinator, asset);
        User assignedToUser = findAssignableUserOrThrow(request.getAssignedToUserId(), coordinator, asset);
        validateNoActiveAssignment(businessTrigger.getId());
        validateExpectedCompletionDate(request.getExpectedCompletionDate());

        InspectionPriority priority = resolvePriority(businessTrigger, request.getPriority());

        Inspection inspection = inspectionRepository.save(new Inspection(
                asset,
                businessTrigger,
                assignedToUser.getId(),
                userId,
                priority,
                request.getExpectedCompletionDate()
        ));

        historyRecorder.recordInspectionAssigned(asset, userId, LocalDate.now());

        operationalEventNotificationService.notifyInspectionAssigned(assignedToUser.getId());

        return InspectionResponse.from(inspection, assignedToUser, coordinator);
    }

    void requireOwnDepartment(User user, Asset asset) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (userDepartment == null || assetDepartment == null
                || !userDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "You may only assign inspections for assets in your own department.");
        }
    }

    @Transactional
    public InspectionResponse completeInspection(Long inspectionId, CompleteInspectionRequest request, Long userId) {
        Inspection inspection = findInspectionOrThrow(inspectionId);
        User performer = authorizationService.requireAssignedPerformer(userId, inspection);
        requireAssignedStatus(inspection);

        PhysicalCondition observedCondition = validateObservedCondition(request.getObservedCondition());
        String observations = normalizeObservations(request.getObservations());
        boolean issueIdentified = request.getIssueIdentified() != null && request.getIssueIdentified();
        LocalDateTime completedAt = validateCompletedAt(request.getCompletedAt());

        inspection.complete(observedCondition, observations, issueIdentified, completedAt, performer.getId());
        inspectionRepository.save(inspection);

        historyRecorder.recordInspectionCompleted(
                inspection.getAsset(), performer.getId(), completedAt.toLocalDate());

        return InspectionResponse.from(inspection, performer, null);
    }

    public void requireCanAssignInspections(Long userId) {
        authorizationService.requireCanAssignInspections(userId);
    }

    private InspectionResponse toResponse(Inspection inspection) {
        User assignedToUser = userService.getById(inspection.getAssignedToUserId());
        return InspectionResponse.from(inspection, assignedToUser, null);
    }

    private Inspection findInspectionOrThrow(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inspection not found"));
    }

    private BusinessTrigger findBusinessTriggerOrThrow(Long businessTriggerId) {
        if (businessTriggerId == null) {
            throw new BusinessValidationException("Business trigger is required");
        }
        return businessTriggerRepository.findById(businessTriggerId)
                .orElseThrow(() -> new BusinessValidationException("Business trigger not found"));
    }

    private User findAssignableUserOrThrow(Long assignedToUserId, User coordinator, Asset asset) {
        if (assignedToUserId == null) {
            throw new BusinessValidationException("Assigned user is required");
        }
        User user = userService.getById(assignedToUserId);
        if (!user.getRole().isFieldEmployee()) {
            throw new ForbiddenOperationException("Assigned user is not a Field Employee.");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ForbiddenOperationException("Assigned worker is disabled.");
        }
        requireAssigneeDepartment(user, coordinator, asset);
        return user;
    }

    private void requireAssigneeDepartment(User assignee, User coordinator, Asset asset) {
        Department assigneeDepartment = assignee.getDepartment();
        Department coordinatorDepartment = coordinator.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (coordinatorDepartment == null || assigneeDepartment == null || assetDepartment == null
                || !assigneeDepartment.getId().equals(coordinatorDepartment.getId())
                || !assigneeDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "Assigned worker must belong to your department.");
        }
    }

    private void validateNoActiveAssignment(Long businessTriggerId) {
        if (inspectionRepository.existsByBusinessTriggerIdAndStatus(
                businessTriggerId, InspectionStatus.ASSIGNED)) {
            throw new BusinessValidationException(
                    "An active inspection is already assigned for this business trigger");
        }
    }

    private void validateExpectedCompletionDate(LocalDate expectedCompletionDate) {
        if (expectedCompletionDate != null && expectedCompletionDate.isBefore(LocalDate.now())) {
            throw new BusinessValidationException(
                    "Expected completion date cannot be in the past");
        }
    }

    private InspectionPriority resolvePriority(BusinessTrigger businessTrigger, InspectionPriority requested) {
        if (requested != null) {
            return requested;
        }
        if (businessTrigger.isUrgent()) {
            return InspectionPriority.URGENT;
        }
        return InspectionPriority.NORMAL;
    }

    private void requireAssignedStatus(Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.ASSIGNED) {
            throw new BusinessValidationException(
                    "Only assigned inspections can be completed");
        }
    }

    private PhysicalCondition validateObservedCondition(PhysicalCondition observedCondition) {
        if (observedCondition == null) {
            throw new BusinessValidationException("Observed condition is required");
        }
        return observedCondition;
    }

    private String normalizeObservations(String observations) {
        if (observations == null || observations.isBlank()) {
            throw new BusinessValidationException("Inspection observations are required");
        }
        return observations.trim();
    }

    private LocalDateTime validateCompletedAt(LocalDateTime completedAt) {
        if (completedAt == null) {
            throw new BusinessValidationException("Completion date and time are required");
        }
        if (completedAt.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException(
                    "Completion date and time cannot be in the future");
        }
        return completedAt;
    }
}
