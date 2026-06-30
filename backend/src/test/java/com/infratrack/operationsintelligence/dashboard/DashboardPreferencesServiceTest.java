package com.infratrack.operationsintelligence.dashboard;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.operationsintelligence.dashboard.dto.DashboardPreferencesRequest;
import com.infratrack.operationsintelligence.dashboard.dto.DashboardPreferencesResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardPreferencesServiceTest {

    @Mock
    private DashboardPreferencesAuthorizationService authorizationService;

    @Mock
    private DashboardPreferencesRepository repository;

    @InjectMocks
    private DashboardPreferencesService preferencesService;

    @Test
    void getPreferences_shouldReturnDefaultsWhenNoRecordExists() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());

        DashboardPreferencesResponse response = preferencesService.getPreferences(1L);

        assertThat(response.isShowOverviewWidget()).isTrue();
        assertThat(response.isShowTrendWidget()).isTrue();
        assertThat(response.getDefaultTrendRange()).isEqualTo("LAST_30_DAYS");
        assertThat(response.getWidgetOrder()).containsExactly(
                "OVERVIEW", "ATTENTION", "TRENDS", "RECENT_ACTIVITY", "QUICK_NAVIGATION");
    }

    @Test
    void savePreferences_shouldPersistPreferencesForUser() {
        when(repository.findByUserId(1L)).thenReturn(Optional.empty());
        when(repository.save(any(DashboardPreferences.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DashboardPreferencesRequest request = validRequest();
        request.setShowRecentActivityWidget(false);
        request.setWidgetOrder(List.of("TRENDS", "OVERVIEW"));

        DashboardPreferencesResponse response = preferencesService.savePreferences(1L, request);

        assertThat(response.isShowRecentActivityWidget()).isFalse();
        assertThat(response.getWidgetOrder().subList(0, 2)).containsExactly("TRENDS", "OVERVIEW");

        ArgumentCaptor<DashboardPreferences> captor = ArgumentCaptor.forClass(DashboardPreferences.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getDefaultTrendRange()).isEqualTo(DashboardTrendRange.LAST_90_DAYS);
    }

    @Test
    void savePreferences_shouldRejectHidingEveryWidget() {
        DashboardPreferencesRequest request = validRequest();
        request.setShowOverviewWidget(false);
        request.setShowAttentionWidget(false);
        request.setShowTrendWidget(false);
        request.setShowRecentActivityWidget(false);
        request.setShowQuickNavigationWidget(false);

        assertThatThrownBy(() -> preferencesService.savePreferences(1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("At least one dashboard widget must remain visible.");

        verify(repository, never()).save(any());
    }

    @Test
    void savePreferences_shouldRejectInvalidTrendRange() {
        DashboardPreferencesRequest request = validRequest();
        request.setDefaultTrendRange("LAST_365_DAYS");

        assertThatThrownBy(() -> preferencesService.savePreferences(1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Unsupported trend range");
    }

    @Test
    void savePreferences_shouldRejectInvalidWidgetType() {
        DashboardPreferencesRequest request = validRequest();
        request.setWidgetOrder(List.of("INVALID_WIDGET"));

        assertThatThrownBy(() -> preferencesService.savePreferences(1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessageContaining("Unsupported widget type");
    }

    @Test
    void resetPreferences_shouldDeleteRecordAndReturnDefaults() {
        DashboardPreferencesResponse response = preferencesService.resetPreferences(1L);

        verify(repository).deleteByUserId(1L);
        assertThat(response.getDefaultTrendRange()).isEqualTo("LAST_30_DAYS");
        assertThat(response.isShowOverviewWidget()).isTrue();
    }

    @Test
    void getPreferences_shouldRejectUnauthorizedUser() {
        org.mockito.Mockito.doThrow(new ForbiddenOperationException("forbidden"))
                .when(authorizationService).requireCanManageOwnPreferences(4L);

        assertThatThrownBy(() -> preferencesService.getPreferences(4L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private static DashboardPreferencesRequest validRequest() {
        DashboardPreferencesRequest request = new DashboardPreferencesRequest();
        request.setShowOverviewWidget(true);
        request.setShowAttentionWidget(true);
        request.setShowTrendWidget(true);
        request.setShowRecentActivityWidget(true);
        request.setShowQuickNavigationWidget(true);
        request.setDefaultTrendRange("LAST_90_DAYS");
        request.setWidgetOrder(List.of("OVERVIEW", "ATTENTION", "TRENDS", "RECENT_ACTIVITY", "QUICK_NAVIGATION"));
        return request;
    }
}
