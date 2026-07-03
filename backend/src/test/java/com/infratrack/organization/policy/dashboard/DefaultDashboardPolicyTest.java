package com.infratrack.organization.policy.dashboard;

import com.infratrack.operationsintelligence.dashboard.DashboardTrendRange;
import com.infratrack.operationsintelligence.dashboard.DashboardWidgetType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultDashboardPolicyTest {

    private DefaultDashboardPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DefaultDashboardPolicy();
    }

    @Test
    void allWidgets_shouldBeVisibleByDefault() {
        assertThat(policy.isOverviewWidgetVisibleByDefault()).isTrue();
        assertThat(policy.isAttentionWidgetVisibleByDefault()).isTrue();
        assertThat(policy.isTrendWidgetVisibleByDefault()).isTrue();
        assertThat(policy.isRecentActivityWidgetVisibleByDefault()).isTrue();
        assertThat(policy.isQuickNavigationWidgetVisibleByDefault()).isTrue();
    }

    @Test
    void defaultWidgetOrder_shouldMatchOriginalDashboardOrder() {
        assertThat(policy.defaultWidgetOrder()).containsExactly(
                DashboardWidgetType.OVERVIEW,
                DashboardWidgetType.ATTENTION,
                DashboardWidgetType.TRENDS,
                DashboardWidgetType.RECENT_ACTIVITY,
                DashboardWidgetType.QUICK_NAVIGATION);
    }

    @Test
    void defaultTrendRange_shouldBeLast30Days() {
        assertThat(policy.defaultTrendRange()).isEqualTo(DashboardTrendRange.LAST_30_DAYS);
    }
}
