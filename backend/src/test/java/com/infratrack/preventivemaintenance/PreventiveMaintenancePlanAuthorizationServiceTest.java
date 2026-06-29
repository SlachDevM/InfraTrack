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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveMaintenancePlanAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PreventiveMaintenancePlanAuthorizationService authorizationService;

    @Test
    void requireCanViewPlans_shouldAllowAdministrator() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));

        assertThatCode(() -> authorizationService.requireCanViewPlans(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewPlans_shouldAllowManager() {
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));

        assertThatCode(() -> authorizationService.requireCanViewPlans(2L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewPlans_shouldAllowOperationalCoordinator() {
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatCode(() -> authorizationService.requireCanViewPlans(3L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewPlans_shouldRejectFieldEmployee() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> authorizationService.requireCanViewPlans(4L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to view preventive maintenance plans");
    }

    @Test
    void requireCanViewPlans_shouldRejectContractor() {
        when(userService.getById(5L)).thenReturn(user(5L, UserRole.CONTRACTOR));

        assertThatThrownBy(() -> authorizationService.requireCanViewPlans(5L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireAdministrator_shouldAllowAdministrator() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));

        assertThatCode(() -> authorizationService.requireAdministrator(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireAdministrator_shouldRejectManager() {
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));

        assertThatThrownBy(() -> authorizationService.requireAdministrator(2L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only administrators can manage preventive maintenance plans");
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
