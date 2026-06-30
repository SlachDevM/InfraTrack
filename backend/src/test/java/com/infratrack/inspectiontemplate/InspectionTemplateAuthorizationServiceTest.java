package com.infratrack.inspectiontemplate;

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
class InspectionTemplateAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private InspectionTemplateAuthorizationService authorizationService;

    @Test
    void requireCanViewTemplates_shouldAllowAdministrator() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));

        assertThatCode(() -> authorizationService.requireCanViewTemplates(1L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewTemplates_shouldAllowManager() {
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));

        assertThatCode(() -> authorizationService.requireCanViewTemplates(2L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewTemplates_shouldAllowOperationalCoordinator() {
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatCode(() -> authorizationService.requireCanViewTemplates(3L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewTemplates_shouldRejectFieldEmployee() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> authorizationService.requireCanViewTemplates(4L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to view inspection templates");
    }

    @Test
    void requireCanViewTemplates_shouldRejectContractor() {
        when(userService.getById(5L)).thenReturn(user(5L, UserRole.CONTRACTOR));

        assertThatThrownBy(() -> authorizationService.requireCanViewTemplates(5L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanViewTemplateChecklist_shouldAllowFieldEmployeeForPublishedTemplate() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatCode(() -> authorizationService.requireCanViewTemplateChecklist(
                4L,
                InspectionTemplateStatus.PUBLISHED))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewTemplateChecklist_shouldRejectFieldEmployeeForDraftTemplate() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> authorizationService.requireCanViewTemplateChecklist(
                4L,
                InspectionTemplateStatus.DRAFT))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to view inspection template checklist questions");
    }

    @Test
    void requireCanViewTemplateChecklist_shouldAllowAdministratorForAnyStatus() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));

        assertThatCode(() -> authorizationService.requireCanViewTemplateChecklist(
                1L,
                InspectionTemplateStatus.DRAFT))
                .doesNotThrowAnyException();
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
                .hasMessage("Only administrators can manage inspection templates");
    }

    @Test
    void requireAdministrator_shouldRejectOperationalCoordinator() {
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatThrownBy(() -> authorizationService.requireAdministrator(3L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
