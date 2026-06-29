package com.infratrack.inspectiontemplate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UpdateInspectionTemplateQuestionChoiceRequest {

    @NotBlank
    @Size(max = 500)
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
