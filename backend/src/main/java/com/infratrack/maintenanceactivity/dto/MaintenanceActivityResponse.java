package com.infratrack.maintenanceactivity.dto;

import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.workorder.WorkOrderStatus;

import java.time.LocalDateTime;

public class MaintenanceActivityResponse {

    private Long id;
    private Long workOrderId;
    private Long assetId;
    private String assetName;
    private Long performedByUserId;
    private String completionNotes;
    private LocalDateTime completedAt;
    private WorkOrderStatus workOrderStatus;
    private Long createdAt;
    private Long updatedAt;

    public static MaintenanceActivityResponse from(MaintenanceActivity maintenanceActivity) {
        MaintenanceActivityResponse response = new MaintenanceActivityResponse();
        response.id = maintenanceActivity.getId();
        response.workOrderId = maintenanceActivity.getWorkOrder().getId();
        response.assetId = maintenanceActivity.getAsset().getId();
        response.assetName = maintenanceActivity.getAsset().getName();
        response.performedByUserId = maintenanceActivity.getPerformedByUserId();
        response.completionNotes = maintenanceActivity.getCompletionNotes();
        response.completedAt = maintenanceActivity.getCompletedAt();
        response.workOrderStatus = maintenanceActivity.getWorkOrder().getStatus();
        response.createdAt = maintenanceActivity.getCreatedAt();
        response.updatedAt = maintenanceActivity.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public Long getPerformedByUserId() {
        return performedByUserId;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public WorkOrderStatus getWorkOrderStatus() {
        return workOrderStatus;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
