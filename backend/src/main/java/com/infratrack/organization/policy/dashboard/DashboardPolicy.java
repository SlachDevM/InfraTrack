package com.infratrack.organization.policy.dashboard;

import com.infratrack.operationsintelligence.dashboard.DashboardTrendRange;
import com.infratrack.operationsintelligence.dashboard.DashboardWidgetType;

import java.util.List;

/**
 * Organizational dashboard defaults (BDR-004).
 *
 * <p>User-scoped {@link com.infratrack.operationsintelligence.dashboard.DashboardPreferences}
 * override these values when saved. When no saved preferences exist, the active policy supplies
 * the dashboard presentation defaults.
 */
public interface DashboardPolicy {

    boolean isOverviewWidgetVisibleByDefault();

    boolean isAttentionWidgetVisibleByDefault();

    boolean isTrendWidgetVisibleByDefault();

    boolean isRecentActivityWidgetVisibleByDefault();

    boolean isQuickNavigationWidgetVisibleByDefault();

    List<DashboardWidgetType> defaultWidgetOrder();

    DashboardTrendRange defaultTrendRange();
}
