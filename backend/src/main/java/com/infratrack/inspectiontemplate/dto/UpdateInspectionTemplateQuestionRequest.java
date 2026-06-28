package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateInspectionTemplateQuestionRequest {

    @NotBlank
    @Size(max = 2000)
    private String questionText;

    @Size(max = 4000)
    private String helpText;

    @NotNull
    private InspectionTemplateQuestionType questionType;

    private Boolean required;

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public InspectionTemplateQuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(InspectionTemplateQuestionType questionType) {
        this.questionType = questionType;
    }

    public Boolean getRequired() {
        return required;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }
}
