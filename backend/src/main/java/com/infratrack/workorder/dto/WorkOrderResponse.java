package com.infratrack.workorder.dto;

import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;

import java.time.LocalDateTime;

public class WorkOrderResponse {

    private Long id;
    private Long operationalDecisionId;
    private Long assetId;
    private String assetName;
    private WorkType workType;
    private WorkOrderStatus status;
    private String description;
    private WorkOrderPriority priority;
    private Long createdByUserId;
    private LocalDateTime createdAtBusinessDate;
    private Long createdAt;
    private Long updatedAt;

    public static WorkOrderResponse from(WorkOrder workOrder) {
        WorkOrderResponse response = new WorkOrderResponse();
        response.id = workOrder.getId();
        response.operationalDecisionId = workOrder.getOperationalDecision().getId();
        response.assetId = workOrder.getAsset().getId();
        response.assetName = workOrder.getAsset().getName();
        response.workType = workOrder.getWorkType();
        response.status = workOrder.getStatus();
        response.description = workOrder.getDescription();
        response.priority = workOrder.getPriority();
        response.createdByUserId = workOrder.getCreatedByUserId();
        response.createdAtBusinessDate = workOrder.getCreatedAtBusinessDate();
        response.createdAt = workOrder.getCreatedAt();
        response.updatedAt = workOrder.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getOperationalDecisionId() {
        return operationalDecisionId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public LocalDateTime getCreatedAtBusinessDate() {
        return createdAtBusinessDate;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
