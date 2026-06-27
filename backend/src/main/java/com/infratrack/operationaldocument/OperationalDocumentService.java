package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetRepository;
import com.infratrack.completionreview.CompletionReview;
import com.infratrack.completionreview.CompletionReviewRepository;
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
import com.infratrack.operationaldocument.dto.OperationalDocumentResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
public class OperationalDocumentService {

    private final OperationalDocumentRepository operationalDocumentRepository;
    private final AssetRepository assetRepository;
    private final InspectionRepository inspectionRepository;
    private final IssueRepository issueRepository;
    private final OperationalDecisionRepository operationalDecisionRepository;
    private final WorkOrderRepository workOrderRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final CompletionReviewRepository completionReviewRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final OperationalDocumentFileStore fileStore;
    private final UserService userService;

    public OperationalDocumentService(
            OperationalDocumentRepository operationalDocumentRepository,
            AssetRepository assetRepository,
            InspectionRepository inspectionRepository,
            IssueRepository issueRepository,
            OperationalDecisionRepository operationalDecisionRepository,
            WorkOrderRepository workOrderRepository,
            MaintenanceActivityRepository maintenanceActivityRepository,
            CompletionReviewRepository completionReviewRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            OperationalDocumentFileStore fileStore,
            UserService userService) {
        this.operationalDocumentRepository = operationalDocumentRepository;
        this.assetRepository = assetRepository;
        this.inspectionRepository = inspectionRepository;
        this.issueRepository = issueRepository;
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.workOrderRepository = workOrderRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.completionReviewRepository = completionReviewRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.fileStore = fileStore;
        this.userService = userService;
    }

    @Transactional
    public OperationalDocumentResponse uploadDocument(
            Long assetId,
            MultipartFile file,
            OperationalDocumentType documentType,
            OperationalDocumentOwnerType ownerType,
            Long ownerId,
            LocalDate documentDate,
            Long userId) {
        User user = userService.getById(userId);
        Asset asset = findAssetOrThrow(assetId);
        MultipartFile validatedFile = validateFile(file);
        OperationalDocumentType validatedDocumentType = validateDocumentType(documentType);
        LocalDate validatedDocumentDate = validateDocumentDate(documentDate);
        OwnerContext ownerContext = resolveOwnerContext(asset, ownerType, ownerId);
        requireUploadAuthorized(user, ownerContext);

        OperationalDocumentFileStore.StoredFileDetails storedFile =
                fileStore.store(validatedFile);
        LocalDateTime uploadedAt = LocalDateTime.now();

        OperationalDocument document = operationalDocumentRepository.save(new OperationalDocument(
                asset,
                ownerContext.ownerType(),
                ownerContext.ownerId(),
                validatedDocumentType,
                validatedFile.getOriginalFilename(),
                storedFile.storedFileName(),
                storedFile.contentType(),
                storedFile.fileSize(),
                storedFile.storagePath(),
                validatedDocumentDate,
                user.getId(),
                uploadedAt
        ));

        LocalDate eventDate = validatedDocumentDate != null
                ? validatedDocumentDate
                : uploadedAt.toLocalDate();
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.OPERATIONAL_DOCUMENT_UPLOADED,
                user.getId(),
                eventDate
        ));

        return OperationalDocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public List<OperationalDocumentResponse> listDocuments(Long assetId) {
        requireAssetExists(assetId);
        return operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(assetId).stream()
                .map(OperationalDocumentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperationalDocumentDownload downloadDocument(Long documentId) {
        OperationalDocument document = operationalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        Resource resource = fileStore.loadAsResource(document.getStoragePath());
        return new OperationalDocumentDownload(resource, document.getOriginalFileName(), document.getContentType());
    }

    private Asset findAssetOrThrow(Long assetId) {
        return assetRepository.findById(assetId)
                .orElseThrow(() -> new NotFoundException("Asset not found"));
    }

    private void requireAssetExists(Long assetId) {
        if (!assetRepository.existsById(assetId)) {
            throw new NotFoundException("Asset not found");
        }
    }

    private MultipartFile validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessValidationException("Document file is required");
        }
        if (file.getOriginalFilename() == null || file.getOriginalFilename().isBlank()) {
            throw new BusinessValidationException("Invalid document");
        }
        return file;
    }

    private OperationalDocumentType validateDocumentType(OperationalDocumentType documentType) {
        if (documentType == null) {
            throw new BusinessValidationException("Document type is required");
        }
        return documentType;
    }

    private LocalDate validateDocumentDate(LocalDate documentDate) {
        if (documentDate != null && documentDate.isAfter(LocalDate.now())) {
            throw new BusinessValidationException("Document date cannot be in the future");
        }
        return documentDate;
    }

    private OwnerContext resolveOwnerContext(
            Asset asset,
            OperationalDocumentOwnerType ownerType,
            Long ownerId) {
        if (ownerId != null && ownerType == null) {
            throw new BusinessValidationException(
                    "Owner type is required when owner id is provided");
        }

        if (ownerType == null) {
            return new OwnerContext(OperationalDocumentOwnerType.ASSET, asset.getId());
        }

        if (ownerType == OperationalDocumentOwnerType.ASSET) {
            if (ownerId != null && !Objects.equals(ownerId, asset.getId())) {
                throw new BusinessValidationException(
                        "Asset owner id must match the target asset");
            }
            return new OwnerContext(OperationalDocumentOwnerType.ASSET, asset.getId());
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
            case ASSET -> new OwnerContext(OperationalDocumentOwnerType.ASSET, asset.getId());
        };
    }

    private OwnerContext ownerFromInspection(Asset asset, Long ownerId) {
        Inspection inspection = inspectionRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, inspection.getAsset().getId());
        return new OwnerContext(OperationalDocumentOwnerType.INSPECTION, inspection.getId());
    }

    private OwnerContext ownerFromIssue(Asset asset, Long ownerId) {
        Issue issue = issueRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, issue.getAsset().getId());
        return new OwnerContext(OperationalDocumentOwnerType.ISSUE, issue.getId());
    }

    private OwnerContext ownerFromOperationalDecision(Asset asset, Long ownerId) {
        OperationalDecision decision = operationalDecisionRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, decision.getAsset().getId());
        return new OwnerContext(OperationalDocumentOwnerType.OPERATIONAL_DECISION, decision.getId());
    }

    private OwnerContext ownerFromWorkOrder(Asset asset, Long ownerId) {
        WorkOrder workOrder = workOrderRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, workOrder.getAsset().getId());
        return new OwnerContext(OperationalDocumentOwnerType.WORK_ORDER, workOrder.getId());
    }

    private OwnerContext ownerFromMaintenanceActivity(Asset asset, Long ownerId) {
        MaintenanceActivity maintenanceActivity = maintenanceActivityRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, maintenanceActivity.getAsset().getId());
        return new OwnerContext(OperationalDocumentOwnerType.MAINTENANCE_ACTIVITY, maintenanceActivity.getId());
    }

    private OwnerContext ownerFromCompletionReview(Asset asset, Long ownerId) {
        CompletionReview completionReview = completionReviewRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Operational owner not found"));
        requireSameAsset(asset, completionReview.getAsset().getId());
        return new OwnerContext(OperationalDocumentOwnerType.COMPLETION_REVIEW, completionReview.getId());
    }

    private void requireSameAsset(Asset asset, Long ownerAssetId) {
        if (!Objects.equals(asset.getId(), ownerAssetId)) {
            throw new BusinessValidationException(
                    "Operational owner belongs to another asset");
        }
    }

    private void requireUploadAuthorized(User user, OwnerContext ownerContext) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbiddenUpload();
        }

        switch (role) {
            case ADMINISTRATOR -> throw new ForbiddenOperationException(
                    "Administrators cannot upload operational evidence");
            case MANAGER, OPERATIONAL_COORDINATOR -> {
                // UC-012: Managers and Operational Coordinators may upload in any context.
            }
            case FIELD_EMPLOYEE, CONTRACTOR -> requireFieldUploadAuthorized(user, ownerContext);
            default -> throw forbiddenUpload();
        }
    }

    private ForbiddenOperationException forbiddenUpload() {
        return new ForbiddenOperationException("Unauthorized to upload operational evidence");
    }

    private void requireFieldUploadAuthorized(User user, OwnerContext ownerContext) {
        switch (ownerContext.ownerType()) {
            case ASSET, OPERATIONAL_DECISION, COMPLETION_REVIEW -> throw new ForbiddenOperationException(
                    "Unauthorized to upload operational evidence for this context");
            case INSPECTION -> {
                Inspection inspection = inspectionRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(inspection.getAssignedToUserId(), user.getId())
                        && !Objects.equals(inspection.getCompletedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
            case ISSUE -> {
                Issue issue = issueRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(issue.getRecordedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
            case WORK_ORDER -> {
                WorkOrder workOrder = workOrderRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(workOrder.getAssignedToUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
            case MAINTENANCE_ACTIVITY -> {
                MaintenanceActivity maintenanceActivity = maintenanceActivityRepository.findById(ownerContext.ownerId())
                        .orElseThrow(() -> new NotFoundException("Operational owner not found"));
                if (!Objects.equals(maintenanceActivity.getPerformedByUserId(), user.getId())) {
                    throw new ForbiddenOperationException(
                            "Unauthorized to upload operational evidence for this context");
                }
            }
        }
    }

    private record OwnerContext(OperationalDocumentOwnerType ownerType, Long ownerId) {
    }

    public record OperationalDocumentDownload(Resource resource, String originalFileName, String contentType) {
    }
}
