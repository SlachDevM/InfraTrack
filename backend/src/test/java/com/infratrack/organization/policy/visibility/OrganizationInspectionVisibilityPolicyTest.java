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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrganizationInspectionVisibilityPolicyTest {

    private final OrganizationInspectionVisibilityPolicy policy = new OrganizationInspectionVisibilityPolicy();

    @Test
    void requireCanView_shouldAllowAdministrator_crossDepartment() {
        Inspection inspection = inspection(asset(5L));
        User administrator = user(1L, UserRole.ADMINISTRATOR, null);

        assertThatCode(() -> policy.requireCanView(administrator, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanView_shouldAllowManager_crossDepartment() {
        Inspection inspection = inspection(asset(5L));
        Department other = new Department("Water");
        other.setId(99L);
        User manager = user(30L, UserRole.MANAGER, other);

        assertThatCode(() -> policy.requireCanView(manager, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanView_shouldAllowOperationalCoordinator_crossDepartment() {
        Inspection inspection = inspection(asset(5L));
        Department other = new Department("Water");
        other.setId(99L);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR, other);

        assertThatCode(() -> policy.requireCanView(coordinator, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanView_shouldAllowFieldEmployee_crossDepartment() {
        Inspection inspection = inspection(asset(5L));
        Department other = new Department("Water");
        other.setId(99L);
        User field = user(20L, UserRole.FIELD_EMPLOYEE, other);

        assertThatCode(() -> policy.requireCanView(field, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanView_shouldAllowContractor_crossDepartment() {
        Inspection inspection = inspection(asset(5L));
        Department other = new Department("Water");
        other.setId(99L);
        User contractor = user(25L, UserRole.CONTRACTOR, other);

        assertThatCode(() -> policy.requireCanView(contractor, inspection))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanView_shouldRejectNullRole() {
        Inspection inspection = inspection(asset(5L));
        User user = user(25L, null, null);

        assertThatThrownBy(() -> policy.requireCanView(user, inspection))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view inspections for assets in your own department.");
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

