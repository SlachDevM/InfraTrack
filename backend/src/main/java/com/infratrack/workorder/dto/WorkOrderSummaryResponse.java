package com.infratrack.workorder.dto;

import com.infratrack.user.User;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;

import java.time.LocalDateTime;
import java.util.Map;

public class WorkOrderSummaryResponse {

    private Long id;
    private Long operationalDecisionId;
    private String assetName;
    private Long assetDepartmentId;
    private WorkType workType;
    private WorkOrderStatus status;
    private String description;
    private WorkOrderPriority priority;
    private LocalDateTime createdAtBusinessDate;
    private Long assignedToUserId;
    private String assignedToUserName;
    private LocalDateTime assignedAt;

    public static WorkOrderSummaryResponse from(WorkOrder workOrder, Map<Long, String> userNamesById) {
        WorkOrderSummaryResponse response = new WorkOrderSummaryResponse();
        response.id = workOrder.getId();
        response.operationalDecisionId = workOrder.getOperationalDecision().getId();
        response.assetName = workOrder.getAsset().getName();
        if (workOrder.getAsset().getDepartment() != null) {
            response.assetDepartmentId = workOrder.getAsset().getDepartment().getId();
        }
        response.workType = workOrder.getWorkType();
        response.status = workOrder.getStatus();
        response.description = workOrder.getDescription();
        response.priority = workOrder.getPriority();
        response.createdAtBusinessDate = workOrder.getCreatedAtBusinessDate();
        response.assignedToUserId = workOrder.getAssignedToUserId();
        if (workOrder.getAssignedToUserId() != null) {
            response.assignedToUserName = userNamesById.get(workOrder.getAssignedToUserId());
        }
        response.assignedAt = workOrder.getAssignedAt();
        return response;
    }

    public static WorkOrderSummaryResponse from(WorkOrder workOrder, User assignedToUser) {
        WorkOrderSummaryResponse response = from(workOrder, Map.of());
        if (assignedToUser != null) {
            response.assignedToUserName = assignedToUser.getName();
        }
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getOperationalDecisionId() {
        return operationalDecisionId;
    }

    public String getAssetName() {
        return assetName;
    }

    public Long getAssetDepartmentId() {
        return assetDepartmentId;
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

    public LocalDateTime getCreatedAtBusinessDate() {
        return createdAtBusinessDate;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public String getAssignedToUserName() {
        return assignedToUserName;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }
}
