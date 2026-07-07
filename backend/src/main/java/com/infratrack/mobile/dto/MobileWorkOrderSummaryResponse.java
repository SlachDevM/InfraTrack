package com.infratrack.mobile.dto;

import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class MobileWorkOrderSummaryResponse {

    private Long workOrderId;
    private Long assetId;
    private String assetName;
    private WorkOrderStatus status;
    private WorkOrderPriority priority;
    private String description;
    private LocalDateTime assignedAt;
    private LocalDate expectedCompletionDate;

    public static MobileWorkOrderSummaryResponse from(WorkOrder workOrder) {
        MobileWorkOrderSummaryResponse response = new MobileWorkOrderSummaryResponse();
        response.workOrderId = workOrder.getId();
        response.assetId = workOrder.getAsset().getId();
        response.assetName = workOrder.getAsset().getName();
        response.status = workOrder.getStatus();
        response.priority = workOrder.getPriority();
        response.description = workOrder.getDescription();
        response.assignedAt = workOrder.getAssignedAt();
        response.expectedCompletionDate = null;
        return response;
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

    public WorkOrderStatus getStatus() {
        return status;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }
}
