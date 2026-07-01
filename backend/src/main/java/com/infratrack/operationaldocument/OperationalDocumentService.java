package com.infratrack.operationaldocument;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.operationaldocument.dto.OperationalDocumentEligibleOwnerResponse;
import com.infratrack.operationaldocument.dto.OperationalDocumentResponse;
import com.infratrack.operationaldocument.dto.OperationalDocumentSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

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
    public Page<OperationalDocumentSummaryResponse> listDocuments(Long assetId, Pageable pageable) {
        ownerResolver.requireAssetExists(assetId);
        return operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(assetId, pageable)
                .map(OperationalDocumentSummaryResponse::from);
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
        authorizationService.requireDownloadAuthorized(user, ownerContext);
        Resource resource = fileStore.loadAsResource(document.getStoragePath());
        return new OperationalDocumentDownload(resource, document.getOriginalFileName(), document.getContentType());
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
