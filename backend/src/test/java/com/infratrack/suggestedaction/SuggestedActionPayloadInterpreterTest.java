package com.infratrack.suggestedaction;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SuggestedActionPayloadInterpreterTest {

    @Test
    void interpret_shouldParseTitleMessageAndSeverityFromPayload() {
        String payload = """
                {
                  "title": "High temperature detected",
                  "message": "Temperature exceeds safe operating range.",
                  "severity": "HIGH",
                  "extraField": "ignored"
                }
                """;

        SuggestedActionPayloadInterpreter.InterpretedPayload result =
                SuggestedActionPayloadInterpreter.interpret(
                        DecisionRuleActionType.SUGGEST_ISSUE,
                        "High temperature",
                        "HIGH_TEMP",
                        payload);

        assertThat(result.title()).isEqualTo("High temperature detected");
        assertThat(result.message()).isEqualTo("Temperature exceeds safe operating range.");
        assertThat(result.severity()).isEqualTo("HIGH");
        assertThat(result.originalPayload()).isEqualTo(payload);
    }

    @Test
    void interpret_shouldGenerateFallbackTextWhenFieldsMissing() {
        SuggestedActionPayloadInterpreter.InterpretedPayload result =
                SuggestedActionPayloadInterpreter.interpret(
                        DecisionRuleActionType.SUGGEST_ISSUE,
                        "High temperature",
                        "HIGH_TEMP",
                        "{}");

        assertThat(result.title()).isEqualTo("Suggested Issue — High temperature");
        assertThat(result.message()).contains("HIGH_TEMP");
        assertThat(result.severity()).isNull();
    }

    @Test
    void interpret_shouldTolerateInvalidJson() {
        SuggestedActionPayloadInterpreter.InterpretedPayload result =
                SuggestedActionPayloadInterpreter.interpret(
                        DecisionRuleActionType.FLAG_FOR_REVIEW,
                        "Leak detected",
                        "LEAK",
                        "not-json");

        assertThat(result.title()).isEqualTo("Flag for Review — Leak detected");
        assertThat(result.message()).contains("LEAK");
        assertThat(result.originalPayload()).isEqualTo("not-json");
    }
}
