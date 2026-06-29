package com.infratrack.inspection.dto;

import com.infratrack.inspection.PhysicalCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class CompleteInspectionRequest {

    @NotNull
    @Schema(description = "Observed physical condition at completion")
    private PhysicalCondition observedCondition;

    @NotBlank
    @Size(max = 4000)
    private String observations;

    @Schema(description = "Whether a new issue was identified during inspection")
    private Boolean issueIdentified;

    @NotNull
    private LocalDateTime completedAt;

    @Valid
    @Schema(description = "Structured checklist answers for templated inspections")
    private List<InspectionAnswerRequest> answers;

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

    public List<InspectionAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<InspectionAnswerRequest> answers) {
        this.answers = answers;
    }
}
