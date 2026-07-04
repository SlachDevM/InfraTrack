package com.infratrack.mobile.dto;

import com.infratrack.maintenanceactivity.MaintenanceActivity;

import java.time.LocalDateTime;

public class MobileAssetLastMaintenanceResponse {

    private Long id;
    private Long workOrderId;
    private LocalDateTime completedAt;
    private String performedBy;

    public static MobileAssetLastMaintenanceResponse from(MaintenanceActivity activity, String performedByName) {
        MobileAssetLastMaintenanceResponse response = new MobileAssetLastMaintenanceResponse();
        response.id = activity.getId();
        response.workOrderId = activity.getWorkOrder().getId();
        response.completedAt = activity.getCompletedAt();
        response.performedBy = performedByName;
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public String getPerformedBy() {
        return performedBy;
    }
}
