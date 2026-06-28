package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalDocumentOwnerLookupServiceTest {

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
    private OperationalDocumentAuthorizationService authorizationService;
    @Mock
    private UserService userService;

    private OperationalDocumentOwnerLookupService lookupService;

    @BeforeEach
    void setUp() {
        lookupService = new OperationalDocumentOwnerLookupService(
                assetRepository,
                inspectionRepository,
                issueRepository,
                operationalDecisionRepository,
                workOrderRepository,
                maintenanceActivityRepository,
                completionReviewRepository,
                authorizationService,
                userService);
    }

    @Test
    void listEligibleOwners_shouldReturnInspectionsForAsset() {
        Asset asset = asset(5L);
        User coordinator = user(10L);
        Inspection inspection = inspection(100L, asset);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        when(inspectionRepository.findAllByAsset_IdOrderByCompletedAtDesc(5L))
                .thenReturn(List.of(inspection));

        var owners = lookupService.listEligibleOwners(
                5L, OperationalDocumentOwnerType.INSPECTION, 10L);

        assertThat(owners).hasSize(1);
        assertThat(owners.get(0).getId()).isEqualTo(100L);
        assertThat(owners.get(0).getLabel()).isEqualTo("Inspection #100");
        verify(authorizationService).requireAssetDepartmentAuthorized(coordinator, asset);
    }

    @Test
    void listEligibleOwners_shouldRejectCrossDepartmentAsset() {
        Asset asset = asset(5L);
        User coordinator = user(10L);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(assetRepository.findById(5L)).thenReturn(Optional.of(asset));
        doThrow(new ForbiddenOperationException(
                "You may only upload operational documents for assets in your own department."))
                .when(authorizationService).requireAssetDepartmentAuthorized(any(), any());

        assertThatThrownBy(() -> lookupService.listEligibleOwners(
                5L, OperationalDocumentOwnerType.INSPECTION, 10L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(inspectionRepository, org.mockito.Mockito.never()).findAllByAsset_IdOrderByCompletedAtDesc(any());
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
                LocalDate.of(2026, 1, 1),
                1L);
        asset.setId(id);
        return asset;
    }

    private Inspection inspection(Long id, Asset asset) {
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
                20L,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7));
        inspection.setId(id);
        return inspection;
    }

    private User user(Long id) {
        User user = new User("user@test.com", "password", "User", UserRole.OPERATIONAL_COORDINATOR);
        user.setId(id);
        user.setEnabled(true);
        Department department = new Department("Parks");
        department.setId(1L);
        user.setDepartment(department);
        return user;
    }
}
