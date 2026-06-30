package com.infratrack.operationsintelligence.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class WorkOrderKpiResponse {

    private long openWorkOrders;
    private long inProgressWorkOrders;
    private long completedWorkOrders;

    @Schema(description = "Always zero — work orders have no due date field in the current model")
    private long overdueWorkOrders;

    public long getOpenWorkOrders() {
        return openWorkOrders;
    }

    public void setOpenWorkOrders(long openWorkOrders) {
        this.openWorkOrders = openWorkOrders;
    }

    public long getInProgressWorkOrders() {
        return inProgressWorkOrders;
    }

    public void setInProgressWorkOrders(long inProgressWorkOrders) {
        this.inProgressWorkOrders = inProgressWorkOrders;
    }

    public long getCompletedWorkOrders() {
        return completedWorkOrders;
    }

    public void setCompletedWorkOrders(long completedWorkOrders) {
        this.completedWorkOrders = completedWorkOrders;
    }

    public long getOverdueWorkOrders() {
        return overdueWorkOrders;
    }

    public void setOverdueWorkOrders(long overdueWorkOrders) {
        this.overdueWorkOrders = overdueWorkOrders;
    }
}
