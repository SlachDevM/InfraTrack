package com.infratrack.validation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessValidationException;

/**
 * Shared JSON parsing/writing helpers for validation-level payloads.
 *
 * Explicit by design: no reflection, no mapping framework.
 */
public final class JsonPayloadSupport {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private JsonPayloadSupport() {
    }

    public static JsonNode parse(String json, String invalidMessage) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException(invalidMessage);
        }
    }

    public static JsonNode parseRequired(String json, String requiredMessage, String invalidMessage) {
        if (json == null || json.isBlank()) {
            throw new BusinessValidationException(requiredMessage);
        }
        return parse(json.trim(), invalidMessage);
    }

    public static void validateOptional(String json, String invalidMessage) {
        if (json == null || json.isBlank()) {
            return;
        }
        parse(json.trim(), invalidMessage);
    }

    public static String write(JsonNode node, String invalidMessage) {
        try {
            return OBJECT_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException(invalidMessage);
        }
    }
}

