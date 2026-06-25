package com.infratrack.inspection.dto;

import com.infratrack.inspection.PhysicalCondition;

import java.time.LocalDateTime;

public class CompleteInspectionRequest {

    private PhysicalCondition observedCondition;
    private String observations;
    private Boolean issueIdentified;
    private LocalDateTime completedAt;

    public PhysicalCondition getObservedCondition() {
        return observedCondition;
    }

    public void setObservedCondition(PhysicalCondition observedCondition) {
        this.observedCondition = observedCondition;
    }

    public String getObservations() {
        return observations;
    }

    public void setObservations(String observations) {
        this.observations = observations;
    }

    public Boolean getIssueIdentified() {
        return issueIdentified;
    }

    public void setIssueIdentified(Boolean issueIdentified) {
        this.issueIdentified = issueIdentified;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
