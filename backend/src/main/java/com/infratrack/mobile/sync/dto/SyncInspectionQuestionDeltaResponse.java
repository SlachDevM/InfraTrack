package com.infratrack.mobile.sync.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.unitofmeasure.UnitOfMeasure;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class SyncInspectionQuestionDeltaResponse {

    @Schema(description = "Checklist question identifier")
    private Long questionId;

    private String code;

    private String label;

    private InspectionTemplateQuestionType questionType;

    private boolean required;

    private Integer displayOrder;

    private String helpText;

    private String unit;

    private String unitLabel;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    private Integer decimalPlaces;

    private List<SyncInspectionChoiceDeltaResponse> choices = new ArrayList<>();

    public static SyncInspectionQuestionDeltaResponse from(
            InspectionTemplateQuestion question,
            List<SyncInspectionChoiceDeltaResponse> choices) {
        SyncInspectionQuestionDeltaResponse response = new SyncInspectionQuestionDeltaResponse();
        response.setQuestionId(question.getId());
        response.setCode(question.getCode());
        response.setLabel(question.getQuestionText());
        response.setQuestionType(question.getQuestionType());
        response.setRequired(question.isRequired());
        response.setDisplayOrder(question.getDisplayOrder());
        response.setHelpText(question.getHelpText());
        response.setMinValue(question.getMinValue());
        response.setMaxValue(question.getMaxValue());
        response.setDecimalPlaces(question.getDecimalPlaces());
        UnitOfMeasure unitOfMeasure = question.getUnitOfMeasure();
        if (unitOfMeasure != null) {
            response.setUnitLabel(unitOfMeasure.getSymbol());
        } else if (question.getUnit() != null) {
            response.setUnitLabel(question.getUnit());
        }
        response.setUnit(question.getUnit());
        response.setChoices(choices != null ? choices : List.of());
        return response;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public InspectionTemplateQuestionType getQuestionType() {
        return questionType;
    }

    public void setQuestionType(InspectionTemplateQuestionType questionType) {
        this.questionType = questionType;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public String getHelpText() {
        return helpText;
    }

    public void setHelpText(String helpText) {
        this.helpText = helpText;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitLabel() {
        return unitLabel;
    }

    public void setUnitLabel(String unitLabel) {
        this.unitLabel = unitLabel;
    }

    public BigDecimal getMinValue() {
        return minValue;
    }

    public void setMinValue(BigDecimal minValue) {
        this.minValue = minValue;
    }

    public BigDecimal getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(BigDecimal maxValue) {
        this.maxValue = maxValue;
    }

    public Integer getDecimalPlaces() {
        return decimalPlaces;
    }

    public void setDecimalPlaces(Integer decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    public List<SyncInspectionChoiceDeltaResponse> getChoices() {
        return choices;
    }

    public void setChoices(List<SyncInspectionChoiceDeltaResponse> choices) {
        this.choices = choices != null ? choices : new ArrayList<>();
    }
}
