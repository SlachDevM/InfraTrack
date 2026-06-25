package com.infratrack.workorder.dto;

import com.infratrack.workorder.WorkOrderPriority;

import java.time.LocalDateTime;

public class CreateWorkOrderRequest {

    private Long operationalDecisionId;
    private String description;
    private WorkOrderPriority priority;
    private LocalDateTime createdAtBusinessDate;

    public Long getOperationalDecisionId() {
        return operationalDecisionId;
    }

    public void setOperationalDecisionId(Long operationalDecisionId) {
        this.operationalDecisionId = operationalDecisionId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrderPriority priority) {
        this.priority = priority;
    }

    public LocalDateTime getCreatedAtBusinessDate() {
        return createdAtBusinessDate;
    }

    public void setCreatedAtBusinessDate(LocalDateTime createdAtBusinessDate) {
        this.createdAtBusinessDate = createdAtBusinessDate;
    }
}
