package com.infratrack.preventivemaintenance;

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
class PreventiveSchedulerAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PreventiveSchedulerAuthorizationService authorizationService;

    @Test
    void requireCanRunScheduler_shouldAllowAdministratorAndManager() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));

        assertThatCode(() -> authorizationService.requireCanRunScheduler(1L)).doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanRunScheduler(2L)).doesNotThrowAnyException();
    }

    @Test
    void requireCanRunScheduler_shouldRejectCoordinator() {
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatThrownBy(() -> authorizationService.requireCanRunScheduler(3L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanRunScheduler_shouldRejectFieldEmployee() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> authorizationService.requireCanRunScheduler(4L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanViewSchedulerRuns_shouldAllowAdministratorManagerAndCoordinator() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatCode(() -> authorizationService.requireCanViewSchedulerRuns(1L)).doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanViewSchedulerRuns(2L)).doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanViewSchedulerRuns(3L)).doesNotThrowAnyException();
    }

    @Test
    void resolveDepartmentScopeForManualRun_shouldReturnNullForAdministrator() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));

        assertThat(authorizationService.resolveDepartmentScopeForManualRun(1L)).isNull();
    }

    @Test
    void resolveDepartmentScopeForManualRun_shouldReturnManagerDepartment() {
        com.infratrack.department.Department department = new com.infratrack.department.Department("Water");
        department.setId(10L);
        User manager = user(2L, UserRole.MANAGER);
        manager.setDepartment(department);
        when(userService.getById(2L)).thenReturn(manager);

        assertThat(authorizationService.resolveDepartmentScopeForManualRun(2L)).isEqualTo(10L);
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
