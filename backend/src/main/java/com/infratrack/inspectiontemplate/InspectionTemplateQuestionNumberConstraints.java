package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;

import java.math.BigDecimal;

/**
 * Validates NUMBER-specific constraints on checklist questions (V2 Domain Engine A2.3.2 / A2.3.3).
 */
public final class InspectionTemplateQuestionNumberConstraints {

    private static final int MAX_DECIMAL_PLACES = 6;

    private InspectionTemplateQuestionNumberConstraints() {
    }

    public static void validateForQuestionType(
            InspectionTemplateQuestionType questionType,
            Long unitOfMeasureId,
            BigDecimal minValue,
            BigDecimal maxValue,
            Integer decimalPlaces) {
        boolean hasNumberConstraints = unitOfMeasureId != null
                || minValue != null
                || maxValue != null
                || decimalPlaces != null;

        if (questionType != InspectionTemplateQuestionType.NUMBER && hasNumberConstraints) {
            throw new BusinessValidationException(
                    "Number constraints apply only to NUMBER checklist questions");
        }
        if (questionType != InspectionTemplateQuestionType.NUMBER) {
            return;
        }

        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new BusinessValidationException("Minimum value cannot exceed maximum value");
        }
        if (decimalPlaces != null && (decimalPlaces < 0 || decimalPlaces > MAX_DECIMAL_PLACES)) {
            throw new BusinessValidationException(
                    "Decimal places must be between 0 and " + MAX_DECIMAL_PLACES);
        }
    }

    public static void validateNumberAnswer(
            InspectionTemplateQuestion question,
            BigDecimal value) {
        if (value == null) {
            throw new BusinessValidationException(
                    "Number checklist question '" + question.getCode() + "' requires a numeric answer");
        }
        if (question.getMinValue() != null && value.compareTo(question.getMinValue()) < 0) {
            throw new BusinessValidationException(
                    "Answer for '" + question.getCode() + "' is below the minimum allowed value");
        }
        if (question.getMaxValue() != null && value.compareTo(question.getMaxValue()) > 0) {
            throw new BusinessValidationException(
                    "Answer for '" + question.getCode() + "' exceeds the maximum allowed value");
        }
        if (question.getDecimalPlaces() != null) {
            int scale = Math.max(value.stripTrailingZeros().scale(), 0);
            if (scale > question.getDecimalPlaces()) {
                throw new BusinessValidationException(
                        "Answer for '" + question.getCode() + "' exceeds allowed decimal places");
            }
        }
    }
}
