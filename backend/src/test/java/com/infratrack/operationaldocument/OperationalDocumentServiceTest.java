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
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
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
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

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
                maintenanceActivityRepository,
                delegatedAuthorityService);
        OperationalDocumentOwnerLookupService ownerLookupService = new OperationalDocumentOwnerLookupService(
                assetRepository,
                inspectionRepository,
                issueRepository,
                operationalDecisionRepository,
                workOrderRepository,
                maintenanceActivityRepository,
                completionReviewRepository,
                authorizationService,
                userService);
        OperationalDocumentHistoryRecorder historyRecorder =
                new OperationalDocumentHistoryRecorder(assetHistoryEventRepository);
        operationalDocumentService = new OperationalDocumentService(
                operationalDocumentRepository,
                ownerResolver,
                authorizationService,
                historyRecorder,
                fileStore,
                uploadValidator,
                userService,
                ownerLookupService);
        lenient().when(delegatedAuthorityService.canManagerActForAssetDepartment(
                any(User.class), any(Department.class), any(LocalDateTime.class))).thenReturn(true);
    }

    @Test
    void listDocuments_shouldAllowAdministratorToListMetadata() {
        Asset asset = asset(5L);
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        Pageable pageable = PageRequest.of(0, 20);

        OperationalDocument assetDocument = savedDocument(200L, asset);
        OperationalDocument inspectionDocument = inspectionDocument(201L, asset, 100L);

        when(userService.getById(1L)).thenReturn(administrator);
        when(assetRepository.existsById(5L)).thenReturn(true);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(assetDocument, inspectionDocument), pageable, 2));

        Page<?> page = operationalDocumentService.listDocuments(5L, pageable, 1L);

        assertThat(page.getContent()).hasSize(2);
    }

    @Test
    void listDocuments_shouldAllowManagerForOwnDepartment() {
        Asset asset = asset(5L);
        User manager = user(30L, UserRole.MANAGER);
        Pageable pageable = PageRequest.of(0, 20);

        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.existsById(5L)).thenReturn(true);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(savedDocument(200L, asset)), pageable, 1));

        Page<?> page = operationalDocumentService.listDocuments(5L, pageable, 30L);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void listDocuments_shouldRejectManagerForCrossDepartmentWithoutListing() {
        Asset asset = asset(5L);
        User manager = user(30L, UserRole.MANAGER);
        Pageable pageable = PageRequest.of(0, 20);

        when(userService.getById(30L)).thenReturn(manager);
        when(assetRepository.existsById(5L)).thenReturn(true);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(delegatedAuthorityService.canManagerActForAssetDepartment(any(User.class), any(Department.class), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> operationalDocumentService.listDocuments(5L, pageable, 30L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only download operational documents for assets in your own department.");

        verify(operationalDocumentRepository, never()).findByAssetIdOrderByUploadedAtDesc(anyLong(), any(Pageable.class));
    }

    @Test
    void listDocuments_shouldAllowOperationalCoordinatorForOwnDepartment() {
        Asset asset = asset(5L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        Pageable pageable = PageRequest.of(0, 20);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.existsById(5L)).thenReturn(true);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(savedDocument(200L, asset)), pageable, 1));

        Page<?> page = operationalDocumentService.listDocuments(5L, pageable, 10L);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void listDocuments_shouldAllowAssignedFieldEmployeeToListMetadataForAuthorizedContext() {
        Asset asset = asset(5L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Pageable pageable = PageRequest.of(0, 20);

        Inspection inspection = inspection(100L, asset, 20L);
        OperationalDocument inspectionDocument = inspectionDocument(201L, asset, 100L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(assetRepository.existsById(5L)).thenReturn(true);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(inspectionDocument), pageable, 1));

        Page<?> page = operationalDocumentService.listDocuments(5L, pageable, 20L);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void listVisibleAssetOwnedDocuments_shouldReturnAssetOwnedDocumentsForAdministrator() {
        Asset asset = asset(5L);
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        OperationalDocument assetDocument = savedDocument(200L, asset);

        when(userService.getById(1L)).thenReturn(administrator);
        when(operationalDocumentRepository.findByAssetIdAndOwnerTypeOrderByUploadedAtDesc(
                5L, OperationalDocumentOwnerType.ASSET))
                .thenReturn(java.util.List.of(assetDocument));

        java.util.List<OperationalDocument> documents =
                operationalDocumentService.listVisibleAssetOwnedDocuments(asset, 1L);

        assertThat(documents).containsExactly(assetDocument);
        verify(operationalDocumentRepository).findByAssetIdAndOwnerTypeOrderByUploadedAtDesc(
                5L, OperationalDocumentOwnerType.ASSET);
    }

    @Test
    void listVisibleAssetOwnedDocuments_shouldReturnEmptyListForFieldEmployeeWhenOnlyAssetOwnedDocumentsExist() {
        Asset asset = asset(5L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        OperationalDocument assetDocument = savedDocument(200L, asset);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(operationalDocumentRepository.findByAssetIdAndOwnerTypeOrderByUploadedAtDesc(
                5L, OperationalDocumentOwnerType.ASSET))
                .thenReturn(java.util.List.of(assetDocument));

        java.util.List<OperationalDocument> documents =
                operationalDocumentService.listVisibleAssetOwnedDocuments(asset, 20L);

        assertThat(documents).isEmpty();
    }

    @Test
    void listVisibleAssetOwnedDocuments_shouldAllowManagerForOwnDepartment() {
        Asset asset = asset(5L);
        User manager = user(30L, UserRole.MANAGER);
        OperationalDocument assetDocument = savedDocument(200L, asset);

        when(userService.getById(30L)).thenReturn(manager);
        when(operationalDocumentRepository.findByAssetIdAndOwnerTypeOrderByUploadedAtDesc(
                5L, OperationalDocumentOwnerType.ASSET))
                .thenReturn(java.util.List.of(assetDocument));

        java.util.List<OperationalDocument> documents =
                operationalDocumentService.listVisibleAssetOwnedDocuments(asset, 30L);

        assertThat(documents).containsExactly(assetDocument);
    }

    @Test
    void listDocuments_shouldRejectUnassignedFieldEmployee() {
        Asset asset = asset(5L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Pageable pageable = PageRequest.of(0, 20);

        Inspection inspection = inspection(100L, asset, 999L);
        OperationalDocument inspectionDocument = inspectionDocument(201L, asset, 100L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(assetRepository.existsById(5L)).thenReturn(true);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(inspectionDocument), pageable, 1));

        assertThatThrownBy(() -> operationalDocumentService.listDocuments(5L, pageable, 20L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Unauthorized to download operational evidence for this context");
    }

    @Test
    void listDocuments_shouldAllowAssignedContractorToListMetadataForAuthorizedContext() {
        Asset asset = asset(5L);
        User contractor = user(25L, UserRole.CONTRACTOR);
        Pageable pageable = PageRequest.of(0, 20);

        WorkOrder workOrder = assignedWorkOrder(1000L, asset, 25L);
        OperationalDocument workOrderDocument = workOrderDocument(301L, asset, 1000L);

        when(userService.getById(25L)).thenReturn(contractor);
        when(assetRepository.existsById(5L)).thenReturn(true);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(operationalDocumentRepository.findByAssetIdOrderByUploadedAtDesc(5L, pageable))
                .thenReturn(new PageImpl<>(java.util.List.of(workOrderDocument), pageable, 1));

        Page<?> page = operationalDocumentService.listDocuments(5L, pageable, 25L);

        assertThat(page.getContent()).hasSize(1);
    }

    @Test
    void listDocuments_shouldPreserveNotFoundWhenAssetDoesNotExist() {
        Pageable pageable = PageRequest.of(0, 20);
        User administrator = user(1L, UserRole.ADMINISTRATOR);

        when(userService.getById(1L)).thenReturn(administrator);
        when(assetRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> operationalDocumentService.listDocuments(999L, pageable, 1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Asset not found");

        verify(operationalDocumentRepository, never()).findByAssetIdOrderByUploadedAtDesc(anyLong(), any(Pageable.class));
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
    void uploadDocument_shouldRejectCrossDepartmentAsset() {
        Asset asset = asset(5L);
        User coordinator = userInDepartment(10L, UserRole.OPERATIONAL_COORDINATOR, 2L);
        MultipartFile file = file("manual.pdf", "application/pdf", 2048L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(uploadValidator.validate(file)).thenReturn(
                new OperationalDocumentUploadValidator.ValidatedUpload("manual.pdf", "application/pdf"));

        assertThatThrownBy(() -> operationalDocumentService.uploadDocument(
                5L, file, OperationalDocumentType.MANUAL, null, null, null, 10L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(operationalDocumentRepository, never()).save(any());
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

    @Test
    void deleteDocument_shouldDeleteAuthorizedDocument() {
        Asset asset = asset(5L);
        User manager = user(30L, UserRole.MANAGER);
        OperationalDocument document = savedDocument(103L, asset);

        when(userService.getById(30L)).thenReturn(manager);
        when(operationalDocumentRepository.findById(103L)).thenReturn(Optional.of(document));
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);

        operationalDocumentService.deleteDocument(103L, 30L);

        verify(fileStore).delete("/tmp/stored-file");
        verify(operationalDocumentRepository).delete(document);
        ArgumentCaptor<AssetHistoryEvent> captor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(captor.capture());
        AssetHistoryEvent event = captor.getValue();
        assertThat(event.getEventType()).isEqualTo(AssetHistoryEventType.OPERATIONAL_DOCUMENT_DELETED);
        assertThat(event.getAsset().getId()).isEqualTo(5L);
        assertThat(event.getPerformedByUserId()).isEqualTo(30L);
        assertThat(event.getEventDate()).isEqualTo(LocalDate.now());
    }

    @Test
    void deleteDocument_shouldRejectCrossDepartmentUser() {
        Asset asset = asset(5L);
        User coordinator = userInDepartment(10L, UserRole.OPERATIONAL_COORDINATOR, 2L);
        OperationalDocument document = savedDocument(103L, asset);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(operationalDocumentRepository.findById(103L)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> operationalDocumentService.deleteDocument(103L, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
        verify(operationalDocumentRepository, never()).delete(any());
        verify(fileStore, never()).delete(any());
    }

    @Test
    void deleteDocument_shouldRejectNonexistentDocument() {
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);
        when(operationalDocumentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationalDocumentService.deleteDocument(999L, 30L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Document not found");
    }

    @Test
    void deleteDocument_shouldRejectUnauthorizedUser() {
        Asset asset = asset(5L);
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        OperationalDocument document = savedDocument(103L, asset);

        when(userService.getById(1L)).thenReturn(administrator);
        when(operationalDocumentRepository.findById(103L)).thenReturn(Optional.of(document));

        assertThatThrownBy(() -> operationalDocumentService.deleteDocument(103L, 1L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void downloadDocument_shouldAllowAdministratorEvenWhenUploadIsForbidden() {
        Asset asset = asset(5L);
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        OperationalDocument document = savedDocument(103L, asset);
        Resource resource = mock(Resource.class);

        when(userService.getById(1L)).thenReturn(administrator);
        when(operationalDocumentRepository.findById(103L)).thenReturn(Optional.of(document));
        when(fileStore.loadAsResource("/tmp/stored-file")).thenReturn(resource);

        OperationalDocumentService.OperationalDocumentDownload download =
                operationalDocumentService.downloadDocument(103L, 1L);

        assertThat(download.resource()).isSameAs(resource);
        assertThat(download.originalFileName()).isEqualTo("report.pdf");
        verify(fileStore).loadAsResource("/tmp/stored-file");
    }

    @Test
    void downloadDocument_shouldAllowManagerForOwnDepartmentDocument() {
        Asset asset = asset(5L);
        User manager = user(30L, UserRole.MANAGER);
        OperationalDocument document = savedDocument(103L, asset);
        Resource resource = mock(Resource.class);

        when(userService.getById(30L)).thenReturn(manager);
        when(operationalDocumentRepository.findById(103L)).thenReturn(Optional.of(document));
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);
        when(fileStore.loadAsResource("/tmp/stored-file")).thenReturn(resource);

        operationalDocumentService.downloadDocument(103L, 30L);

        verify(fileStore).loadAsResource("/tmp/stored-file");
    }

    @Test
    void downloadDocument_shouldRejectManagerForCrossDepartmentDocumentWithoutLoadingStorage() {
        Asset asset = asset(5L);
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        OperationalDocument document = savedDocument(103L, asset);

        when(userService.getById(30L)).thenReturn(manager);
        when(operationalDocumentRepository.findById(103L)).thenReturn(Optional.of(document));
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> operationalDocumentService.downloadDocument(103L, 30L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only download operational documents for assets in your own department.");

        verify(fileStore, never()).loadAsResource(any());
    }

    @Test
    void downloadDocument_shouldAllowAssignedFieldEmployeeForInspectionDocument() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        OperationalDocument document = inspectionDocument(104L, asset, 100L);
        Resource resource = mock(Resource.class);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(operationalDocumentRepository.findById(104L)).thenReturn(Optional.of(document));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(fileStore.loadAsResource("/tmp/stored-file")).thenReturn(resource);

        operationalDocumentService.downloadDocument(104L, 20L);

        verify(fileStore).loadAsResource("/tmp/stored-file");
    }

    @Test
    void downloadDocument_shouldRejectUnassignedFieldEmployeeWithoutLoadingStorage() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(100L, asset, 99L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        OperationalDocument document = inspectionDocument(104L, asset, 100L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(operationalDocumentRepository.findById(104L)).thenReturn(Optional.of(document));
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> operationalDocumentService.downloadDocument(104L, 20L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Unauthorized to download operational evidence for this context");

        verify(fileStore, never()).loadAsResource(any());
    }

    @Test
    void downloadDocument_shouldAllowAssignedContractorForWorkOrderDocument() {
        Asset asset = asset(5L);
        User contractor = user(25L, UserRole.CONTRACTOR);
        WorkOrder workOrder = assignedWorkOrder(1000L, asset, 25L);
        OperationalDocument document = workOrderDocument(105L, asset, 1000L);
        Resource resource = mock(Resource.class);

        when(userService.getById(25L)).thenReturn(contractor);
        when(operationalDocumentRepository.findById(105L)).thenReturn(Optional.of(document));
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(fileStore.loadAsResource("/tmp/stored-file")).thenReturn(resource);

        operationalDocumentService.downloadDocument(105L, 25L);

        verify(fileStore).loadAsResource("/tmp/stored-file");
    }

    @Test
    void downloadDocument_shouldRejectNonexistentDocument() {
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);
        when(operationalDocumentRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationalDocumentService.downloadDocument(999L, 30L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Document not found");

        verify(fileStore, never()).loadAsResource(any());
    }

    @Test
    void deleteDocument_shouldPreventDownloadAfterDeletion() {
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);
        when(operationalDocumentRepository.findById(103L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationalDocumentService.downloadDocument(103L, 30L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Document not found");
    }

    private OperationalDocument savedDocument(Long id, Asset asset) {
        OperationalDocument document = new OperationalDocument(
                asset,
                OperationalDocumentOwnerType.ASSET,
                asset.getId(),
                OperationalDocumentType.REPORT,
                "report.pdf",
                "stored-file",
                "application/pdf",
                1024L,
                "/tmp/stored-file",
                null,
                30L,
                LocalDateTime.of(2026, 6, 20, 10, 0));
        document.setId(id);
        return document;
    }

    private OperationalDocument inspectionDocument(Long id, Asset asset, Long inspectionId) {
        OperationalDocument document = new OperationalDocument(
                asset,
                OperationalDocumentOwnerType.INSPECTION,
                inspectionId,
                OperationalDocumentType.PHOTO,
                "photo.jpg",
                "stored-file",
                "image/jpeg",
                1024L,
                "/tmp/stored-file",
                null,
                20L,
                LocalDateTime.of(2026, 6, 20, 10, 0));
        document.setId(id);
        return document;
    }

    private OperationalDocument workOrderDocument(Long id, Asset asset, Long workOrderId) {
        OperationalDocument document = new OperationalDocument(
                asset,
                OperationalDocumentOwnerType.WORK_ORDER,
                workOrderId,
                OperationalDocumentType.REPORT,
                "report.pdf",
                "stored-file",
                "application/pdf",
                1024L,
                "/tmp/stored-file",
                null,
                25L,
                LocalDateTime.of(2026, 6, 20, 10, 0));
        document.setId(id);
        return document;
    }

    private WorkOrder assignedWorkOrder(Long id, Asset asset, Long assigneeId) {
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
                WorkType.CONTRACTOR_WORK,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusHours(1));
        workOrder.setId(id);
        workOrder.assign(assigneeId, 10L, LocalDateTime.now().minusMinutes(30));
        return workOrder;
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
        Department department = new Department("Parks");
        department.setId(1L);
        user.setDepartment(department);
        return user;
    }

    private User userInDepartment(Long id, UserRole role, Long departmentId) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        user.setDepartment(department);
        return user;
    }
}
