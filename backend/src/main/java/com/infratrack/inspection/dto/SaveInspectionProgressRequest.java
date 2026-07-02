package com.infratrack.inspection.dto;

import com.infratrack.inspection.PhysicalCondition;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.util.List;

public class SaveInspectionProgressRequest {

    @Schema(description = "Observed physical condition (draft; does not complete the inspection)")
    private PhysicalCondition observedCondition;

    @Size(max = 4000)
    @Schema(description = "Draft observations (does not complete the inspection)")
    private String observations;

    @Schema(description = "Draft issue flag (does not complete the inspection)")
    private Boolean issueIdentified;

    @Valid
    @Schema(description = "Structured checklist answers to save progressively (draft)")
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

    public List<InspectionAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<InspectionAnswerRequest> answers) {
        this.answers = answers;
    }
}

