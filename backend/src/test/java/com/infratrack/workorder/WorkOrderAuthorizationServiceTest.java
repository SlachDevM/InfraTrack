package com.infratrack.workorder;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private WorkOrderAuthorizationService authorizationService;

    @Test
    void requireOperationalCoordinator_shouldReturnCoordinator() {
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(40L)).thenReturn(coordinator);

        User result = authorizationService.requireOperationalCoordinator(40L);

        assertThat(result).isEqualTo(coordinator);
    }

    @Test
    void requireOperationalCoordinator_shouldRejectFieldEmployee() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> authorizationService.requireOperationalCoordinator(20L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only operational coordinators can create work orders");
    }

    @Test
    void requireOperationalCoordinatorForAssignment_shouldRejectManager() {
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);

        assertThatThrownBy(() -> authorizationService.requireOperationalCoordinatorForAssignment(30L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only operational coordinators can assign work orders");
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
