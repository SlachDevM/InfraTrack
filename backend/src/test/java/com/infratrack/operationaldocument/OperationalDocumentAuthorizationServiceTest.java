package com.infratrack.operationaldocument;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationalDocumentAuthorizationServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private MaintenanceActivityRepository maintenanceActivityRepository;

    private OperationalDocumentAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new OperationalDocumentAuthorizationService(
                inspectionRepository,
                issueRepository,
                workOrderRepository,
                maintenanceActivityRepository);
    }

    @Test
    void requireUploadAuthorized_shouldDenyAdministrator() {
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        OperationalDocumentOwnerContext context = ownerContext(asset(5L));

        assertThatThrownBy(() -> authorizationService.requireUploadAuthorized(administrator, context))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Administrators cannot upload operational evidence");
    }

    @Test
    void requireUploadAuthorized_shouldAllowManagerForAnyContext() {
        User manager = user(30L, UserRole.MANAGER);
        OperationalDocumentOwnerContext context = ownerContext(asset(5L));

        assertThatCode(() -> authorizationService.requireUploadAuthorized(manager, context))
                .doesNotThrowAnyException();
    }

    @Test
    void requireUploadAuthorized_shouldAllowAssignedFieldEmployeeForInspection() {
        Asset asset = asset(5L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        inspection.setId(100L);
        OperationalDocumentOwnerContext context = new OperationalDocumentOwnerContext(
                asset, OperationalDocumentOwnerType.INSPECTION, 100L);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatCode(() -> authorizationService.requireUploadAuthorized(fieldEmployee, context))
                .doesNotThrowAnyException();
    }

    @Test
    void requireUploadAuthorized_shouldDenyFieldEmployeeForAssetLevelUpload() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        OperationalDocumentOwnerContext context = ownerContext(asset(5L));

        assertThatThrownBy(() -> authorizationService.requireUploadAuthorized(fieldEmployee, context))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Unauthorized to upload operational evidence for this context");
    }

    private OperationalDocumentOwnerContext ownerContext(Asset asset) {
        return new OperationalDocumentOwnerContext(asset, OperationalDocumentOwnerType.ASSET, asset.getId());
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

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
