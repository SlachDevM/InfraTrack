package com.infratrack.operationsintelligence.dashboard.dto;

import java.util.List;

public class DashboardPreferencesResponse {

    private boolean showOverviewWidget;
    private boolean showAttentionWidget;
    private boolean showTrendWidget;
    private boolean showRecentActivityWidget;
    private boolean showQuickNavigationWidget;
    private String defaultTrendRange;
    private List<String> widgetOrder;

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
