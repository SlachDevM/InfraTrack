package com.infratrack.reporting;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.department.Department;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportingAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private ReportingAuthorizationService authorizationService;

    @Test
    void resolveScope_administrator_returnsGlobalScope() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR, null));

        ExportScope scope = authorizationService.resolveScope(1L);

        assertThat(scope.isGlobal()).isTrue();
    }

    @Test
    void resolveScope_manager_returnsDepartmentScope() {
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER, 3L));

        ExportScope scope = authorizationService.resolveScope(2L);

        assertThat(scope.departmentId()).isEqualTo(3L);
    }

    @Test
    void resolveScope_coordinator_returnsDepartmentScope() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.OPERATIONAL_COORDINATOR, 5L));

        ExportScope scope = authorizationService.resolveScope(4L);

        assertThat(scope.departmentId()).isEqualTo(5L);
    }

    @Test
    void resolveScope_fieldEmployee_isForbidden() {
        when(userService.getById(20L)).thenReturn(user(20L, UserRole.FIELD_EMPLOYEE, 3L));

        assertThatThrownBy(() -> authorizationService.resolveScope(20L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void resolveScope_contractor_isForbidden() {
        when(userService.getById(21L)).thenReturn(user(21L, UserRole.CONTRACTOR, 3L));

        assertThatThrownBy(() -> authorizationService.resolveScope(21L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private static User user(Long id, UserRole role, Long departmentId) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        if (departmentId != null) {
            Department department = new Department("Parks");
            department.setId(departmentId);
            user.setDepartment(department);
        }
        return user;
    }
}
