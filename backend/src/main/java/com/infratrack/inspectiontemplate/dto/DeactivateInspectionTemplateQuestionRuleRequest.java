package com.infratrack.inspectiontemplate.dto;

import jakarta.validation.constraints.Size;

public class DeactivateInspectionTemplateQuestionRuleRequest {

    @Size(max = 2000)
    private String disabledReason;

    public String getDisabledReason() {
        return disabledReason;
    }

    public void setDisabledReason(String disabledReason) {
        this.disabledReason = disabledReason;
    }
}
