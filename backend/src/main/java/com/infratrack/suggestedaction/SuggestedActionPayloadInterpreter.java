package com.infratrack.suggestedaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;

/**
 * Tolerant interpretation of decision rule action payloads for suggested actions (A3.4).
 */
public final class SuggestedActionPayloadInterpreter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SuggestedActionPayloadInterpreter() {
    }

    public static InterpretedPayload interpret(
            DecisionRuleActionType actionType,
            String ruleName,
            String ruleCode,
            String actionPayload) {
        String title = null;
        String message = null;
        String severity = null;

        if (actionPayload != null && !actionPayload.isBlank()) {
            try {
                JsonNode root = OBJECT_MAPPER.readTree(actionPayload);
                title = textOrNull(root.get("title"));
                message = textOrNull(root.get("message"));
                severity = textOrNull(root.get("severity"));
            } catch (Exception ignored) {
                // Unknown or invalid JSON — fall back to generated text.
            }
        }

        if (title == null || title.isBlank()) {
            title = defaultTitle(actionType, ruleName);
        }
        if (message == null || message.isBlank()) {
            message = defaultMessage(ruleName, ruleCode);
        }

        return new InterpretedPayload(title, message, severity, actionPayload);
    }

    private static String defaultTitle(DecisionRuleActionType actionType, String ruleName) {
        String prefix = switch (actionType) {
            case SUGGEST_ISSUE -> "Suggested Issue";
            case SUGGEST_SEVERITY -> "Suggested Severity";
            case SUGGEST_OPERATIONAL_DECISION -> "Suggested Operational Decision";
            case FLAG_FOR_REVIEW -> "Flag for Review";
        };
        return prefix + " — " + ruleName;
    }

    private static String defaultMessage(String ruleName, String ruleCode) {
        return "Rule '" + ruleName + "' (" + ruleCode + ") matched during inspection.";
    }

    private static String textOrNull(JsonNode node) {
        if (node == null || node.isNull()) {
            return null;
        }
        String value = node.asText();
        return value.isBlank() ? null : value;
    }

    public record InterpretedPayload(
            String title,
            String message,
            String severity,
            String originalPayload) {
    }
}
