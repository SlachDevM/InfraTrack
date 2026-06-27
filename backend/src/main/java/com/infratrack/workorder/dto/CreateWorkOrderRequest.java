package com.infratrack.workorder.dto;

import com.infratrack.workorder.WorkOrderPriority;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class CreateWorkOrderRequest {

    @NotNull
    @Positive
    private Long operationalDecisionId;

    @NotBlank
    @Size(max = 4000)
    private String description;

    @NotNull
    @Schema(description = "Operational priority for the work order")
    private WorkOrderPriority priority;

    @NotNull
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
