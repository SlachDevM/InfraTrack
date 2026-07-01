package com.infratrack.inspection.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;

import java.util.List;

public class SaveInspectionAnswersRequest {

    @Valid
    @Schema(description = "Structured checklist answers to save progressively")
    private List<InspectionAnswerRequest> answers;

    public List<InspectionAnswerRequest> getAnswers() {
        return answers;
    }

    public void setAnswers(List<InspectionAnswerRequest> answers) {
        this.answers = answers;
    }
}
