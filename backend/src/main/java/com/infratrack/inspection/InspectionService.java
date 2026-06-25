package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.model.User;
import com.infratrack.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final BusinessTriggerRepository businessTriggerRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;

    public InspectionService(
            InspectionRepository inspectionRepository,
            BusinessTriggerRepository businessTriggerRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService) {
        this.inspectionRepository = inspectionRepository;
        this.businessTriggerRepository = businessTriggerRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
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

        User assignedByUser = userService.getById(userId);
        return InspectionResponse.from(inspection, assignedToUser, assignedByUser);
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
}
