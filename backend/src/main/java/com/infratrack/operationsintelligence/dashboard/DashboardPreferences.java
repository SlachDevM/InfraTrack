package com.infratrack.operationsintelligence.dashboard;

import com.infratrack.organization.policy.dashboard.DashboardPolicy;
import jakarta.persistence.*;

@Entity
@Table(name = "dashboard_preferences")
public class DashboardPreferences {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "show_overview_widget", nullable = false)
    private boolean showOverviewWidget;

    @Column(name = "show_attention_widget", nullable = false)
    private boolean showAttentionWidget;

    @Column(name = "show_trend_widget", nullable = false)
    private boolean showTrendWidget;

    @Column(name = "show_recent_activity_widget", nullable = false)
    private boolean showRecentActivityWidget;

    @Column(name = "show_quick_navigation_widget", nullable = false)
    private boolean showQuickNavigationWidget;

    @Enumerated(EnumType.STRING)
    @Column(name = "default_trend_range", nullable = false, length = 50)
    private DashboardTrendRange defaultTrendRange;

    @Column(name = "widget_order_json", nullable = false, columnDefinition = "TEXT")
    private String widgetOrderJson;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected DashboardPreferences() {
    }

    public static DashboardPreferences createDefaultForUser(
            Long userId, String widgetOrderJson, DashboardPolicy policy) {
        DashboardPreferences preferences = new DashboardPreferences();
        long now = System.currentTimeMillis();
        preferences.userId = userId;
        preferences.showOverviewWidget = policy.isOverviewWidgetVisibleByDefault();
        preferences.showAttentionWidget = policy.isAttentionWidgetVisibleByDefault();
        preferences.showTrendWidget = policy.isTrendWidgetVisibleByDefault();
        preferences.showRecentActivityWidget = policy.isRecentActivityWidgetVisibleByDefault();
        preferences.showQuickNavigationWidget = policy.isQuickNavigationWidgetVisibleByDefault();
        preferences.defaultTrendRange = policy.defaultTrendRange();
        preferences.widgetOrderJson = widgetOrderJson;
        preferences.createdAt = now;
        preferences.updatedAt = now;
        return preferences;
    }

    public void applyDefaults(String widgetOrderJson, DashboardPolicy policy) {
        this.showOverviewWidget = policy.isOverviewWidgetVisibleByDefault();
        this.showAttentionWidget = policy.isAttentionWidgetVisibleByDefault();
        this.showTrendWidget = policy.isTrendWidgetVisibleByDefault();
        this.showRecentActivityWidget = policy.isRecentActivityWidgetVisibleByDefault();
        this.showQuickNavigationWidget = policy.isQuickNavigationWidgetVisibleByDefault();
        this.defaultTrendRange = policy.defaultTrendRange();
        this.widgetOrderJson = widgetOrderJson;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isShowOverviewWidget() {
        return showOverviewWidget;
    }

    public void setShowOverviewWidget(boolean showOverviewWidget) {
        this.showOverviewWidget = showOverviewWidget;
    }

    public boolean isShowAttentionWidget() {
        return showAttentionWidget;
    }

    public void setShowAttentionWidget(boolean showAttentionWidget) {
        this.showAttentionWidget = showAttentionWidget;
    }

    public boolean isShowTrendWidget() {
        return showTrendWidget;
    }

    public void setShowTrendWidget(boolean showTrendWidget) {
        this.showTrendWidget = showTrendWidget;
    }

    public boolean isShowRecentActivityWidget() {
        return showRecentActivityWidget;
    }

    public void setShowRecentActivityWidget(boolean showRecentActivityWidget) {
        this.showRecentActivityWidget = showRecentActivityWidget;
    }

    public boolean isShowQuickNavigationWidget() {
        return showQuickNavigationWidget;
    }

    public void setShowQuickNavigationWidget(boolean showQuickNavigationWidget) {
        this.showQuickNavigationWidget = showQuickNavigationWidget;
    }

    public DashboardTrendRange getDefaultTrendRange() {
        return defaultTrendRange;
    }

    public void setDefaultTrendRange(DashboardTrendRange defaultTrendRange) {
        this.defaultTrendRange = defaultTrendRange;
    }

    public String getWidgetOrderJson() {
        return widgetOrderJson;
    }

    public void setWidgetOrderJson(String widgetOrderJson) {
        this.widgetOrderJson = widgetOrderJson;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}
