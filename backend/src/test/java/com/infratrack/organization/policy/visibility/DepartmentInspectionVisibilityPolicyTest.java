package com.infratrack.organization.policy.visibility;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DepartmentInspectionVisibilityPolicyTest {

    private final DepartmentInspectionVisibilityPolicy policy = new DepartmentInspectionVisibilityPolicy();

    @Test
    void requireCanView_shouldAllowAdministrator() {
        Inspection inspection = inspection(asset(5L));
        User administrator = user(1L, UserRole.ADMINISTRATOR, null);

        assertThatCode(() -> policy.requireCanView(administrator, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanView_shouldAllowSameDepartmentUser() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(asset);
        User manager = user(30L, UserRole.MANAGER, asset.getDepartment());

        assertThatCode(() -> policy.requireCanView(manager, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanView_shouldRejectCrossDepartmentUser() {
        Asset asset = asset(5L);
        Inspection inspection = inspection(asset);
        Department other = new Department("Water");
        other.setId(99L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR, other);

        assertThatThrownBy(() -> policy.requireCanView(coordinator, inspection))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view inspections for assets in your own department.");
    }

    @Test
    void resolveListScope_shouldReturnAllForAdministrator() {
        User administrator = user(1L, UserRole.ADMINISTRATOR, null);

        InspectionVisibilityScope scope = policy.resolveListScope(administrator);

        assertThat(scope.type()).isEqualTo(InspectionVisibilityScopeType.ALL);
    }

    @Test
    void resolveListScope_shouldReturnDepartmentForNonAdmin() {
        Department dept = new Department("Parks");
        dept.setId(1L);
        User manager = user(30L, UserRole.MANAGER, dept);

        InspectionVisibilityScope scope = policy.resolveListScope(manager);

        assertThat(scope.type()).isEqualTo(InspectionVisibilityScopeType.DEPARTMENT);
        assertThat(scope.departmentId()).isEqualTo(1L);
    }

    private Inspection inspection(Asset asset) {
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        inspection.setId(100L);
        return inspection;
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

    private User user(Long id, UserRole role, Department department) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        user.setDepartment(department);
        return user;
    }
}

