package com.infratrack.maintenanceactivity;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MaintenanceActivityAuthorizationServiceTest {

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

    private MaintenanceActivityAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new MaintenanceActivityAuthorizationService(delegatedAuthorityService);
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldAllowAdministrator() {
        User administrator = user(1L, UserRole.ADMINISTRATOR, null);

        assertThatCode(() -> authorizationService.requireCanViewMaintenanceActivity(
                administrator, maintenanceActivity(5000L, 1L, 20L)))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldAllowManagerForOwnDepartment() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 1L);
        MaintenanceActivity activity = maintenanceActivity(5000L, 1L, 20L);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(activity.getAsset().getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatCode(() -> authorizationService.requireCanViewMaintenanceActivity(manager, activity))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldAllowManagerForDelegatedDepartment() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        MaintenanceActivity activity = maintenanceActivity(5000L, 1L, 20L);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(activity.getAsset().getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatCode(() -> authorizationService.requireCanViewMaintenanceActivity(manager, activity))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldRejectManagerForCrossDepartmentWithoutDelegation() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        MaintenanceActivity activity = maintenanceActivity(5000L, 1L, 20L);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(activity.getAsset().getDepartment()), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> authorizationService.requireCanViewMaintenanceActivity(manager, activity))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view maintenance activities for assets in your own department.");
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldAllowOperationalCoordinatorForOwnDepartment() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 1L);

        assertThatCode(() -> authorizationService.requireCanViewMaintenanceActivity(
                coordinator, maintenanceActivity(5000L, 1L, 20L)))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldRejectOperationalCoordinatorForCrossDepartment() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 2L);

        assertThatThrownBy(() -> authorizationService.requireCanViewMaintenanceActivity(
                coordinator, maintenanceActivity(5000L, 1L, 20L)))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view maintenance activities for assets in your own department.");
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldAllowAssignedFieldEmployee() {
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);

        assertThatCode(() -> authorizationService.requireCanViewMaintenanceActivity(
                fieldEmployee, maintenanceActivity(5000L, 1L, 20L)))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldAllowFieldEmployeeWhoPerformedMaintenance() {
        User fieldEmployee = userInDepartment(21L, UserRole.FIELD_EMPLOYEE, 1L);
        MaintenanceActivity activity = maintenanceActivity(5000L, 1L, 21L);
        activity.getWorkOrder().assign(99L, 40L, LocalDateTime.now().minusHours(2));

        assertThatCode(() -> authorizationService.requireCanViewMaintenanceActivity(fieldEmployee, activity))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldRejectUnassignedFieldEmployee() {
        User fieldEmployee = userInDepartment(21L, UserRole.FIELD_EMPLOYEE, 1L);

        assertThatThrownBy(() -> authorizationService.requireCanViewMaintenanceActivity(
                fieldEmployee, maintenanceActivity(5000L, 1L, 20L)))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view maintenance activities for work orders assigned to you.");
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldAllowAssignedContractor() {
        User contractor = userInDepartment(25L, UserRole.CONTRACTOR, 1L);
        MaintenanceActivity activity = maintenanceActivity(5000L, 1L, 25L);
        activity.getWorkOrder().assign(25L, 40L, LocalDateTime.now().minusHours(2));

        assertThatCode(() -> authorizationService.requireCanViewMaintenanceActivity(contractor, activity))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewMaintenanceActivity_shouldRejectCrossDepartmentContractor() {
        User contractor = userInDepartment(25L, UserRole.CONTRACTOR, 2L);

        assertThatThrownBy(() -> authorizationService.requireCanViewMaintenanceActivity(
                contractor, maintenanceActivity(5000L, 1L, 20L)))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view maintenance activities for work orders assigned to you.");
    }

    private MaintenanceActivity maintenanceActivity(Long id, Long departmentId, Long assignedToUserId) {
        Asset asset = asset(departmentId);
        WorkOrder workOrder = workOrder(asset, assignedToUserId);
        MaintenanceActivity activity = new MaintenanceActivity(
                workOrder,
                asset,
                assignedToUserId,
                "Completed repair",
                LocalDateTime.now().minusHours(1));
        activity.setId(id);
        return activity;
    }

    private WorkOrder workOrder(Asset asset, Long assignedToUserId) {
        Issue issue = issue(asset);
        OperationalDecision decision = new OperationalDecision(
                issue,
                asset,
                OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                "Decision rationale",
                30L,
                LocalDateTime.now().minusHours(2));
        WorkOrder workOrder = new WorkOrder(
                decision,
                asset,
                WorkType.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusHours(2));
        workOrder.setId(1000L);
        workOrder.assign(assignedToUserId, 40L, LocalDateTime.now().minusHours(1));
        workOrder.complete();
        return workOrder;
    }

    private Issue issue(Asset asset) {
        Issue issue = new Issue(
                null,
                asset,
                "Broken swing chain",
                IssueSeverity.HIGH,
                20L,
                LocalDateTime.now().minusHours(3));
        issue.setId(500L);
        return issue;
    }

    private Asset asset(Long departmentId) {
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 1),
                10L);
        asset.setId(5L);
        return asset;
    }

    private User user(Long id, UserRole role, Long departmentId) {
        User user = new User("user" + id + "@test.com", "password", "User " + id, role);
        user.setId(id);
        user.setEnabled(true);
        if (departmentId != null) {
            Department department = new Department("Department " + departmentId);
            department.setId(departmentId);
            user.setDepartment(department);
        }
        return user;
    }

    private User userInDepartment(Long id, UserRole role, Long departmentId) {
        return user(id, role, departmentId);
    }
}
