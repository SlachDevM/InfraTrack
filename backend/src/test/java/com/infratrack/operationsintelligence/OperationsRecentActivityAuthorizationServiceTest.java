package com.infratrack.operationsintelligence;

import com.infratrack.exception.ForbiddenOperationException;
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
class OperationsRecentActivityAuthorizationServiceTest {

    @Mock
    private OperationsKpiAuthorizationService kpiAuthorizationService;

    @InjectMocks
    private OperationsRecentActivityAuthorizationService authorizationService;

    @Test
    void resolveScope_shouldDelegateToKpiAuthorization() {
        when(kpiAuthorizationService.resolveScope(1L)).thenReturn(OperationsKpiScope.global());

        OperationsKpiScope scope = authorizationService.resolveScope(1L);

        assertThat(scope.isGlobal()).isTrue();
    }

    @Test
    void resolveScope_shouldRejectFieldEmployee() {
        when(kpiAuthorizationService.resolveScope(4L))
                .thenThrow(new ForbiddenOperationException("You do not have permission to view operational KPIs."));

        assertThatThrownBy(() -> authorizationService.resolveScope(4L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void requireCanViewRecentActivity_shouldDelegateToKpiAuthorization() {
        assertThatCode(() -> authorizationService.requireCanViewRecentActivity(1L))
                .doesNotThrowAnyException();
    }
}
