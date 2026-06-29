package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.unitofmeasure.QuantityType;
import com.infratrack.unitofmeasure.UnitOfMeasure;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class InspectionTemplateQuestionResponse {

    private Long id;
    private Long inspectionTemplateId;
    private String code;
    private String questionText;
    private String helpText;
    private InspectionTemplateQuestionType questionType;
    private boolean required;
    private Integer displayOrder;
    private boolean active;
    private Long unitOfMeasureId;
    private String unitCode;
    private String unitSymbol;
    private String unitName;
    private QuantityType unitQuantityType;
    private String unit;
    private BigDecimal minValue;
    private BigDecimal maxValue;
    private Integer decimalPlaces;
    private List<InspectionTemplateQuestionChoiceResponse> choices;
    private Long createdAt;
    private Long updatedAt;

    public static InspectionTemplateQuestionResponse from(InspectionTemplateQuestion question) {
        return from(question, Collections.emptyList());
    }

    public static InspectionTemplateQuestionResponse from(
            InspectionTemplateQuestion question,
            List<InspectionTemplateQuestionChoiceResponse> choices) {
        InspectionTemplateQuestionResponse response = new InspectionTemplateQuestionResponse();
        response.id = question.getId();
        response.inspectionTemplateId = question.getInspectionTemplate().getId();
        response.code = question.getCode();
        response.questionText = question.getQuestionText();
        response.helpText = question.getHelpText();
        response.questionType = question.getQuestionType();
        response.required = question.isRequired();
        response.displayOrder = question.getDisplayOrder();
        response.active = question.isActive();
        applyUnitDetails(response, question);
        response.minValue = question.getMinValue();
        response.maxValue = question.getMaxValue();
        response.decimalPlaces = question.getDecimalPlaces();
        response.choices = choices;
        response.createdAt = question.getCreatedAt();
        response.updatedAt = question.getUpdatedAt();
        return response;
    }

    private static void applyUnitDetails(
            InspectionTemplateQuestionResponse response,
            InspectionTemplateQuestion question) {
        UnitOfMeasure unitOfMeasure = question.getUnitOfMeasure();
        if (unitOfMeasure != null) {
            response.unitOfMeasureId = unitOfMeasure.getId();
            response.unitCode = unitOfMeasure.getCode();
            response.unitSymbol = unitOfMeasure.getSymbol();
            response.unitName = unitOfMeasure.getName();
            response.unitQuantityType = unitOfMeasure.getQuantityType();
            response.unit = unitOfMeasure.getSymbol();
            return;
        }
        response.unit = question.getUnit();
        if (question.getUnit() != null) {
            response.unitSymbol = question.getUnit();
        }
    }

    public Long getId() {
        return id;
    }

    public Long getInspectionTemplateId() {
        return inspectionTemplateId;
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

    public InspectionTemplateQuestionType getQuestionType() {
        return questionType;
    }

    public boolean isRequired() {
        return required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public Long getUnitOfMeasureId() {
        return unitOfMeasureId;
    }

    public String getUnitCode() {
        return unitCode;
    }

    public String getUnitSymbol() {
        return unitSymbol;
    }

    public String getUnitName() {
        return unitName;
    }

    public QuantityType getUnitQuantityType() {
        return unitQuantityType;
    }

    public String getUnit() {
        return unit;
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

    public List<InspectionTemplateQuestionChoiceResponse> getChoices() {
        return choices;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
