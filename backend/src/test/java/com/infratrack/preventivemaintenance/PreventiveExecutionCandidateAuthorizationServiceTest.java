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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveExecutionCandidateAuthorizationServiceTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private PreventiveExecutionCandidateAuthorizationService authorizationService;

    @Test
    void requireCanGenerateCandidates_shouldAllowAdministratorAndManager() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));

        assertThatCode(() -> authorizationService.requireCanGenerateCandidates(1L))
                .doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanGenerateCandidates(2L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanGenerateCandidates_shouldRejectCoordinator() {
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatThrownBy(() -> authorizationService.requireCanGenerateCandidates(3L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to generate preventive execution candidates");
    }

    @Test
    void requireCanGenerateCandidates_shouldRejectFieldEmployee() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> authorizationService.requireCanGenerateCandidates(4L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanViewCandidates_shouldAllowAdministratorManagerAndCoordinator() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatCode(() -> authorizationService.requireCanViewCandidates(1L))
                .doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanViewCandidates(2L))
                .doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanViewCandidates(3L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanViewCandidates_shouldRejectFieldEmployee() {
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> authorizationService.requireCanViewCandidates(4L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to view preventive execution candidates");
    }

    @Test
    void requireAuthorizedToViewReportAsset_shouldRejectFieldEmployee() {
        com.infratrack.asset.Asset asset = mock(com.infratrack.asset.Asset.class);
        when(userService.getById(4L)).thenReturn(user(4L, UserRole.FIELD_EMPLOYEE));

        assertThatThrownBy(() -> authorizationService.requireAuthorizedToViewReportAsset(4L, asset))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You do not have permission to view preventive execution reports");
    }

    @Test
    void requireAuthorizedToViewReportAsset_shouldRejectCrossDepartmentManager() {
        com.infratrack.department.Department managerDept = new com.infratrack.department.Department("Water");
        managerDept.setId(10L);
        com.infratrack.department.Department assetDept = new com.infratrack.department.Department("Roads");
        assetDept.setId(20L);
        com.infratrack.user.User manager = user(2L, UserRole.MANAGER);
        manager.setDepartment(managerDept);
        com.infratrack.asset.Asset asset = mock(com.infratrack.asset.Asset.class);
        when(asset.getDepartment()).thenReturn(assetDept);
        when(userService.getById(2L)).thenReturn(manager);

        assertThatThrownBy(() -> authorizationService.requireAuthorizedToViewReportAsset(2L, asset))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanReviewCandidates_shouldAllowAdministratorAndManager() {
        when(userService.getById(1L)).thenReturn(user(1L, UserRole.ADMINISTRATOR));
        when(userService.getById(2L)).thenReturn(user(2L, UserRole.MANAGER));

        assertThatCode(() -> authorizationService.requireCanReviewCandidates(1L))
                .doesNotThrowAnyException();
        assertThatCode(() -> authorizationService.requireCanReviewCandidates(2L))
                .doesNotThrowAnyException();
    }

    @Test
    void requireCanReviewCandidates_shouldRejectCoordinator() {
        when(userService.getById(3L)).thenReturn(user(3L, UserRole.OPERATIONAL_COORDINATOR));

        assertThatThrownBy(() -> authorizationService.requireCanReviewCandidates(3L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
