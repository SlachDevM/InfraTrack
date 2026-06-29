package com.infratrack.preventivemaintenance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessValidationException;

final class TriggerDefinitionFactory {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TriggerDefinitionFactory() {
    }

    static TriggerDefinition from(PlanBusinessTrigger trigger) {
        String normalizedJson = TriggerDefinitionValidator.validateAndNormalize(
                trigger.getTriggerType(),
                trigger.getConfigurationJson());
        JsonNode root = parseJson(normalizedJson);
        return switch (trigger.getTriggerType()) {
            case TIME -> TimeTriggerDefinition.fromJson(root);
            case METER -> MeterTriggerDefinition.fromJson(root);
            case EVENT -> EventTriggerDefinition.fromJson(root);
        };
    }

    static TriggerDefinition from(PlanTriggerType triggerType, String configurationJson) {
        String normalizedJson = TriggerDefinitionValidator.validateAndNormalize(triggerType, configurationJson);
        JsonNode root = parseJson(normalizedJson);
        return switch (triggerType) {
            case TIME -> TimeTriggerDefinition.fromJson(root);
            case METER -> MeterTriggerDefinition.fromJson(root);
            case EVENT -> EventTriggerDefinition.fromJson(root);
        };
    }

    private static JsonNode parseJson(String configurationJson) {
        try {
            return OBJECT_MAPPER.readTree(configurationJson);
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException("Trigger configuration must be valid JSON");
        }
    }
}
