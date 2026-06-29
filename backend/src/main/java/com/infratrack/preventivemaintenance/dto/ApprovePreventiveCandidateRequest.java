package com.infratrack.preventivemaintenance.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class ApprovePreventiveCandidateRequest {

    @NotNull
    @Positive
    private Long assigneeId;

    private Long plannedAt;

    @Size(max = 4000)
    private String notes;

    public Long getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(Long assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Long getPlannedAt() {
        return plannedAt;
    }

    public void setPlannedAt(Long plannedAt) {
        this.plannedAt = plannedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
