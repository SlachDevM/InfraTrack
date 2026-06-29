package com.infratrack.preventivemaintenance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.infratrack.exception.BusinessValidationException;

/**
 * Validates trigger configuration JSON according to trigger type (V2 Phase B Sprint B1.2).
 */
public final class TriggerDefinitionValidator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private TriggerDefinitionValidator() {
    }

    public static String validateAndNormalize(PlanTriggerType triggerType, String configurationJson) {
        if (configurationJson == null || configurationJson.isBlank()) {
            throw new BusinessValidationException("Trigger configuration is required");
        }
        JsonNode root = parseJson(configurationJson.trim());
        return switch (triggerType) {
            case TIME -> validateTime(root);
            case METER -> validateMeter(root);
            case EVENT -> validateEvent(root);
        };
    }

    private static JsonNode parseJson(String configurationJson) {
        try {
            return OBJECT_MAPPER.readTree(configurationJson);
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException("Trigger configuration must be valid JSON");
        }
    }

    private static String validateTime(JsonNode root) {
        if (!root.isObject()) {
            throw new BusinessValidationException("TIME trigger configuration must be a JSON object");
        }
        JsonNode everyNode = root.get("every");
        if (everyNode == null || !everyNode.isInt() || everyNode.asInt() <= 0) {
            throw new BusinessValidationException("Trigger every must be greater than zero");
        }
        JsonNode unitNode = root.get("unit");
        if (unitNode == null || unitNode.isNull() || unitNode.asText().isBlank()) {
            throw new BusinessValidationException("Trigger unit is required");
        }
        PlanTimeUnit unit = parseEnum(unitNode.asText(), PlanTimeUnit.class, "Unsupported trigger unit");
        ObjectNode normalized = OBJECT_MAPPER.createObjectNode();
        normalized.put("every", everyNode.asInt());
        normalized.put("unit", unit.name());
        return writeJson(normalized);
    }

    private static String validateMeter(JsonNode root) {
        if (!root.isObject()) {
            throw new BusinessValidationException("METER trigger configuration must be a JSON object");
        }
        JsonNode meterNode = root.get("meter");
        if (meterNode == null || meterNode.isNull() || meterNode.asText().isBlank()) {
            throw new BusinessValidationException("Trigger meter is required");
        }
        PlanMeterType meter = parseEnum(meterNode.asText(), PlanMeterType.class, "Unsupported trigger meter");
        JsonNode everyNode = root.get("every");
        if (everyNode == null || !everyNode.isInt() || everyNode.asInt() <= 0) {
            throw new BusinessValidationException("Trigger every must be greater than zero");
        }
        ObjectNode normalized = OBJECT_MAPPER.createObjectNode();
        normalized.put("meter", meter.name());
        normalized.put("every", everyNode.asInt());
        return writeJson(normalized);
    }

    private static String validateEvent(JsonNode root) {
        if (!root.isObject()) {
            throw new BusinessValidationException("EVENT trigger configuration must be a JSON object");
        }
        JsonNode eventNode = root.get("event");
        if (eventNode == null || eventNode.isNull() || eventNode.asText().isBlank()) {
            throw new BusinessValidationException("Trigger event is required");
        }
        PlanEventType event = parseEnum(eventNode.asText(), PlanEventType.class, "Unsupported trigger event");
        ObjectNode normalized = OBJECT_MAPPER.createObjectNode();
        normalized.put("event", event.name());
        return writeJson(normalized);
    }

    private static <E extends Enum<E>> E parseEnum(String value, Class<E> enumType, String errorMessage) {
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BusinessValidationException(errorMessage);
        }
    }

    private static String writeJson(JsonNode node) {
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException("Trigger configuration must be valid JSON");
        }
    }
}
