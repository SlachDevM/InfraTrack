package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.completionreview.CompletionReview;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class OperationalDocumentOwnerResolver {

    private final AssetRepository assetRepository;
    private final InspectionRepository inspectionRepository;
    private final IssueRepository issueRepository;
    private final OperationalDecisionRepository operationalDecisionRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final CompletionReviewRepository completionReviewRepository;

    public OperationalDocumentOwnerResolver(
            AssetRepository assetRepository,
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            WorkOrderRepository workOrderRepository,
            MaintenanceActivityRepository maintenanceActivityRepository,
            CompletionReviewRepository completionReviewRepository) {
        this.assetRepository = assetRepository;
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.workOrderRepository = workOrderRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.completionReviewRepository = completionReviewRepository;
    }

    public void requireAssetExists(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new NotFoundException("Asset not found");
        }
    }

    public OperationalDocumentOwnerContext resolve(
            Long assetId,
            OperationalDocumentOwnerType ownerType,
            Long ownerId) {
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
        return resolveForAsset(asset, ownerType, ownerId);
    }

    OperationalDocumentOwnerContext resolveForAsset(
            Asset asset,
            OperationalDocumentOwnerType ownerType,
            Long ownerId) {
        if (ownerId != null && ownerType == null) {
            throw new BusinessValidationException(
                    "Owner type is required when owner id is provided");
        }

        if (ownerType == null) {
            return new OperationalDocumentOwnerContext(asset, OperationalDocumentOwnerType.ASSET, asset.getId());
        }

        if (ownerType == OperationalDocumentOwnerType.ASSET) {
            if (ownerId != null && !Objects.equals(ownerId, asset.getId())) {
                throw new BusinessValidationException(
                        "Asset owner id must match the target asset");
            }
            return new OperationalDocumentOwnerContext(asset, OperationalDocumentOwnerType.ASSET, asset.getId());
        }

        if (ownerId == null) {
            throw new BusinessValidationException(
                    "Owner id is required for the selected owner type");
        }

        return switch (ownerType) {
            case INSPECTION -> ownerFromInspection(asset, ownerId);
            case ISSUE -> ownerFromIssue(asset, ownerId);
            case OPERATIONAL_DECISION -> ownerFromOperationalDecision(asset, ownerId);
            case WORK_ORDER -> ownerFromWorkOrder(asset, ownerId);
            case MAINTENANCE_ACTIVITY -> ownerFromMaintenanceActivity(asset, ownerId);
            case COMPLETION_REVIEW -> ownerFromCompletionReview(asset, ownerId);
            case ASSET -> new OperationalDocumentOwnerContext(asset, OperationalDocumentOwnerType.ASSET, asset.getId());
        };
    }

    private OperationalDocumentOwnerContext ownerFromInspection(Asset asset, Long ownerId) {
        Inspection inspection = inspectionRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, inspection.getAsset().getId());
        return new OperationalDocumentOwnerContext(asset, OperationalDocumentOwnerType.INSPECTION, inspection.getId());
    }

    private OperationalDocumentOwnerContext ownerFromIssue(Asset asset, Long ownerId) {
        Issue issue = issueRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, issue.getAsset().getId());
        return new OperationalDocumentOwnerContext(asset, OperationalDocumentOwnerType.ISSUE, issue.getId());
    }

    private OperationalDocumentOwnerContext ownerFromOperationalDecision(Asset asset, Long ownerId) {
        OperationalDecision decision = operationalDecisionRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, decision.getAsset().getId());
        return new OperationalDocumentOwnerContext(
                asset, OperationalDocumentOwnerType.OPERATIONAL_DECISION, decision.getId());
    }

    private OperationalDocumentOwnerContext ownerFromWorkOrder(Asset asset, Long ownerId) {
        WorkOrder workOrder = workOrderRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, workOrder.getAsset().getId());
        return new OperationalDocumentOwnerContext(asset, OperationalDocumentOwnerType.WORK_ORDER, workOrder.getId());
    }

    private OperationalDocumentOwnerContext ownerFromMaintenanceActivity(Asset asset, Long ownerId) {
        MaintenanceActivity maintenanceActivity = maintenanceActivityRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, maintenanceActivity.getAsset().getId());
        return new OperationalDocumentOwnerContext(
                asset, OperationalDocumentOwnerType.MAINTENANCE_ACTIVITY, maintenanceActivity.getId());
    }

    private OperationalDocumentOwnerContext ownerFromCompletionReview(Asset asset, Long ownerId) {
        CompletionReview completionReview = completionReviewRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, completionReview.getAsset().getId());
        return new OperationalDocumentOwnerContext(
                asset, OperationalDocumentOwnerType.COMPLETION_REVIEW, completionReview.getId());
    }

    private void requireSameAsset(Asset asset, Long ownerAssetId) {
        if (!Objects.equals(asset.getId(), ownerAssetId)) {
            throw new BusinessValidationException(
                    "Operational owner belongs to another asset");
        }
    }
}
