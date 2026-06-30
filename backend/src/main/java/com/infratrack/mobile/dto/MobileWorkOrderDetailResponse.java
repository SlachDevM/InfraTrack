package com.infratrack.mobile.dto;

import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;

public class MobileWorkOrderDetailResponse {

    private Long id;
    private String status;
    private WorkOrderPriority priority;
    private String description;
    private Long assignedTo;

    public static MobileWorkOrderDetailResponse from(WorkOrder workOrder) {
        MobileWorkOrderDetailResponse response = new MobileWorkOrderDetailResponse();
        response.id = workOrder.getId();
        response.status = workOrder.getStatus() != null ? workOrder.getStatus().name() : null;
        response.priority = workOrder.getPriority();
        response.description = workOrder.getDescription();
        response.assignedTo = workOrder.getAssignedToUserId();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
        return status;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public String getDescription() {
        return description;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }
}
