package com.infratrack.preventivemaintenance;

import com.infratrack.exception.BusinessValidationException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TriggerDefinitionValidatorTest {

    @Test
    void validateTime_shouldAcceptValidConfiguration() {
        String json = TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");

        assertThat(json).isEqualTo("{\"every\":1,\"unit\":\"MONTH\"}");
    }

    @Test
    void validateTime_shouldRejectZeroEvery() {
        assertThatThrownBy(() -> TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.TIME,
                "{\"every\":0,\"unit\":\"MONTH\"}"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Trigger every must be greater than zero");
    }

    @Test
    void validateTime_shouldRejectUnsupportedUnit() {
        assertThatThrownBy(() -> TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MINUTE\"}"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Unsupported trigger unit");
    }

    @Test
    void validateTime_shouldRejectMissingUnit() {
        assertThatThrownBy(() -> TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.TIME,
                "{\"every\":1}"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Trigger unit is required");
    }

    @Test
    void validateMeter_shouldAcceptValidConfiguration() {
        String json = TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.METER,
                "{\"meter\":\"OPERATING_HOURS\",\"every\":250}");

        assertThat(json).isEqualTo("{\"meter\":\"OPERATING_HOURS\",\"every\":250}");
    }

    @Test
    void validateMeter_shouldRejectBlankMeter() {
        assertThatThrownBy(() -> TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.METER,
                "{\"meter\":\"\",\"every\":250}"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Trigger meter is required");
    }

    @Test
    void validateEvent_shouldAcceptValidConfiguration() {
        String json = TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.EVENT,
                "{\"event\":\"COMPLETION_REVIEW\"}");

        assertThat(json).isEqualTo("{\"event\":\"COMPLETION_REVIEW\"}");
    }

    @Test
    void validateEvent_shouldRejectUnknownEvent() {
        assertThatThrownBy(() -> TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.EVENT,
                "{\"event\":\"UNKNOWN\"}"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Unsupported trigger event");
    }

    @Test
    void validate_shouldRejectMalformedJson() {
        assertThatThrownBy(() -> TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.TIME,
                "not-json"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Trigger configuration must be valid JSON");
    }

    @Test
    void validate_shouldRejectMissingConfiguration() {
        assertThatThrownBy(() -> TriggerDefinitionValidator.validateAndNormalize(
                PlanTriggerType.TIME,
                "   "))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Trigger configuration is required");
    }
}
