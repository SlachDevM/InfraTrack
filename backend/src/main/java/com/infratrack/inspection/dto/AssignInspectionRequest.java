package com.infratrack.inspection.dto;

import com.infratrack.inspection.InspectionPriority;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class AssignInspectionRequest {

    @NotNull
    @Positive
    private Long businessTriggerId;

    @NotNull
    @Positive
    private Long assignedToUserId;

    private InspectionPriority priority;

    private LocalDate expectedCompletionDate;

    public Long getBusinessTriggerId() {
        return businessTriggerId;
    }

    public void setBusinessTriggerId(Long businessTriggerId) {
        this.businessTriggerId = businessTriggerId;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public void setAssignedToUserId(Long assignedToUserId) {
        this.assignedToUserId = assignedToUserId;
    }

    public InspectionPriority getPriority() {
        return priority;
    }

    public void setPriority(InspectionPriority priority) {
        this.priority = priority;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public void setExpectedCompletionDate(LocalDate expectedCompletionDate) {
        this.expectedCompletionDate = expectedCompletionDate;
    }
}
