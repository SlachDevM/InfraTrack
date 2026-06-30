package com.infratrack.operationsintelligence;

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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsKpiAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private OperationsKpiAuthorizationService authorizationService;

    @Test
    void resolveScope_shouldReturnGlobalScopeForAdministrator() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR, null));

        OperationsKpiScope scope = authorizationService.resolveScope(1L);

        assertThat(scope.isGlobal()).isTrue();
        assertThat(scope.departmentId()).isNull();
    }

    @Test
    void resolveScope_shouldReturnDepartmentScopeForManager() {
        Department department = department(10L);
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER, department));

        OperationsKpiScope scope = authorizationService.resolveScope(2L);

        assertThat(scope.isGlobal()).isFalse();
        assertThat(scope.departmentId()).isEqualTo(10L);
    }

    @Test
    void resolveScope_shouldReturnDepartmentScopeForOperationalCoordinator() {
        Department department = department(20L);
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR, department));

        OperationsKpiScope scope = authorizationService.resolveScope(3L);

        assertThat(scope.departmentId()).isEqualTo(20L);
    }

    @Test
    void resolveScope_shouldRejectFieldEmployee() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE, department(10L)));

        assertThatThrownBy(() -> authorizationService.resolveScope(4L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to view operational KPIs.");
    }

    @Test
    void resolveScope_shouldRejectContractor() {
        when(userService.getById(5L)).thenReturn(user(5L, UserRole.CONTRACTOR, department(10L)));

        assertThatThrownBy(() -> authorizationService.resolveScope(5L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void resolveScope_shouldRejectManagerWithoutDepartment() {
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER, null));

        assertThatThrownBy(() -> authorizationService.resolveScope(2L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You must belong to a department to view operational KPIs.");
    }

    @Test
    void requireCanViewKpis_shouldAllowAuthorizedRoles() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR, null));

        assertThatCode(() -> authorizationService.requireCanViewKpis(1L))
                .doesNotThrowAnyException();
    }

    private static User user(Long id, UserRole role, Department department) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setDepartment(department);
        return user;
    }

    private static Department department(Long id) {
        Department department = new Department("Operations");
        department.setId(id);
        return department;
    }
}
