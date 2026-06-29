package com.infratrack.inspection.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class InspectionAnswerRequest {

    @NotNull
    @Positive
    private Long questionId;

    private Boolean booleanValue;

    @Size(max = 4000)
    private String textValue;

    private BigDecimal numberValue;

    @Size(max = 100)
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
