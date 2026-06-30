package com.infratrack.operationsintelligence.dto;

public class InspectionKpiResponse {

    private long assignedInspections;
    private long inProgressInspections;
    private long completedInspections;
    private long overdueInspections;

    public long getAssignedInspections() {
        return assignedInspections;
    }

    public void setAssignedInspections(long assignedInspections) {
        this.assignedInspections = assignedInspections;
    }

    public long getInProgressInspections() {
        return inProgressInspections;
    }

    public void setInProgressInspections(long inProgressInspections) {
        this.inProgressInspections = inProgressInspections;
    }

    public long getCompletedInspections() {
        return completedInspections;
    }

    public void setCompletedInspections(long completedInspections) {
        this.completedInspections = completedInspections;
    }

    public long getOverdueInspections() {
        return overdueInspections;
    }

    public void setOverdueInspections(long overdueInspections) {
        this.overdueInspections = overdueInspections;
    }
}
