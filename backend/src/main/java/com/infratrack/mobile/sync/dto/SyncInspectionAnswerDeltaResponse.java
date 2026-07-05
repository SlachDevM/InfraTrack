package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

public class SyncInspectionAnswerDeltaResponse {

    @Schema(description = "Checklist question identifier")
    private Long questionId;

    private Boolean booleanValue;

    private String textValue;

    private BigDecimal numberValue;

    @Schema(description = "Selected choice business code when applicable")
    private String choiceCodeValue;

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(Boolean booleanValue) {
        this.booleanValue = booleanValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public void setTextValue(String textValue) {
        this.textValue = textValue;
    }

    public BigDecimal getNumberValue() {
        return numberValue;
    }

    public void setNumberValue(BigDecimal numberValue) {
        this.numberValue = numberValue;
    }

    public String getChoiceCodeValue() {
        return choiceCodeValue;
    }

    public void setChoiceCodeValue(String choiceCodeValue) {
        this.choiceCodeValue = choiceCodeValue;
    }
}
