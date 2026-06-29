package com.infratrack.suggestedaction;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SuggestionConfidenceCalculatorTest {

    @Test
    void calculate_shouldReturnLowForSingleMatchedRule() {
        assertThat(SuggestionConfidenceCalculator.calculate(1, DecisionRuleActionType.SUGGEST_ISSUE))
                .isEqualTo(SuggestionConfidence.LOW);
        assertThat(SuggestionConfidenceCalculator.calculate(1, DecisionRuleActionType.FLAG_FOR_REVIEW))
                .isEqualTo(SuggestionConfidence.LOW);
    }

    @Test
    void calculate_shouldReturnMediumForTwoMatchedRules() {
        assertThat(SuggestionConfidenceCalculator.calculate(2, DecisionRuleActionType.SUGGEST_ISSUE))
                .isEqualTo(SuggestionConfidence.MEDIUM);
    }

    @Test
    void calculate_shouldReturnHighForThreeOrMoreMatchedRules() {
        assertThat(SuggestionConfidenceCalculator.calculate(3, DecisionRuleActionType.SUGGEST_ISSUE))
                .isEqualTo(SuggestionConfidence.HIGH);
    }
}
