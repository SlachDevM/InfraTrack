package com.infratrack.mobile.sync.dto;

import com.infratrack.mobile.dto.MobileDashboardResponse;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/**
 * Server-computed mobile dashboard snapshot for sync delta download (M6.2-BE1).
 */
public class SyncDashboardDeltaResponse {

    @Schema(description = "Snapshot generation time (epoch millis)")
    private Long generatedAt;

    private long assignedInspections;

    private long assignedWorkOrders;

    private long overdueInspections;

    private long overdueWorkOrders;

    private long completedToday;

    public static SyncDashboardDeltaResponse from(MobileDashboardResponse dashboard, Instant generatedAt) {
        SyncDashboardDeltaResponse response = new SyncDashboardDeltaResponse();
        response.setGeneratedAt(generatedAt.toEpochMilli());
        response.setAssignedInspections(dashboard.getAssignedInspections());
        response.setAssignedWorkOrders(dashboard.getAssignedWorkOrders());
        response.setOverdueInspections(dashboard.getOverdueInspections());
        response.setOverdueWorkOrders(dashboard.getOverdueWorkOrders());
        response.setCompletedToday(dashboard.getCompletedToday());
        return response;
    }

    public Long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public long getAssignedInspections() {
        return assignedInspections;
    }

    public void setAssignedInspections(long assignedInspections) {
        this.assignedInspections = assignedInspections;
    }

    public long getAssignedWorkOrders() {
        return assignedWorkOrders;
    }

    public void setAssignedWorkOrders(long assignedWorkOrders) {
        this.assignedWorkOrders = assignedWorkOrders;
    }

    public long getOverdueInspections() {
        return overdueInspections;
    }

    public void setOverdueInspections(long overdueInspections) {
        this.overdueInspections = overdueInspections;
    }

    public long getOverdueWorkOrders() {
        return overdueWorkOrders;
    }

    public void setOverdueWorkOrders(long overdueWorkOrders) {
        this.overdueWorkOrders = overdueWorkOrders;
    }

    public long getCompletedToday() {
        return completedToday;
    }

    public void setCompletedToday(long completedToday) {
        this.completedToday = completedToday;
    }
}
