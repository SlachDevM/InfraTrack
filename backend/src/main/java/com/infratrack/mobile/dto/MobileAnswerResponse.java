package com.infratrack.mobile.dto;

import com.infratrack.inspection.InspectionAnswer;

import java.math.BigDecimal;

public class MobileAnswerResponse {

    private Long questionId;
    private Boolean booleanValue;
    private String textValue;
    private BigDecimal numberValue;
    private String choiceCodeValue;

    public static MobileAnswerResponse from(InspectionAnswer answer) {
        MobileAnswerResponse response = new MobileAnswerResponse();
        response.questionId = answer.getQuestion().getId();
        response.booleanValue = answer.getBooleanValue();
        response.textValue = answer.getTextValue();
        response.numberValue = answer.getNumberValue();
        response.choiceCodeValue = answer.getChoiceCodeValue();
        return response;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public BigDecimal getNumberValue() {
        return numberValue;
    }

    public String getChoiceCodeValue() {
        return choiceCodeValue;
    }
}
