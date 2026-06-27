package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InspectionAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private InspectionAuthorizationService authorizationService;

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
