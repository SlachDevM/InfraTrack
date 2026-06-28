package com.infratrack.completionreview;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
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
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompletionReviewAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

    @InjectMocks
    private CompletionReviewAuthorizationService authorizationService;

    @Test
    void requireManager_shouldAllowManager() {
        User manager = manager(30L, 1L);
        when(userService.getById(30L)).thenReturn(manager);

        assertThatCode(() -> authorizationService.requireManager(30L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireManager_shouldRejectOperationalCoordinator() {
        User coordinator = user(30L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(30L)).thenReturn(coordinator);

        assertThatThrownBy(() -> authorizationService.requireManager(30L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only managers can record completion reviews");
    }

    @Test
    void requireManagerAuthorizedForAsset_shouldAllowOwnDepartment() {
        User manager = manager(30L, 1L);
        Asset asset = assetInDepartment(1L);
        LocalDateTime reviewedAt = LocalDateTime.now().minusMinutes(1);

        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                manager, asset.getDepartment(), reviewedAt))
                .thenReturn(true);

        assertThatCode(() -> authorizationService.requireManagerAuthorizedForAsset(
                manager, asset, reviewedAt))
                .doesNotThrowAnyException();
    }

    @Test
    void requireManagerAuthorizedForAsset_shouldRejectCrossDepartmentWithoutDelegation() {
        User manager = manager(30L, 2L);
        Asset asset = assetInDepartment(1L);
        LocalDateTime reviewedAt = LocalDateTime.now().minusMinutes(1);

        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                manager, asset.getDepartment(), reviewedAt))
                .thenReturn(false);

        assertThatThrownBy(() -> authorizationService.requireManagerAuthorizedForAsset(
                manager, asset, reviewedAt))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only record completion reviews for assets in your own department.");
    }

    @Test
    void requireManagerAuthorizedForAsset_shouldAllowActiveDelegation() {
        User manager = manager(30L, 2L);
        Asset asset = assetInDepartment(1L);
        LocalDateTime reviewedAt = LocalDateTime.now().minusMinutes(1);

        when(delegatedAuthorityService.canManagerActForAssetDepartment(
                manager, asset.getDepartment(), reviewedAt))
                .thenReturn(true);

        assertThatCode(() -> authorizationService.requireManagerAuthorizedForAsset(
                manager, asset, reviewedAt))
                .doesNotThrowAnyException();

        verify(delegatedAuthorityService).canManagerActForAssetDepartment(
                manager, asset.getDepartment(), reviewedAt);
    }

    private User manager(Long id, Long departmentId) {
        User manager = user(id, UserRole.MANAGER);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        manager.setDepartment(department);
        return manager;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@example.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }

    private Asset assetInDepartment(Long departmentId) {
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Park Swing",
                department,
                category,
                "Central Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L
        );
        asset.setId(5L);
        return asset;
    }
}
