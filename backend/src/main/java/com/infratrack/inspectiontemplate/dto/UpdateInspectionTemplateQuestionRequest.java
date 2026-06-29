package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class UpdateInspectionTemplateQuestionRequest {

    @NotBlank
    @Size(max = 2000)
    private String questionText;

    @Size(max = 4000)
    private String helpText;

    @NotNull
    private InspectionTemplateQuestionType questionType;

    private Boolean required;

    @Positive
    private Long unitOfMeasureId;

    private BigDecimal minValue;

    private BigDecimal maxValue;

    @Min(0)
    @Max(6)
    private Integer decimalPlaces;

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

    public Long getUnitOfMeasureId() {
        return unitOfMeasureId;
    }

    public void setUnitOfMeasureId(Long unitOfMeasureId) {
        this.unitOfMeasureId = unitOfMeasureId;
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
}
