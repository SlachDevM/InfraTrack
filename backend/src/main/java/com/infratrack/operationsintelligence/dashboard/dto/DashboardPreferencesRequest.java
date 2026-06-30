package com.infratrack.operationsintelligence.dashboard.dto;

import java.util.List;

public class DashboardPreferencesRequest {

    private Boolean showOverviewWidget;
    private Boolean showAttentionWidget;
    private Boolean showTrendWidget;
    private Boolean showRecentActivityWidget;
    private Boolean showQuickNavigationWidget;
    private String defaultTrendRange;
    private List<String> widgetOrder;

    public Boolean getShowOverviewWidget() {
        return showOverviewWidget;
    }

    public void setShowOverviewWidget(Boolean showOverviewWidget) {
        this.showOverviewWidget = showOverviewWidget;
    }

    public Boolean getShowAttentionWidget() {
        return showAttentionWidget;
    }

    public void setShowAttentionWidget(Boolean showAttentionWidget) {
        this.showAttentionWidget = showAttentionWidget;
    }

    public Boolean getShowTrendWidget() {
        return showTrendWidget;
    }

    public void setShowTrendWidget(Boolean showTrendWidget) {
        this.showTrendWidget = showTrendWidget;
    }

    public Boolean getShowRecentActivityWidget() {
        return showRecentActivityWidget;
    }

    public void setShowRecentActivityWidget(Boolean showRecentActivityWidget) {
        this.showRecentActivityWidget = showRecentActivityWidget;
    }

    public Boolean getShowQuickNavigationWidget() {
        return showQuickNavigationWidget;
    }

    public void setShowQuickNavigationWidget(Boolean showQuickNavigationWidget) {
        this.showQuickNavigationWidget = showQuickNavigationWidget;
    }

    public String getDefaultTrendRange() {
        return defaultTrendRange;
    }

    public void setDefaultTrendRange(String defaultTrendRange) {
        this.defaultTrendRange = defaultTrendRange;
    }

    public List<String> getWidgetOrder() {
        return widgetOrder;
    }

    public void setWidgetOrder(List<String> widgetOrder) {
        this.widgetOrder = widgetOrder;
    }
}
