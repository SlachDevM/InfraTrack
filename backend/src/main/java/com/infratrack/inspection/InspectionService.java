package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final BusinessTriggerRepository businessTriggerRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;
    private final OperationalEventNotificationService operationalEventNotificationService;

    public InspectionService(
            InspectionRepository inspectionRepository,
            BusinessTriggerRepository businessTriggerRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService,
            OperationalEventNotificationService operationalEventNotificationService) {
        this.inspectionRepository = inspectionRepository;
        this.businessTriggerRepository = businessTriggerRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
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
        requireCanAssignInspections(userId);

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

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.INSPECTION_ASSIGNED,
                userId,
                LocalDate.now()
        ));

        operationalEventNotificationService.notifyInspectionAssigned(assignedToUser.getId());

        User assignedByUser = userService.getById(userId);
        return InspectionResponse.from(inspection, assignedToUser, assignedByUser);
    }

    @Transactional
    public InspectionResponse completeInspection(Long inspectionId, CompleteInspectionRequest request, Long userId) {
        Inspection inspection = findInspectionOrThrow(inspectionId);
        User performer = requireAssignedPerformer(userId, inspection);
        requireAssignedStatus(inspection);

        PhysicalCondition observedCondition = validateObservedCondition(request.getObservedCondition());
        String observations = normalizeObservations(request.getObservations());
        boolean issueIdentified = request.getIssueIdentified() != null && request.getIssueIdentified();
        LocalDateTime completedAt = validateCompletedAt(request.getCompletedAt());

        inspection.complete(observedCondition, observations, issueIdentified, completedAt, performer.getId());
        inspectionRepository.save(inspection);

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                inspection.getAsset(),
                AssetHistoryEventType.INSPECTION_COMPLETED,
                performer.getId(),
                completedAt.toLocalDate()
        ));

        return InspectionResponse.from(inspection, performer, null);
    }

    public void requireCanAssignInspections(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isOperationalCoordinator()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only operational coordinators can assign inspections");
        }
    }

    private InspectionResponse toResponse(Inspection inspection) {
        User assignedToUser = userService.getById(inspection.getAssignedToUserId());
        return InspectionResponse.from(inspection, assignedToUser, null);
    }

    private Inspection findInspectionOrThrow(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Inspection not found"));
    }

    private BusinessTrigger findBusinessTriggerOrThrow(Long businessTriggerId) {
        if (businessTriggerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business trigger is required");
        }
        return businessTriggerRepository.findById(businessTriggerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Business trigger not found"));
    }

    private User findAssignableUserOrThrow(Long assignedToUserId) {
        if (assignedToUserId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Assigned user is required");
        }
        User user = userService.getById(assignedToUserId);
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Inspections can only be assigned to field employees or contractors");
        }
        return user;
    }

    private void validateNoActiveAssignment(Long businessTriggerId) {
        if (inspectionRepository.existsByBusinessTriggerIdAndStatus(
                businessTriggerId, InspectionStatus.ASSIGNED)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "An active inspection is already assigned for this business trigger");
        }
    }

    private void validateExpectedCompletionDate(LocalDate expectedCompletionDate) {
        if (expectedCompletionDate != null && expectedCompletionDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
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

    private User requireAssignedPerformer(Long userId, Inspection inspection) {
        User user = userService.getById(userId);
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only field employees and contractors can perform inspections");
        }
        if (!inspection.getAssignedToUserId().equals(userId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the assigned user can perform this inspection");
        }
        return user;
    }

    private void requireAssignedStatus(Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.ASSIGNED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Only assigned inspections can be completed");
        }
    }

    private PhysicalCondition validateObservedCondition(PhysicalCondition observedCondition) {
        if (observedCondition == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Observed condition is required");
        }
        return observedCondition;
    }

    private String normalizeObservations(String observations) {
        if (observations == null || observations.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inspection observations are required");
        }
        return observations.trim();
    }

    private LocalDateTime validateCompletedAt(LocalDateTime completedAt) {
        if (completedAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Completion date and time are required");
        }
        if (completedAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Completion date and time cannot be in the future");
        }
        return completedAt;
    }
}
