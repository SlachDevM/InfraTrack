package com.infratrack.inspection.dto;

import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerQuestionTypeSnapshot;

import java.math.BigDecimal;

public class InspectionAnswerResponse {

    private Long id;
    private Long questionId;
    private String questionCodeSnapshot;
    private String questionTextSnapshot;
    private InspectionAnswerQuestionTypeSnapshot questionTypeSnapshot;
    private Boolean booleanValue;
    private String textValue;
    private BigDecimal numberValue;
    private String choiceCodeValue;
    private String choiceLabelSnapshot;
    private String numberUnitSnapshot;
    private BigDecimal numberMinSnapshot;
    private BigDecimal numberMaxSnapshot;
    private Integer decimalPlacesSnapshot;
    private String unitCodeSnapshot;
    private String unitSymbolSnapshot;
    private String unitNameSnapshot;
    private Integer questionVersionSnapshot;
    private Long createdAt;
    private Long updatedAt;

    public static InspectionAnswerResponse from(InspectionAnswer answer) {
        InspectionAnswerResponse response = new InspectionAnswerResponse();
        response.id = answer.getId();
        response.questionId = answer.getQuestion().getId();
        response.questionCodeSnapshot = answer.getQuestionCodeSnapshot();
        response.questionTextSnapshot = answer.getQuestionTextSnapshot();
        response.questionTypeSnapshot = answer.getQuestionTypeSnapshot();
        response.booleanValue = answer.getBooleanValue();
        response.textValue = answer.getTextValue();
        response.numberValue = answer.getNumberValue();
        response.choiceCodeValue = answer.getChoiceCodeValue();
        response.choiceLabelSnapshot = answer.getChoiceLabelSnapshot();
        response.numberUnitSnapshot = answer.getNumberUnitSnapshot();
        response.numberMinSnapshot = answer.getNumberMinSnapshot();
        response.numberMaxSnapshot = answer.getNumberMaxSnapshot();
        response.decimalPlacesSnapshot = answer.getDecimalPlacesSnapshot();
        response.unitCodeSnapshot = answer.getUnitCodeSnapshot();
        response.unitSymbolSnapshot = answer.getUnitSymbolSnapshot() != null
                ? answer.getUnitSymbolSnapshot()
                : answer.getNumberUnitSnapshot();
        response.unitNameSnapshot = answer.getUnitNameSnapshot();
        response.questionVersionSnapshot = answer.getQuestionVersionSnapshot();
        response.createdAt = answer.getCreatedAt();
        response.updatedAt = answer.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getQuestionCodeSnapshot() {
        return questionCodeSnapshot;
    }

    public String getQuestionTextSnapshot() {
        return questionTextSnapshot;
    }

    public InspectionAnswerQuestionTypeSnapshot getQuestionTypeSnapshot() {
        return questionTypeSnapshot;
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

    public String getChoiceLabelSnapshot() {
        return choiceLabelSnapshot;
    }

    public String getNumberUnitSnapshot() {
        return numberUnitSnapshot;
    }

    public BigDecimal getNumberMinSnapshot() {
        return numberMinSnapshot;
    }

    public BigDecimal getNumberMaxSnapshot() {
        return numberMaxSnapshot;
    }

    public Integer getDecimalPlacesSnapshot() {
        return decimalPlacesSnapshot;
    }

    public String getUnitCodeSnapshot() {
        return unitCodeSnapshot;
    }

    public String getUnitSymbolSnapshot() {
        return unitSymbolSnapshot;
    }

    public String getUnitNameSnapshot() {
        return unitNameSnapshot;
    }

    public Integer getQuestionVersionSnapshot() {
        return questionVersionSnapshot;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
