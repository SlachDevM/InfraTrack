package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.completionreview.CompletionReview;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.operationaldocument.dto.OperationalDocumentEligibleOwnerResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Lists operational records eligible as document owners for a given asset and owner type.
 */
@Service
public class OperationalDocumentOwnerLookupService {

    private final AssetRepository assetRepository;
    private final InspectionRepository inspectionRepository;
    private final IssueRepository issueRepository;
    private final OperationalDecisionRepository operationalDecisionRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final CompletionReviewRepository completionReviewRepository;
    private final OperationalDocumentAuthorizationService authorizationService;
    private final UserService userService;

    public OperationalDocumentOwnerLookupService(
            AssetRepository assetRepository,
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            WorkOrderRepository workOrderRepository,
            MaintenanceActivityRepository maintenanceActivityRepository,
            CompletionReviewRepository completionReviewRepository,
            OperationalDocumentAuthorizationService authorizationService,
            UserService userService) {
        this.assetRepository = assetRepository;
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.workOrderRepository = workOrderRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.completionReviewRepository = completionReviewRepository;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<OperationalDocumentEligibleOwnerResponse> listEligibleOwners(
            Long assetId,
            OperationalDocumentOwnerType ownerType,
            Long userId) {
        if (ownerType == null || ownerType == OperationalDocumentOwnerType.ASSET) {
            throw new BusinessValidationException("Owner type is required");
        }
        User user = userService.getById(userId);
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        authorizationService.requireAssetDepartmentAuthorized(user, asset);

        return switch (ownerType) {
            case INSPECTION -> inspectionRepository.findAllByAsset_IdOrderByCompletedAtDesc(assetId).stream()
                    .map(this::toInspectionOwner)
                    .toList();
            case ISSUE -> issueRepository.findAllByAsset_IdOrderByRecordedAtDesc(assetId).stream()
                    .map(this::toIssueOwner)
                    .toList();
            case OPERATIONAL_DECISION -> operationalDecisionRepository
                    .findAllByAsset_IdOrderByDecidedAtDesc(assetId).stream()
                    .map(this::toOperationalDecisionOwner)
                    .toList();
            case WORK_ORDER -> workOrderRepository.findAllByAsset_IdOrderByCreatedAtDesc(assetId).stream()
                    .map(this::toWorkOrderOwner)
                    .toList();
            case MAINTENANCE_ACTIVITY -> maintenanceActivityRepository
                    .findAllByAsset_IdOrderByCompletedAtDesc(assetId).stream()
                    .map(this::toMaintenanceActivityOwner)
                    .toList();
            case COMPLETION_REVIEW -> completionReviewRepository
                    .findAllByAsset_IdOrderByReviewedAtDesc(assetId).stream()
                    .map(this::toCompletionReviewOwner)
                    .toList();
            case ASSET -> List.of();
        };
    }

    private OperationalDocumentEligibleOwnerResponse toInspectionOwner(Inspection inspection) {
        LocalDate businessDate = inspection.getCompletedAt() != null
                ? inspection.getCompletedAt().toLocalDate()
                : inspection.getExpectedCompletionDate();
        return OperationalDocumentEligibleOwnerResponse.of(
                inspection.getId(),
                "Inspection #" + inspection.getId(),
                inspection.getStatus().name(),
                businessDate,
                inspection.getPriority().name());
    }

    private OperationalDocumentEligibleOwnerResponse toIssueOwner(Issue issue) {
        return OperationalDocumentEligibleOwnerResponse.of(
                issue.getId(),
                "Issue #" + issue.getId(),
                issue.getSeverity().name(),
                issue.getRecordedAt().toLocalDate(),
                truncate(issue.getDescription(), 80));
    }

    private OperationalDocumentEligibleOwnerResponse toOperationalDecisionOwner(OperationalDecision decision) {
        return OperationalDocumentEligibleOwnerResponse.of(
                decision.getId(),
                "Operational Decision #" + decision.getId(),
                decision.getOutcome().name(),
                decision.getDecidedAt().toLocalDate(),
                truncate(decision.getRationale(), 80));
    }

    private OperationalDocumentEligibleOwnerResponse toWorkOrderOwner(WorkOrder workOrder) {
        return OperationalDocumentEligibleOwnerResponse.of(
                workOrder.getId(),
                "Work Order #" + workOrder.getId(),
                workOrder.getStatus().name(),
                workOrder.getCreatedAtBusinessDate().toLocalDate(),
                workOrder.getWorkType().name() + " — " + truncate(workOrder.getDescription(), 60));
    }

    private OperationalDocumentEligibleOwnerResponse toMaintenanceActivityOwner(MaintenanceActivity activity) {
        return OperationalDocumentEligibleOwnerResponse.of(
                activity.getId(),
                "Maintenance Activity #" + activity.getId(),
                activity.getWorkOrder().getStatus().name(),
                activity.getCompletedAt().toLocalDate(),
                truncate(activity.getCompletionNotes(), 80));
    }

    private OperationalDocumentEligibleOwnerResponse toCompletionReviewOwner(CompletionReview review) {
        return OperationalDocumentEligibleOwnerResponse.of(
                review.getId(),
                "Completion Review #" + review.getId(),
                review.getDecision().name(),
                review.getReviewedAt().toLocalDate(),
                truncate(review.getReviewNotes(), 80));
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return "";
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength - 3) + "...";
    }
}
