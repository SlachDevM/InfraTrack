package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.organization.policy.visibility.InspectionVisibilityPolicyService;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectionAuthorizationServiceTest {

    @Mock
    private UserService userService;

    private InspectionAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new InspectionAuthorizationService(
                userService,
                new InspectionVisibilityPolicyService("DEPARTMENT"));
    }

    @Test
    void requireCanAssignInspections_shouldRejectManager() {
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);

        assertThatThrownBy(() -> authorizationService.requireCanAssignInspections(30L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only operational coordinators can assign inspections");
    }

    @Test
    void requireAssignedPerformer_shouldReturnAssignedUser() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        User result = authorizationService.requireAssignedPerformer(20L, inspection);

        assertThat(result).isEqualTo(fieldEmployee);
    }

    @Test
    void requireAssignedPerformer_shouldRejectUnassignedUser() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User otherUser = user(99L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(99L)).thenReturn(otherUser);

        assertThatThrownBy(() -> authorizationService.requireAssignedPerformer(99L, inspection))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only the assigned user can perform this inspection");
    }

    @Test
    void requireCanViewInspection_shouldAllowSameDepartmentUser() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        coordinator.setDepartment(asset.getDepartment());

        assertThatCode(() -> authorizationService.requireCanViewInspection(coordinator, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewInspection_shouldRejectCrossDepartmentUser() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User otherDepartmentUser = user(30L, UserRole.OPERATIONAL_COORDINATOR);
        Department otherDepartment = new Department("Water");
        otherDepartment.setId(99L);
        otherDepartmentUser.setDepartment(otherDepartment);

        assertThatThrownBy(() -> authorizationService.requireCanViewInspection(otherDepartmentUser, inspection))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view inspections for assets in your own department.");
    }

    @Test
    void requireCanViewInspection_shouldAllowCrossDepartmentUser_whenOrganizationModeConfigured() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);

        User otherDepartmentUser = user(30L, UserRole.OPERATIONAL_COORDINATOR);
        Department otherDepartment = new Department("Water");
        otherDepartment.setId(99L);
        otherDepartmentUser.setDepartment(otherDepartment);

        InspectionAuthorizationService orgModeService = new InspectionAuthorizationService(
                userService,
                new InspectionVisibilityPolicyService("ORGANIZATION"));

        assertThatCode(() -> orgModeService.requireCanViewInspection(otherDepartmentUser, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanSaveInspectionAnswers_shouldStillRejectCrossDepartmentManager_whenOrganizationModeConfigured() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);

        User manager = user(30L, UserRole.MANAGER);
        Department otherDepartment = new Department("Water");
        otherDepartment.setId(99L);
        manager.setDepartment(otherDepartment);

        InspectionAuthorizationService orgModeService = new InspectionAuthorizationService(
                userService,
                new InspectionVisibilityPolicyService("ORGANIZATION"));

        assertThatThrownBy(() -> orgModeService.requireCanSaveInspectionAnswers(manager, inspection))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view inspections for assets in your own department.");
    }

    @Test
    void requireCanViewInspection_shouldAllowAdministrator() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User administrator = user(1L, UserRole.ADMINISTRATOR);

        assertThatCode(() -> authorizationService.requireCanViewInspection(administrator, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanSaveInspectionAnswers_shouldAllowAssignedFieldEmployee() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        assertThatCode(() -> authorizationService.requireCanSaveInspectionAnswers(fieldEmployee, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanSaveInspectionAnswers_shouldAllowAdministrator() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User administrator = user(1L, UserRole.ADMINISTRATOR);

        assertThatCode(() -> authorizationService.requireCanSaveInspectionAnswers(administrator, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanSaveInspectionAnswers_shouldAllowManagerInSameDepartment() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User manager = user(30L, UserRole.MANAGER);
        manager.setDepartment(asset.getDepartment());

        assertThatCode(() -> authorizationService.requireCanSaveInspectionAnswers(manager, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanSaveInspectionAnswers_shouldRejectUnassignedFieldEmployee() {
        Asset asset = asset(5L);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        User otherFieldEmployee = user(99L, UserRole.FIELD_EMPLOYEE);

        assertThatThrownBy(() -> authorizationService.requireCanSaveInspectionAnswers(otherFieldEmployee, inspection))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only the assigned user can save inspection answers");
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
        if (role != UserRole.ADMINISTRATOR) {
            Department department = new Department("Parks");
            department.setId(1L);
            user.setDepartment(department);
        }
        return user;
    }
}
