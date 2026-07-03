package com.infratrack.operationsintelligence.dashboard;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.operationsintelligence.dashboard.dto.DashboardPreferencesRequest;
import com.infratrack.operationsintelligence.dashboard.dto.DashboardPreferencesResponse;
import com.infratrack.organization.policy.dashboard.DashboardPolicy;
import com.infratrack.organization.policy.dashboard.DashboardPolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DashboardPreferencesService {

    private final DashboardPreferencesAuthorizationService authorizationService;
    private final DashboardPreferencesRepository repository;
    private final DashboardPolicyService dashboardPolicyService;

    public DashboardPreferencesService(
            DashboardPreferencesAuthorizationService authorizationService,
            DashboardPreferencesRepository repository,
            DashboardPolicyService dashboardPolicyService) {
        this.authorizationService = authorizationService;
        this.repository = repository;
        this.dashboardPolicyService = dashboardPolicyService;
    }

    @Transactional(readOnly = true)
    public DashboardPreferencesResponse getPreferences(Long userId) {
        authorizationService.requireCanManageOwnPreferences(userId);
        return repository.findByUserId(userId)
                .map(this::toResponse)
                .orElseGet(this::defaultResponse);
    }

    @Transactional
    public DashboardPreferencesResponse savePreferences(Long userId, DashboardPreferencesRequest request) {
        authorizationService.requireCanManageOwnPreferences(userId);
        validateVisibility(request);
        DashboardTrendRange trendRange = DashboardTrendRange.parse(request.getDefaultTrendRange());
        List<DashboardWidgetType> widgetOrder = DashboardWidgetOrderSupport.parseAndNormalize(request.getWidgetOrder());
        String widgetOrderJson = DashboardWidgetOrderSupport.serialize(widgetOrder);

        DashboardPolicy policy = dashboardPolicyService.getPolicy();
        DashboardPreferences preferences = repository.findByUserId(userId)
                .orElseGet(() -> DashboardPreferences.createDefaultForUser(userId, widgetOrderJson, policy));

        preferences.setShowOverviewWidget(Boolean.TRUE.equals(request.getShowOverviewWidget()));
        preferences.setShowAttentionWidget(Boolean.TRUE.equals(request.getShowAttentionWidget()));
        preferences.setShowTrendWidget(Boolean.TRUE.equals(request.getShowTrendWidget()));
        preferences.setShowRecentActivityWidget(Boolean.TRUE.equals(request.getShowRecentActivityWidget()));
        preferences.setShowQuickNavigationWidget(Boolean.TRUE.equals(request.getShowQuickNavigationWidget()));
        preferences.setDefaultTrendRange(trendRange);
        preferences.setWidgetOrderJson(widgetOrderJson);
        preferences.setUpdatedAt(System.currentTimeMillis());

        return toResponse(repository.save(preferences));
    }

    @Transactional
    public DashboardPreferencesResponse resetPreferences(Long userId) {
        authorizationService.requireCanManageOwnPreferences(userId);
        repository.deleteByUserId(userId);
        return defaultResponse();
    }

    private void validateVisibility(DashboardPreferencesRequest request) {
        boolean anyVisible = Boolean.TRUE.equals(request.getShowOverviewWidget())
                || Boolean.TRUE.equals(request.getShowAttentionWidget())
                || Boolean.TRUE.equals(request.getShowTrendWidget())
                || Boolean.TRUE.equals(request.getShowRecentActivityWidget())
                || Boolean.TRUE.equals(request.getShowQuickNavigationWidget());
        if (!anyVisible) {
            throw new BusinessValidationException("At least one dashboard widget must remain visible.");
        }
        if (request.getDefaultTrendRange() == null) {
            throw new BusinessValidationException("Trend range is required.");
        }
    }

    private DashboardPreferencesResponse defaultResponse() {
        DashboardPolicy policy = dashboardPolicyService.getPolicy();
        DashboardPreferencesResponse response = new DashboardPreferencesResponse();
        response.setShowOverviewWidget(policy.isOverviewWidgetVisibleByDefault());
        response.setShowAttentionWidget(policy.isAttentionWidgetVisibleByDefault());
        response.setShowTrendWidget(policy.isTrendWidgetVisibleByDefault());
        response.setShowRecentActivityWidget(policy.isRecentActivityWidgetVisibleByDefault());
        response.setShowQuickNavigationWidget(policy.isQuickNavigationWidgetVisibleByDefault());
        response.setDefaultTrendRange(policy.defaultTrendRange().name());
        response.setWidgetOrder(policy.defaultWidgetOrder().stream().map(Enum::name).toList());
        return response;
    }

    private DashboardPreferencesResponse toResponse(DashboardPreferences preferences) {
        DashboardPreferencesResponse response = new DashboardPreferencesResponse();
        response.setShowOverviewWidget(preferences.isShowOverviewWidget());
        response.setShowAttentionWidget(preferences.isShowAttentionWidget());
        response.setShowTrendWidget(preferences.isShowTrendWidget());
        response.setShowRecentActivityWidget(preferences.isShowRecentActivityWidget());
        response.setShowQuickNavigationWidget(preferences.isShowQuickNavigationWidget());
        response.setDefaultTrendRange(preferences.getDefaultTrendRange().name());
        response.setWidgetOrder(
                DashboardWidgetOrderSupport.parseStored(preferences.getWidgetOrderJson()).stream()
                        .map(Enum::name)
                        .toList());
        return response;
    }
}
