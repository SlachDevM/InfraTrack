package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final BusinessTriggerRepository businessTriggerRepository;
    private final InspectionAuthorizationService authorizationService;
    private final InspectionHistoryRecorder historyRecorder;
    private final UserService userService;
    private final OperationalEventNotificationService operationalEventNotificationService;

    public InspectionService(
            InspectionRepository inspectionRepository,
            BusinessTriggerRepository businessTriggerRepository,
            InspectionAuthorizationService authorizationService,
            InspectionHistoryRecorder historyRecorder,
            UserService userService,
            OperationalEventNotificationService operationalEventNotificationService) {
        this.inspectionRepository = inspectionRepository;
        this.businessTriggerRepository = businessTriggerRepository;
        this.authorizationService = authorizationService;
        this.historyRecorder = historyRecorder;
        this.userService = userService;
        this.operationalEventNotificationService = operationalEventNotificationService;
    }

    @Transactional(readOnly = true)
    public List<InspectionResponse> listAll() {
        return inspectionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InspectionResponse getById(Long id) {
        return toResponse(findInspectionOrThrow(id));
    }

    @Transactional
    public InspectionResponse assignInspection(AssignInspectionRequest request, Long userId) {
        authorizationService.requireCanAssignInspections(userId);

        BusinessTrigger businessTrigger = findBusinessTriggerOrThrow(request.getBusinessTriggerId());
        User assignedToUser = findAssignableUserOrThrow(request.getAssignedToUserId());
        validateNoActiveAssignment(businessTrigger.getId());
        validateExpectedCompletionDate(request.getExpectedCompletionDate());

        Asset asset = businessTrigger.getAsset();
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

        User assignedByUser = userService.getById(userId);
        return InspectionResponse.from(inspection, assignedToUser, assignedByUser);
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

    private User findAssignableUserOrThrow(Long assignedToUserId) {
        if (assignedToUserId == null) {
            throw new BusinessValidationException("Assigned user is required");
        }
        User user = userService.getById(assignedToUserId);
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            throw new BusinessValidationException(
                    "Inspections can only be assigned to field employees or contractors");
        }
        return user;
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
