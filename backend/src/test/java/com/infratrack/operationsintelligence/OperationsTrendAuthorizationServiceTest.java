package com.infratrack.operationsintelligence;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.operationsintelligence.dto.TrendScopeResponse;
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
class OperationsTrendAuthorizationServiceTest {

    @Mock
    private OperationsKpiAuthorizationService kpiAuthorizationService;

    @InjectMocks
    private OperationsTrendAuthorizationService authorizationService;

    @Test
    void resolveScope_shouldReturnGlobalScopeForAdministrator() {
        when(kpiAuthorizationService.resolveScope(1L)).thenReturn(OperationsKpiScope.global());

        OperationsKpiScope scope = authorizationService.resolveScope(1L);

        assertThat(scope.isGlobal()).isTrue();
    }

    @Test
    void resolveScopeResponse_shouldReturnGlobalForAdministrator() {
        when(kpiAuthorizationService.resolveScope(1L)).thenReturn(OperationsKpiScope.global());

        TrendScopeResponse scope = authorizationService.resolveScopeResponse(1L);

        assertThat(scope.getType()).isEqualTo("GLOBAL");
        assertThat(scope.getDepartmentId()).isNull();
    }

    @Test
    void resolveScopeResponse_shouldReturnDepartmentForManager() {
        when(kpiAuthorizationService.resolveScope(2L)).thenReturn(OperationsKpiScope.forDepartment(10L));

        TrendScopeResponse scope = authorizationService.resolveScopeResponse(2L);

        assertThat(scope.getType()).isEqualTo("DEPARTMENT");
        assertThat(scope.getDepartmentId()).isEqualTo(10L);
    }

    @Test
    void resolveScope_shouldRejectFieldEmployee() {
        when(kpiAuthorizationService.resolveScope(4L))
                .thenThrow(new ForbiddenOperationException("You do not have permission to view operational KPIs."));

        assertThatThrownBy(() -> authorizationService.resolveScope(4L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void resolveScope_shouldRejectContractor() {
        when(kpiAuthorizationService.resolveScope(5L))
                .thenThrow(new ForbiddenOperationException("You do not have permission to view operational KPIs."));

        assertThatThrownBy(() -> authorizationService.resolveScope(5L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanViewTrends_shouldDelegateToKpiAuthorization() {
        assertThatCode(() -> authorizationService.requireCanViewTrends(1L))
                .doesNotThrowAnyException();
    }
}
