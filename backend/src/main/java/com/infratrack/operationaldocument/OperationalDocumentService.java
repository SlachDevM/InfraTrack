package com.infratrack.operationaldocument;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.messages.OperationalEvidenceMessages;
import com.infratrack.operationaldocument.dto.OperationalDocumentEligibleOwnerResponse;
import com.infratrack.operationaldocument.dto.OperationalDocumentResponse;
import com.infratrack.operationaldocument.dto.OperationalDocumentSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.infratrack.asset.Asset;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Uploads, lists and downloads operational documents linked to assets (UC-012).
 */
@Service
public class OperationalDocumentService {

    private final OperationalDocumentRepository operationalDocumentRepository;
    private final OperationalDocumentOwnerResolver ownerResolver;
    private final OperationalDocumentAuthorizationService authorizationService;
    private final OperationalDocumentHistoryRecorder historyRecorder;
    private final OperationalDocumentFileStore fileStore;
    private final OperationalDocumentUploadValidator uploadValidator;
    private final UserService userService;
    private final OperationalDocumentOwnerLookupService ownerLookupService;

    public OperationalDocumentService(
            OperationalDocumentRepository operationalDocumentRepository,
            OperationalDocumentOwnerResolver ownerResolver,
            OperationalDocumentAuthorizationService authorizationService,
            OperationalDocumentHistoryRecorder historyRecorder,
            OperationalDocumentFileStore fileStore,
            OperationalDocumentUploadValidator uploadValidator,
            UserService userService,
            OperationalDocumentOwnerLookupService ownerLookupService) {
        this.operationalDocumentRepository = operationalDocumentRepository;
        this.ownerResolver = ownerResolver;
        this.authorizationService = authorizationService;
        this.historyRecorder = historyRecorder;
        this.fileStore = fileStore;
        this.uploadValidator = uploadValidator;
        this.userService = userService;
        this.ownerLookupService = ownerLookupService;
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
        OperationalDocumentOwnerContext ownerContext = ownerResolver.resolve(assetId, ownerType, ownerId);
        OperationalDocumentUploadValidator.ValidatedUpload validatedUpload = uploadValidator.validate(file);
        OperationalDocumentType validatedDocumentType = validateDocumentType(documentType);
        LocalDate validatedDocumentDate = validateDocumentDate(documentDate);
        authorizationService.requireUploadAuthorized(user, ownerContext);

        OperationalDocumentFileStore.StoredFileDetails storedFile =
                fileStore.store(file, validatedUpload.detectedContentType());
        LocalDateTime uploadedAt = LocalDateTime.now();

        OperationalDocument document = operationalDocumentRepository.save(new OperationalDocument(
                ownerContext.asset(),
                ownerContext.ownerType(),
                ownerContext.ownerId(),
                validatedDocumentType,
                validatedUpload.sanitizedFileName(),
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
        historyRecorder.recordUploaded(ownerContext.asset(), user.getId(), eventDate);

        return OperationalDocumentResponse.from(document);
    }

    @Transactional(readOnly = true)
    public List<OperationalDocumentEligibleOwnerResponse> listEligibleOwners(
            Long assetId,
            OperationalDocumentOwnerType ownerType,
            Long userId) {
        return ownerLookupService.listEligibleOwners(assetId, ownerType, userId);
    }

    @Transactional(readOnly = true)
    public Page<OperationalDocumentSummaryResponse> listDocuments(Long assetId, Pageable pageable, Long userId) {
        User user = userService.getById(userId);
        ownerResolver.requireAssetExists(assetId);

        OperationalDocumentOwnerContext assetContext = ownerResolver.resolve(assetId, null, null);
        Asset asset = assetContext.asset();

        if (user.getRole() != null && user.getRole().isAdministrator()) {
            return operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(assetId, pageable)
                    .map(OperationalDocumentSummaryResponse::from);
        }

        if (user.getRole() != null
                && (user.getRole().isManager() || user.getRole().isOperationalCoordinator())) {
            authorizationService.requireAssetDepartmentDownloadAuthorized(user, asset);
            return operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(assetId, pageable)
                    .map(OperationalDocumentSummaryResponse::from);
        }

        if (user.getRole() != null && (user.getRole().isFieldEmployee() || user.getRole().isContractor())) {
            authorizationService.requireAssetDepartmentDownloadAuthorized(user, asset);

            Page<OperationalDocument> page = operationalDocumentRepository.findVisibleToFieldUser(
                    assetId, user.getId(), pageable);
            if (page.getTotalElements() == 0) {
                throw new ForbiddenOperationException(
                        OperationalEvidenceMessages.UNAUTHORIZED_DOWNLOAD_OPERATIONAL_EVIDENCE_CONTEXT);
            }

            return page.map(OperationalDocumentSummaryResponse::from);
        }

        throw new ForbiddenOperationException(OperationalEvidenceMessages.UNAUTHORIZED_DOWNLOAD_OPERATIONAL_EVIDENCE);
    }

    /**
     * Returns asset-owned operational documents visible to the caller for mobile asset context (M4-BE4).
     * Field employees and contractors who can view the asset context (same department) also see
     * asset-owned reference documents (M4-BE4.1). Web asset document listing rules are unchanged.
     */
    @Transactional(readOnly = true)
    public List<OperationalDocument> listVisibleAssetOwnedDocuments(Asset asset, Long userId) {
        User user = userService.getById(userId);
        return listVisibleAssetOwnedDocuments(user, asset);
    }

    @Transactional(readOnly = true)
    public List<OperationalDocument> listVisibleAssetOwnedDocuments(User user, Asset asset) {
        List<OperationalDocument> documents = operationalDocumentRepository
                .findByAssetIdAndOwnerTypeOrderByUploadedAtDesc(asset.getId(), OperationalDocumentOwnerType.ASSET);

        if (user.getRole() != null && user.getRole().isAdministrator()) {
            return documents;
        }

        if (user.getRole() != null
                && (user.getRole().isManager()
                || user.getRole().isOperationalCoordinator()
                || user.getRole().isFieldEmployee()
                || user.getRole().isContractor())) {
            authorizationService.requireAssetDepartmentDownloadAuthorized(user, asset);
            return documents;
        }

        return List.of();
    }

    @Transactional(readOnly = true)
    public Map<Long, List<OperationalDocument>> listVisibleAssetOwnedDocumentsForAssets(
            User user, Collection<Asset> assets) {
        if (assets == null || assets.isEmpty()) {
            return Map.of();
        }

        List<Asset> authorizedAssets = assets.stream()
                .filter(asset -> isAssetOwnedDocumentVisible(user, asset))
                .toList();
        if (authorizedAssets.isEmpty()) {
            return Map.of();
        }

        List<Long> assetIds = authorizedAssets.stream().map(Asset::getId).toList();
        return operationalDocumentRepository
                .findByAssetIdInAndOwnerTypeOrderByUploadedAtDesc(assetIds, OperationalDocumentOwnerType.ASSET)
                .stream()
                .collect(Collectors.groupingBy(document -> document.getAsset().getId()));
    }

    private boolean isAssetOwnedDocumentVisible(User user, Asset asset) {
        if (user.getRole() != null && user.getRole().isAdministrator()) {
            return true;
        }

        if (user.getRole() != null
                && (user.getRole().isManager()
                || user.getRole().isOperationalCoordinator()
                || user.getRole().isFieldEmployee()
                || user.getRole().isContractor())) {
            try {
                authorizationService.requireAssetDepartmentDownloadAuthorized(user, asset);
                return true;
            } catch (ForbiddenOperationException ex) {
                return false;
            }
        }

        return false;
    }

    @Transactional(readOnly = true)
    public OperationalDocumentDownload downloadDocument(Long documentId, Long userId) {
        User user = userService.getById(userId);
        OperationalDocument document = operationalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        OperationalDocumentOwnerContext ownerContext = ownerResolver.resolveForAsset(
                document.getAsset(),
                document.getOwnerType(),
                document.getOwnerId());
        if (isMobileVisibleAssetOwnedDocument(user, ownerContext)) {
            authorizationService.requireAssetDepartmentDownloadAuthorized(user, document.getAsset());
        } else {
            authorizationService.requireDownloadAuthorized(user, ownerContext);
        }
        Resource resource = fileStore.loadAsResource(document.getStoragePath());
        return new OperationalDocumentDownload(resource, document.getOriginalFileName(), document.getContentType());
    }

    private boolean isMobileVisibleAssetOwnedDocument(User user, OperationalDocumentOwnerContext ownerContext) {
        UserRole role = user.getRole();
        return role != null
                && (role.isFieldEmployee() || role.isContractor())
                && ownerContext.ownerType() == OperationalDocumentOwnerType.ASSET;
    }

    @Transactional
    public void deleteDocument(Long documentId, Long userId) {
        User user = userService.getById(userId);
        OperationalDocument document = operationalDocumentRepository.findById(documentId)
                .orElseThrow(() -> new NotFoundException("Document not found"));
        OperationalDocumentOwnerContext ownerContext = ownerResolver.resolveForAsset(
                document.getAsset(),
                document.getOwnerType(),
                document.getOwnerId());
        authorizationService.requireDeleteAuthorized(user, ownerContext);

        fileStore.delete(document.getStoragePath());
        operationalDocumentRepository.delete(document);

        historyRecorder.recordDeleted(document.getAsset(), user.getId(), LocalDate.now());
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

    public record OperationalDocumentDownload(Resource resource, String originalFileName, String contentType) {
    }
}
