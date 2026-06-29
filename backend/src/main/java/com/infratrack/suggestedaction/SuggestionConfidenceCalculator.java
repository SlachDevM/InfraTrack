package com.infratrack.suggestedaction;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;

/**
 * Deterministic confidence calculation for suggested actions (A3.5).
 */
public final class SuggestionConfidenceCalculator {

    private SuggestionConfidenceCalculator() {
    }

    public static SuggestionConfidence calculate(int matchedRuleCount, DecisionRuleActionType actionType) {
        if (matchedRuleCount >= 3) {
            return SuggestionConfidence.HIGH;
        }
        if (matchedRuleCount == 2) {
            return SuggestionConfidence.MEDIUM;
        }
        return SuggestionConfidence.LOW;
    }
}
