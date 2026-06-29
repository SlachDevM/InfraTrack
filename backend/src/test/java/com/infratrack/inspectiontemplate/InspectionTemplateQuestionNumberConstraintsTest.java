package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

class InspectionTemplateQuestionNumberConstraintsTest {

    @Test
    void validateForQuestionType_shouldAcceptValidNumberConstraints() {
        assertThatCode(() -> InspectionTemplateQuestionNumberConstraints.validateForQuestionType(
                InspectionTemplateQuestionType.NUMBER,
                1L,
                new BigDecimal("0"),
                new BigDecimal("120"),
                1))
                .doesNotThrowAnyException();
    }

    @Test
    void validateForQuestionType_shouldRejectMinGreaterThanMax() {
        assertThatThrownBy(() -> InspectionTemplateQuestionNumberConstraints.validateForQuestionType(
                InspectionTemplateQuestionType.NUMBER,
                null,
                new BigDecimal("120"),
                new BigDecimal("0"),
                null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Minimum value cannot exceed maximum value");
    }

    @Test
    void validateForQuestionType_shouldRejectDecimalPlacesOutsideRange() {
        assertThatThrownBy(() -> InspectionTemplateQuestionNumberConstraints.validateForQuestionType(
                InspectionTemplateQuestionType.NUMBER,
                null,
                null,
                null,
                7))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Decimal places must be between 0 and 6");
    }

    @Test
    void validateForQuestionType_shouldRejectNumberConstraintsOnNonNumberQuestion() {
        assertThatThrownBy(() -> InspectionTemplateQuestionNumberConstraints.validateForQuestionType(
                InspectionTemplateQuestionType.BOOLEAN,
                1L,
                null,
                null,
                null))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Number constraints apply only to NUMBER checklist questions");
    }

    @Test
    void validateNumberAnswer_shouldRejectBelowMin() {
        InspectionTemplateQuestion question = numberQuestion(new BigDecimal("0"), new BigDecimal("120"), 1);

        assertThatThrownBy(() -> InspectionTemplateQuestionNumberConstraints.validateNumberAnswer(
                question, new BigDecimal("-1")))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answer for 'TEMPERATURE' is below the minimum allowed value");
    }

    @Test
    void validateNumberAnswer_shouldRejectAboveMax() {
        InspectionTemplateQuestion question = numberQuestion(new BigDecimal("0"), new BigDecimal("120"), 1);

        assertThatThrownBy(() -> InspectionTemplateQuestionNumberConstraints.validateNumberAnswer(
                question, new BigDecimal("121")))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answer for 'TEMPERATURE' exceeds the maximum allowed value");
    }

    @Test
    void validateNumberAnswer_shouldRejectTooManyDecimals() {
        InspectionTemplateQuestion question = numberQuestion(new BigDecimal("0"), new BigDecimal("120"), 1);

        assertThatThrownBy(() -> InspectionTemplateQuestionNumberConstraints.validateNumberAnswer(
                question, new BigDecimal("87.55")))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answer for 'TEMPERATURE' exceeds allowed decimal places");
    }

    private InspectionTemplateQuestion numberQuestion(
            BigDecimal minValue,
            BigDecimal maxValue,
            Integer decimalPlaces) {
        com.infratrack.assetcategory.AssetCategory category = new com.infratrack.assetcategory.AssetCategory("Pump");
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection", null, category, 1, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Temperature",
                "TEMPERATURE",
                null,
                InspectionTemplateQuestionType.NUMBER,
                true,
                1
        );
        question.setMinValue(minValue);
        question.setMaxValue(maxValue);
        question.setDecimalPlaces(decimalPlaces);
        return question;
    }
}
