package com.infratrack.mobile.dto;

public class MobileDashboardResponse {

    private long assignedInspections;
    private long assignedWorkOrders;
    private long overdueInspections;
    private long overdueWorkOrders;
    private long completedToday;

    public MobileDashboardResponse(
            long assignedInspections,
            long assignedWorkOrders,
            long overdueInspections,
            long overdueWorkOrders,
            long completedToday) {
        this.assignedInspections = assignedInspections;
        this.assignedWorkOrders = assignedWorkOrders;
        this.overdueInspections = overdueInspections;
        this.overdueWorkOrders = overdueWorkOrders;
        this.completedToday = completedToday;
    }

    public long getAssignedInspections() {
        return assignedInspections;
    }

    public long getAssignedWorkOrders() {
        return assignedWorkOrders;
    }

    public long getOverdueInspections() {
        return overdueInspections;
    }

    public long getOverdueWorkOrders() {
        return overdueWorkOrders;
    }

    public long getCompletedToday() {
        return completedToday;
    }
}
