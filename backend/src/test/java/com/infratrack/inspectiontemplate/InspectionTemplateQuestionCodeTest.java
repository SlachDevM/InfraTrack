package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class InspectionTemplateQuestionCodeTest {

    @Test
    void suggestFromQuestionText_shouldDeriveUppercaseSnakeCaseCode() {
        assertThat(InspectionTemplateQuestionCode.suggestFromQuestionText("Is abnormal vibration present?"))
                .isEqualTo("IS_ABNORMAL_VIBRATION_PRESENT");
    }

    @Test
    void suggestFromQuestionText_shouldPrefixNonLetterStart() {
        assertThat(InspectionTemplateQuestionCode.suggestFromQuestionText("123 vibration check"))
                .isEqualTo("Q_123_VIBRATION_CHECK");
    }

    @Test
    void normalize_shouldUppercaseValidCode() {
        assertThat(InspectionTemplateQuestionCode.normalize("  leak  "))
                .isEqualTo("LEAK");
    }

    @Test
    void validateFormat_shouldAcceptValidCode() {
        assertThatCode(() -> InspectionTemplateQuestionCode.validateFormat("HIGH_TEMPERATURE"))
                .doesNotThrowAnyException();
    }

    @Test
    void validateFormat_shouldRejectInvalidRegex() {
        assertThatThrownBy(() -> InspectionTemplateQuestionCode.validateFormat("lowercase"))
                .isInstanceOf(BusinessValidationException.class);
        assertThatThrownBy(() -> InspectionTemplateQuestionCode.validateFormat("1_STARTS_WITH_DIGIT"))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void normalize_shouldRejectBlankCode() {
        assertThatThrownBy(() -> InspectionTemplateQuestionCode.normalize("   "))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Question code is required");
    }
}
