package com.infratrack.operationaldocument;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.operationaldocument.dto.OperationalDocumentResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationalDocumentServiceTest {

    @Mock
    private OperationalDocumentRepository operationalDocumentRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private OperationalDecisionRepository operationalDecisionRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private MaintenanceActivityRepository maintenanceActivityRepository;

    @Mock
    private CompletionReviewRepository completionReviewRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private OperationalDocumentFileStore fileStore;

    @Mock
    private OperationalDocumentUploadValidator uploadValidator;

    @Mock
    private UserService userService;

    private OperationalDocumentService operationalDocumentService;

    @BeforeEach
    void setUp() {
        OperationalDocumentOwnerResolver ownerResolver = new OperationalDocumentOwnerResolver(
                assetRepository,
                inspectionRepository,
                issueRepository,
                operationalDecisionRepository,
                workOrderRepository,
                maintenanceActivityRepository,
                completionReviewRepository);
        OperationalDocumentAuthorizationService authorizationService = new OperationalDocumentAuthorizationService(
                inspectionRepository,
                issueRepository,
                workOrderRepository,
                maintenanceActivityRepository);
        OperationalDocumentHistoryRecorder historyRecorder =
                new OperationalDocumentHistoryRecorder(assetHistoryEventRepository);
        operationalDocumentService = new OperationalDocumentService(
                operationalDocumentRepository,
                ownerResolver,
                authorizationService,
                historyRecorder,
                fileStore,
                uploadValidator,
                userService);
    }

    @Test
    void uploadDocument_shouldAllowUploadLinkedDirectlyToAsset() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("manual.pdf", "application/pdf", 2048L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("manual.pdf", "application/pdf"));
        when(fileStore.store(file, "application/pdf")).thenReturn(storedFile("manual.pdf", "application/pdf", 2048L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(100L);
            return document;
        });

        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.MANUAL, null, null, null, 10L);

        assertThat(response.getAssetId()).isEqualTo(5L);
        assertThat(response.getOwnerType()).isEqualTo(OperationalDocumentOwnerType.ASSET);
        assertThat(response.getOwnerId()).isEqualTo(5L);
        assertThat(response.getDocumentType()).isEqualTo(OperationalDocumentType.MANUAL);
        assertThat(response.getOriginalFileName()).isEqualTo("manual.pdf");
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getFileSize()).isEqualTo(2048L);
    }

    @Test
    void uploadDocument_shouldAllowUploadLinkedToInspectionOnSameAsset() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("evidence.jpg", "image/jpeg", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("evidence.jpg", "image/jpeg"));
        when(fileStore.store(file, "image/jpeg")).thenReturn(storedFile("evidence.jpg", "image/jpeg", 1024L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(101L);
            return document;
        });

        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.PHOTO,
                OperationalDocumentOwnerType.INSPECTION,
                100L,
                null,
                10L);

        assertThat(response.getOwnerType()).isEqualTo(OperationalDocumentOwnerType.INSPECTION);
        assertThat(response.getOwnerId()).isEqualTo(100L);
        assertThat(response.getDocumentType()).isEqualTo(OperationalDocumentType.PHOTO);
    }

    @Test
    void uploadDocument_shouldAllowUploadLinkedToMaintenanceActivityOnSameAsset() {
        Asset asset = asset(5L);
        MaintenanceActivity maintenanceActivity = maintenanceActivity(500L, asset, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        MultipartFile file = file("completion.jpg", "image/jpeg", 512L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(maintenanceActivityRepository.findById(500L)).thenReturn(Optional.of(maintenanceActivity));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("completion.jpg", "image/jpeg"));
        when(fileStore.store(file, "image/jpeg")).thenReturn(storedFile("completion.jpg", "image/jpeg", 512L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(102L);
            return document;
        });

        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.REPORT,
                OperationalDocumentOwnerType.MAINTENANCE_ACTIVITY,
                500L,
                null,
                20L);

        assertThat(response.getOwnerType()).isEqualTo(OperationalDocumentOwnerType.MAINTENANCE_ACTIVITY);
        assertThat(response.getOwnerId()).isEqualTo(500L);
    }

    @Test
    void uploadDocument_shouldRejectOwnerFromDifferentAsset() {
        Asset asset = asset(5L);
        Asset otherAsset = asset(99L);
        otherAsset.setId(99L);
        Inspection inspection = inspection(100L, otherAsset, 20L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("evidence.jpg", "image/jpeg", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.PHOTO,
                OperationalDocumentOwnerType.INSPECTION,
                100L,
                null,
                10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void uploadDocument_shouldRejectInvalidOwner() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("evidence.jpg", "image/jpeg", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.PHOTO,
                OperationalDocumentOwnerType.INSPECTION,
                999L,
                null,
                10L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void uploadDocument_shouldRejectMissingFile() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = mock(MultipartFile.class);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file))
                .thenThrow(new BusinessValidationException("Document file is required"));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.PHOTO, null, null, null, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void uploadDocument_shouldRejectEmptyFile() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = mock(MultipartFile.class);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file))
                .thenThrow(new BusinessValidationException("Document file is required"));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.REPORT, null, null, null, 10L))
                .extracting(Throwable::getMessage)
                .isEqualTo("Document file is required");
    }

    @Test
    void uploadDocument_shouldRejectMissingDocumentType() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("report.pdf", "application/pdf", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L, file, null, null, null, null, 10L))
                .extracting(Throwable::getMessage)
                .isEqualTo("Document type is required");
    }

    @Test
    void uploadDocument_shouldRejectFutureDocumentDate() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("report.pdf", "application/pdf", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.REPORT,
                null,
                null,
                LocalDate.now().plusDays(1),
                10L))
                .extracting(Throwable::getMessage)
                .isEqualTo("Document date cannot be in the future");
    }

    @Test
    void uploadDocument_shouldRejectUnauthorizedUser() {
        Asset asset = asset(5L);
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        MultipartFile file = file("report.pdf", "application/pdf", 1024L);

        when(userService.getById(1L)).thenReturn(administrator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.REPORT, null, null, null, 1L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void uploadDocument_shouldCreateExactlyOneAssetHistoryEvent() {
        Asset asset = asset(5L);
        User manager = user(30L, UserRole.MANAGER);
        MultipartFile file = file("report.pdf", "application/pdf", 1024L);

        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("report.pdf", "application/pdf"));
        when(fileStore.store(file, "application/pdf")).thenReturn(storedFile("report.pdf", "application/pdf", 1024L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(103L);
            return document;
        });

        operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.REPORT,
                null,
                null,
                LocalDate.of(2026, 6, 20),
                30L);

        ArgumentCaptor<AssetHistoryEvent> captor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository, times(1)).save(captor.capture());
        AssetHistoryEvent event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(AssetHistoryEventType.OPERATIONAL_DOCUMENT_UPLOADED);
        assertThat(event.getAsset().getId()).isEqualTo(5L);
        assertThat(event.getPerformedByUserId()).isEqualTo(30L);
        assertThat(event.getEventDate()).isEqualTo(LocalDate.of(2026, 6, 20));
    }

    @Test
    void uploadDocument_shouldLeaveAssetStatusUnchanged() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("manual.pdf", "application/pdf", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("manual.pdf", "application/pdf"));
        when(fileStore.store(file, "application/pdf")).thenReturn(storedFile("manual.pdf", "application/pdf", 1024L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(104L);
            return document;
        });

        operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.MANUAL, null, null, null, 10L);

        verify(assetRepository, never()).save(any(Asset.class));
    }

    @Test
    void uploadDocument_shouldLeaveWorkflowStateUnchanged() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("photo.jpg", "image/jpeg", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("photo.jpg", "image/jpeg"));
        when(fileStore.store(file, "image/jpeg")).thenReturn(storedFile("photo.jpg", "image/jpeg", 1024L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(105L);
            return document;
        });

        operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.PHOTO,
                OperationalDocumentOwnerType.INSPECTION,
                100L,
                null,
                10L);

        verify(inspectionRepository, never()).save(any(Inspection.class));
        verify(workOrderRepository, never()).save(any(WorkOrder.class));
        verify(maintenanceActivityRepository, never()).save(any(MaintenanceActivity.class));
    }

    @Test
    void uploadDocument_shouldStoreFileMetadata() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("drawing.png", "image/png", 4096L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("drawing.png", "image/png"));
        when(fileStore.store(file, "image/png")).thenReturn(new OperationalDocumentFileStore.StoredFileDetails(
                "stored-uuid",
                "/tmp/stored-uuid",
                "image/png",
                4096L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(106L);
            return document;
        });

        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.DRAWING, null, null, null, 10L);

        ArgumentCaptor<OperationalDocument> captor = ArgumentCaptor.forClass(OperationalDocument.class);
        verify(operationalDocumentRepository).save(captor.capture());
        OperationalDocument saved = captor.getValue();
        assertThat(saved.getOriginalFileName()).isEqualTo("drawing.png");
        assertThat(saved.getStoredFileName()).isEqualTo("stored-uuid");
        assertThat(saved.getContentType()).isEqualTo("image/png");
        assertThat(saved.getFileSize()).isEqualTo(4096L);
        assertThat(saved.getStoragePath()).isEqualTo("/tmp/stored-uuid");
        assertThat(response.getContentType()).isEqualTo("image/png");
    }

    @Test
    void uploadDocument_shouldAllowManagerToUploadDirectlyToAsset() {
        Asset asset = asset(5L);
        User manager = user(30L, UserRole.MANAGER);
        MultipartFile file = file("report.pdf", "application/pdf", 1024L);

        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("report.pdf", "application/pdf"));
        when(fileStore.store(file, "application/pdf")).thenReturn(storedFile("report.pdf", "application/pdf", 1024L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(108L);
            return document;
        });

        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.REPORT, null, null, null, 30L);

        assertThat(response.getOwnerType()).isEqualTo(OperationalDocumentOwnerType.ASSET);
        assertThat(response.getOwnerId()).isEqualTo(5L);
    }

    @Test
    void uploadDocument_shouldAllowManagerToUploadLinkedToIssueOnSameAsset() {
        Asset asset = asset(5L);
        Issue issue = issue(500L, asset);
        User manager = user(30L, UserRole.MANAGER);
        MultipartFile file = file("evidence.jpg", "image/jpeg", 1024L);

        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("evidence.jpg", "image/jpeg"));
        when(fileStore.store(file, "image/jpeg")).thenReturn(storedFile("evidence.jpg", "image/jpeg", 1024L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(109L);
            return document;
        });

        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                5L,
                file,
                OperationalDocumentType.PHOTO,
                OperationalDocumentOwnerType.ISSUE,
                500L,
                null,
                30L);

        assertThat(response.getOwnerType()).isEqualTo(OperationalDocumentOwnerType.ISSUE);
        assertThat(response.getOwnerId()).isEqualTo(500L);
    }

    @Test
    void uploadDocument_shouldRejectFieldEmployeeForAssetLevelUpload() {
        Asset asset = asset(5L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        MultipartFile file = file("photo.jpg", "image/jpeg", 1024L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.PHOTO, null, null, null, 20L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void uploadDocument_shouldAllowBusinessDocumentTypeIndependentOfFileFormat() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        MultipartFile file = file("scan.pdf", "application/pdf", 1024L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("scan.pdf", "application/pdf"));
        when(fileStore.store(file, "application/pdf")).thenReturn(storedFile("scan.pdf", "application/pdf", 1024L));
        when(operationalDocumentRepository.save(any(OperationalDocument.class))).thenAnswer(invocation -> {
            OperationalDocument document = invocation.getArgument(0);
            document.setId(107L);
            return document;
        });

        OperationalDocumentResponse response = operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.PHOTO, null, null, null, 10L);

        assertThat(response.getDocumentType()).isEqualTo(OperationalDocumentType.PHOTO);
        assertThat(response.getContentType()).isEqualTo("application/pdf");
        assertThat(response.getOriginalFileName()).isEqualTo("scan.pdf");
    }

    private MultipartFile file(String originalFilename, String contentType, long size) {
        MultipartFile file = mock(MultipartFile.class);
        lenient().when(file.isEmpty()).thenReturn(false);
        lenient().when(file.getOriginalFilename()).thenReturn(originalFilename);
        lenient().when(file.getContentType()).thenReturn(contentType);
        lenient().when(file.getSize()).thenReturn(size);
        return file;
    }

    private OperationalDocumentFileStore.StoredFileDetails storedFile(
            String originalFilename,
            String contentType,
            long size) {
        return new OperationalDocumentFileStore.StoredFileDetails(
                "stored-file",
                "/tmp/stored-file",
                contentType,
                size);
    }

    private Asset asset(Long id) {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L);
        asset.setId(id);
        return asset;
    }

    private Inspection inspection(Long id, Asset asset, Long assignedToUserId) {
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
                false,
                10L);
        trigger.setId(1L);
        Inspection inspection = new Inspection(
                asset,
                trigger,
                assignedToUserId,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7));
        inspection.setId(id);
        return inspection;
    }

    private MaintenanceActivity maintenanceActivity(Long id, Asset asset, Long performedByUserId) {
        OperationalDecision decision = new OperationalDecision(
                issue(500L, asset),
                asset,
                OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain",
                30L,
                LocalDateTime.now().minusHours(2));
        decision.setId(200L);
        WorkOrder workOrder = new WorkOrder(
                decision,
                asset,
                WorkType.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusHours(1));
        workOrder.setId(1000L);
        workOrder.assign(performedByUserId, 10L, LocalDateTime.now().minusMinutes(30));
        workOrder.complete();

        MaintenanceActivity maintenanceActivity = new MaintenanceActivity(
                workOrder,
                asset,
                performedByUserId,
                "Replaced swing chain",
                LocalDateTime.now().minusMinutes(10));
        maintenanceActivity.setId(id);
        return maintenanceActivity;
    }

    private Issue issue(Long id, Asset asset) {
        Inspection inspection = inspection(100L, asset, 20L);
        inspection.complete(
                PhysicalCondition.POOR,
                "Damaged swing chain observed",
                true,
                LocalDateTime.now().minusHours(2),
                20L);
        Issue issue = new Issue(
                inspection,
                asset,
                "Broken swing chain requires replacement",
                IssueSeverity.HIGH,
                20L,
                LocalDateTime.now().minusHours(1).minusMinutes(30));
        issue.setId(id);
        return issue;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
