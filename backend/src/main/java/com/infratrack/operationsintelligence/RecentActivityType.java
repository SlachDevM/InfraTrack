package com.infratrack.operationsintelligence;

public enum RecentActivityType {
    INSPECTION_COMPLETED("Inspection completed", "/inspections"),
    ISSUE_CREATED("Issue created", "/issues"),
    WORK_ORDER_COMPLETED("Work order completed", "/work-orders"),
    PREVENTIVE_CANDIDATE_GENERATED("Preventive candidate generated", "/preventive-execution-candidates"),
    PREVENTIVE_CANDIDATE_APPROVED("Preventive candidate approved", "/preventive-execution-candidates"),
    SUGGESTED_ACTION_ACCEPTED("Suggested action accepted", "/inspections");

    private final String title;
    private final String route;

    RecentActivityType(String title, String route) {
        this.title = title;
        this.route = route;
    }

    public String getTitle() {
        return title;
    }

    public String getRoute() {
        return route;
    }
}
