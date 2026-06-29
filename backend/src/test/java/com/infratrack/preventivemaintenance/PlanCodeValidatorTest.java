package com.infratrack.preventivemaintenance;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.*;

class PlanCodeValidatorTest {

    @Test
    void validateAndNormalize_shouldAcceptValidCode() {
        assertThat(PlanCodeValidator.validateAndNormalize("PUMP_MONTHLY")).isEqualTo("PUMP_MONTHLY");
    }

    @Test
    void validateAndNormalize_shouldNormalizeCaseAndSeparators() {
        assertThat(PlanCodeValidator.validateAndNormalize("pump-monthly")).isEqualTo("PUMP_MONTHLY");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   ", "1PUMP", "PUMP__MONTHLY", "_PUMP"})
    void validateAndNormalize_shouldRejectInvalidCode(String planCode) {
        assertThatThrownBy(() -> PlanCodeValidator.validateAndNormalize(planCode))
                .isInstanceOf(BusinessValidationException.class);
    }
}
