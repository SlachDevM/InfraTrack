package com.infratrack.asset;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
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
class AssetAuthorizationServiceTest {

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

    private AssetAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new AssetAuthorizationService(delegatedAuthorityService);
    }

    @Test
    void requireCanViewAsset_shouldAllowAdministrator() {
        User administrator = user(1L, UserRole.ADMINISTRATOR, null);

        assertThatCode(() -> authorizationService.requireCanViewAsset(administrator, asset(5L, 1L)))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewAsset_shouldAllowManagerForOwnDepartment() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 1L);
        Asset asset = asset(5L, 1L);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatCode(() -> authorizationService.requireCanViewAsset(manager, asset))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewAsset_shouldAllowManagerForDelegatedDepartment() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        Asset asset = asset(5L, 1L);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(true);

        assertThatCode(() -> authorizationService.requireCanViewAsset(manager, asset))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewAsset_shouldRejectManagerForCrossDepartmentWithoutDelegation() {
        User manager = userInDepartment(30L, UserRole.MANAGER, 2L);
        Asset asset = asset(5L, 1L);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                eq(manager), eq(asset.getDepartment()), any(LocalDateTime.class)))
                .thenReturn(false);

        assertThatThrownBy(() -> authorizationService.requireCanViewAsset(manager, asset))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view asset history for assets in your own department.");
    }

    @Test
    void requireCanViewAsset_shouldAllowOperationalCoordinatorForOwnDepartment() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 1L);

        assertThatCode(() -> authorizationService.requireCanViewAsset(coordinator, asset(5L, 1L)))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewAsset_shouldRejectOperationalCoordinatorForCrossDepartment() {
        User coordinator = userInDepartment(40L, UserRole.OPERATIONAL_COORDINATOR, 2L);

        assertThatThrownBy(() -> authorizationService.requireCanViewAsset(coordinator, asset(5L, 1L)))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view asset history for assets in your own department.");
    }

    @Test
    void requireCanViewAsset_shouldAllowFieldEmployeeForOwnDepartment() {
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);

        assertThatCode(() -> authorizationService.requireCanViewAsset(fieldEmployee, asset(5L, 1L)))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewAsset_shouldRejectFieldEmployeeForCrossDepartment() {
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 2L);

        assertThatThrownBy(() -> authorizationService.requireCanViewAsset(fieldEmployee, asset(5L, 1L)))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view asset history for assets in your own department.");
    }

    @Test
    void requireCanViewAsset_shouldAllowContractorForOwnDepartment() {
        User contractor = userInDepartment(25L, UserRole.CONTRACTOR, 1L);

        assertThatCode(() -> authorizationService.requireCanViewAsset(contractor, asset(5L, 1L)))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewAsset_shouldRejectContractorForCrossDepartment() {
        User contractor = userInDepartment(25L, UserRole.CONTRACTOR, 2L);

        assertThatThrownBy(() -> authorizationService.requireCanViewAsset(contractor, asset(5L, 1L)))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only view asset history for assets in your own department.");
    }

    private Asset asset(Long id, Long departmentId) {
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
        asset.setId(id);
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
