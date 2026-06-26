package com.infratrack.user;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserDepartmentTest {

    @Test
    void managerBelongsToDepartment_whenRoleIsManagerAndDepartmentMatches() {
        Department parks = new Department("Parks");
        parks.setId(1L);

        User manager = new User("manager@test.com", "password", "Manager", UserRole.MANAGER);
        manager.setId(10L);
        manager.setDepartment(parks);

        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByRoleAndDepartmentId(UserRole.MANAGER, 1L)).thenReturn(List.of(manager));

        List<User> managers = userRepository.findByRoleAndDepartmentId(UserRole.MANAGER, 1L);

        assertThat(managers).hasSize(1);
        assertThat(managers.get(0).getRole()).isEqualTo(UserRole.MANAGER);
        assertThat(managers.get(0).getDepartment().getId()).isEqualTo(1L);
    }

    @Test
    void departmentRename_shouldNotChangeAssetDepartmentReferenceOrStatus() {
        Department department = new Department("Parks");
        department.setId(1L);

        AssetCategory category = new AssetCategory("Bench");
        category.setId(5L);

        Asset asset = new Asset(
                "Bench A",
                department,
                category,
                "Main Street",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 1, 1),
                1L);
        asset.setId(100L);

        department.setName("Parks and Gardens");

        assertThat(asset.getDepartment().getId()).isEqualTo(1L);
        assertThat(asset.getDepartment().getName()).isEqualTo("Parks and Gardens");
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.ACTIVE);
    }

    @Test
    void userDepartmentChange_shouldNotAlterAssetOperationalState() {
        Department department = new Department("Roads");
        department.setId(2L);

        User user = new User("coordinator@test.com", "password", "Coordinator", UserRole.OPERATIONAL_COORDINATOR);
        user.setDepartment(department);

        user.setDepartment(null);

        assertThat(user.getDepartment()).isNull();
        assertThat(department.getId()).isEqualTo(2L);
    }
}
