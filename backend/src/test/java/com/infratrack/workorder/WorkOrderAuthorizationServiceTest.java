package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

    @InjectMocks
    private WorkOrderAuthorizationService authorizationService;

    @Test
    void requireOperationalCoordinator_shouldReturnCoordinator() {
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(40L)).thenReturn(coordinator);

        User result = authorizationService.requireOperationalCoordinator(40L);

        assertThat(result).isEqualTo(coordinator);
    }

    @Test
    void requireOperationalCoordinator_shouldRejectFieldEmployee() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> authorizationService.requireOperationalCoordinator(20L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only operational coordinators can create work orders");
    }

    @Test
    void requireOperationalCoordinatorForAssignment_shouldRejectManager() {
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);

        assertThatThrownBy(() -> authorizationService.requireOperationalCoordinatorForAssignment(30L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only operational coordinators can assign work orders");
    }

    @Test
    void requireCoordinatorOwnDepartment_shouldAllowMatchingDepartment() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 1L);
        Asset asset = assetInDepartment(1L);

        authorizationService.requireCoordinatorOwnDepartment(coordinator, asset);
    }

    @Test
    void requireCoordinatorOwnDepartment_shouldRejectCrossDepartment() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 2L);
        Asset asset = assetInDepartment(1L);

        assertThatThrownBy(() -> authorizationService.requireCoordinatorOwnDepartment(coordinator, asset))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireEligibleAssignee_shouldAcceptActiveFieldEmployeeInAssetDepartment() {
        Asset asset = assetInDepartment(1L);
        User assignee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);
        when(userService.getById(20L)).thenReturn(assignee);

        User result = authorizationService.requireEligibleAssignee(
                20L, WorkType.INTERNAL_MAINTENANCE, asset);

        assertThat(result).isEqualTo(assignee);
    }

    @Test
    void requireEligibleAssignee_shouldAcceptActiveContractorForContractorWork() {
        Asset asset = assetInDepartment(1L);
        User assignee = userInDepartment(25L, UserRole.CONTRACTOR, 1L);
        when(userService.getById(25L)).thenReturn(assignee);

        User result = authorizationService.requireEligibleAssignee(
                25L, WorkType.CONTRACTOR_WORK, asset);

        assertThat(result).isEqualTo(assignee);
    }

    @Test
    void requireEligibleAssignee_shouldRejectDisabledWorker() {
        Asset asset = assetInDepartment(1L);
        User assignee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);
        assignee.setEnabled(false);
        when(userService.getById(20L)).thenReturn(assignee);

        assertThatThrownBy(() -> authorizationService.requireEligibleAssignee(
                20L, WorkType.INTERNAL_MAINTENANCE, asset))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireEligibleAssignee_shouldRejectCrossDepartmentWorker() {
        Asset asset = assetInDepartment(1L);
        User assignee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 2L);
        when(userService.getById(20L)).thenReturn(assignee);

        assertThatThrownBy(() -> authorizationService.requireEligibleAssignee(
                20L, WorkType.INTERNAL_MAINTENANCE, asset))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireEligibleAssignee_shouldRejectContractorForInternalMaintenance() {
        Asset asset = assetInDepartment(1L);
        User assignee = userInDepartment(25L, UserRole.CONTRACTOR, 1L);
        when(userService.getById(25L)).thenReturn(assignee);

        assertThatThrownBy(() -> authorizationService.requireEligibleAssignee(
                25L, WorkType.INTERNAL_MAINTENANCE, asset))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void requireEligibleAssignee_shouldRejectManagerForInternalMaintenance() {
        Asset asset = assetInDepartment(1L);
        User assignee = userInDepartment(30L, UserRole.MANAGER, 1L);
        when(userService.getById(30L)).thenReturn(assignee);

        assertThatThrownBy(() -> authorizationService.requireEligibleAssignee(
                30L, WorkType.INTERNAL_MAINTENANCE, asset))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void requireEligibleAssignee_shouldRejectOperationalCoordinatorForInternalMaintenance() {
        Asset asset = assetInDepartment(1L);
        User assignee = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 1L);
        when(userService.getById(40L)).thenReturn(assignee);

        assertThatThrownBy(() -> authorizationService.requireEligibleAssignee(
                40L, WorkType.INTERNAL_MAINTENANCE, asset))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void requireCanViewWorkOrder_shouldAllowAdministrator() {
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        WorkOrder workOrder = workOrderInDepartment(100L, 20L, 1L);

        authorizationService.requireCanViewWorkOrder(administrator, workOrder);
    }

    @Test
    void requireCanViewWorkOrder_shouldAllowAssignedFieldEmployee() {
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);
        WorkOrder workOrder = workOrderInDepartment(100L, 20L, 1L);

        authorizationService.requireCanViewWorkOrder(fieldEmployee, workOrder);
    }

    @Test
    void requireCanViewWorkOrder_shouldRejectUnassignedFieldEmployee() {
        User fieldEmployee = userInDepartment(21L, UserRole.FIELD_EMPLOYEE, 1L);
        WorkOrder workOrder = workOrderInDepartment(100L, 20L, 1L);

        assertThatThrownBy(() -> authorizationService.requireCanViewWorkOrder(fieldEmployee, workOrder))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view work orders assigned to you.");
    }

    @Test
    void requireCanViewWorkOrder_shouldRejectCrossDepartmentCoordinator() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 2L);
        WorkOrder workOrder = workOrderInDepartment(100L, 20L, 1L);

        assertThatThrownBy(() -> authorizationService.requireCanViewWorkOrder(coordinator, workOrder))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanViewWorkOrder_shouldAllowManagerWithDelegation() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        WorkOrder workOrder = workOrderInDepartment(100L, 20L, 1L);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(workOrder.getAsset().getDepartment()), any(java.time.LocalDateTime.class)))
                .thenReturn(true);

        authorizationService.requireCanViewWorkOrder(manager, workOrder);
    }

    private WorkOrder workOrderInDepartment(Long id, Long assignedToUserId, Long departmentId) {
        WorkOrder workOrder = new WorkOrder(
                null,
                assetInDepartment(departmentId),
                WorkType.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                java.time.LocalDateTime.now().minusHours(1));
        workOrder.setId(id);
        if (assignedToUserId != null) {
            workOrder.assign(assignedToUserId, 40L, java.time.LocalDateTime.now().minusMinutes(30));
        }
        return workOrder;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }

    private User userInDepartment(Long id, UserRole role, Long departmentId) {
        User worker = user(id, role);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        worker.setDepartment(department);
        return worker;
    }

    private Asset assetInDepartment(Long departmentId) {
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        return new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L
        );
    }
}
