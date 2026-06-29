package com.infratrack.suggestedaction;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import com.infratrack.ruleevaluation.RuleEvaluationResult;
import com.infratrack.suggestedaction.dto.SuggestedActionExplanationResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SuggestedActionExplainabilityBuilderTest {

    @Test
    void fromResult_shouldBuildExplanationFromSnapshots() {
        RuleEvaluationResult result = new RuleEvaluationResult(
                1L,
                "HIGH_TEMP",
                "High temperature",
                DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN,
                "90",
                "95",
                true,
                DecisionRuleActionType.SUGGEST_ISSUE,
                "{\"severity\":\"HIGH\"}",
                10,
                1L,
                1L);

        SuggestedActionExplanationResponse explanation =
                SuggestedActionExplainabilityBuilder.fromResult(result);

        assertThat(explanation.getMatchedRuleCode()).isEqualTo("HIGH_TEMP");
        assertThat(explanation.getConditionDescription()).isEqualTo("HIGH_TEMP > 90");
        assertThat(explanation.getActualValue()).isEqualTo("95");
        assertThat(explanation.getConfiguredActionDescription()).contains("HIGH");
    }
}
