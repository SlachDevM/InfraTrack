package com.infratrack.mobile.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.unitofmeasure.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.List;

public class MobileQuestionResponse {

    private Long id;
    private String code;
    private String questionText;
    private String helpText;
    private InspectionTemplateQuestionType type;
    private boolean required;
    private Integer displayOrder;
    private String unitSymbol;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer decimalPlaces;
    private List<MobileChoiceResponse> choices;

    public static MobileQuestionResponse from(
            InspectionTemplateQuestion question,
            List<MobileChoiceResponse> choices) {
        MobileQuestionResponse response = new MobileQuestionResponse();
        response.id = question.getId();
        response.code = question.getCode();
        response.questionText = question.getQuestionText();
        response.helpText = question.getHelpText();
        response.type = question.getQuestionType();
        response.required = question.isRequired();
        response.displayOrder = question.getDisplayOrder();
        response.minValue = question.getMinValue();
        response.maxValue = question.getMaxValue();
        response.decimalPlaces = question.getDecimalPlaces();
        UnitOfMeasure unitOfMeasure = question.getUnitOfMeasure();
        if (unitOfMeasure != null) {
            response.unitSymbol = unitOfMeasure.getSymbol();
        } else if (question.getUnit() != null) {
            response.unitSymbol = question.getUnit();
        }
        response.choices = choices;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getHelpText() {
        return helpText;
    }

    public InspectionTemplateQuestionType getType() {
        return type;
    }

    public boolean isRequired() {
        return required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public String getUnitSymbol() {
        return unitSymbol;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public List<MobileChoiceResponse> getChoices() {
        return choices;
    }
}
