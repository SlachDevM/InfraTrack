package com.infratrack.organization.policy.dashboard;

import com.infratrack.operationsintelligence.dashboard.DashboardTrendRange;
import com.infratrack.operationsintelligence.dashboard.DashboardWidgetType;

import java.util.List;

/**
 * Default dashboard policy matching the original fixed InfraTrack behaviour.
 */
public class DefaultDashboardPolicy implements DashboardPolicy {

    @Override
    public boolean isOverviewWidgetVisibleByDefault() {
        return true;
    }

    @Override
    public boolean isAttentionWidgetVisibleByDefault() {
        return true;
    }

    @Override
    public boolean isTrendWidgetVisibleByDefault() {
        return true;
    }

    @Override
    public boolean isRecentActivityWidgetVisibleByDefault() {
        return true;
    }

    @Override
    public boolean isQuickNavigationWidgetVisibleByDefault() {
        return true;
    }

    @Override
    public List<DashboardWidgetType> defaultWidgetOrder() {
        return DashboardWidgetType.DEFAULT_ORDER;
    }

    @Override
    public DashboardTrendRange defaultTrendRange() {
        return DashboardTrendRange.LAST_30_DAYS;
    }
}
