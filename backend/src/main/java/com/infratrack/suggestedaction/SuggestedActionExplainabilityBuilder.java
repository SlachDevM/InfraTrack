package com.infratrack.suggestedaction;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import com.infratrack.ruleevaluation.RuleEvaluationResult;
import com.infratrack.suggestedaction.dto.SuggestedActionExplanationResponse;

/**
 * Builds human-readable explanations from persisted rule evaluation snapshots (A3.5).
 */
public final class SuggestedActionExplainabilityBuilder {

    private SuggestedActionExplainabilityBuilder() {
    }

    public static SuggestedActionExplanationResponse fromResult(RuleEvaluationResult result) {
        SuggestedActionExplanationResponse explanation = new SuggestedActionExplanationResponse();
        explanation.setMatchedRuleCode(result.getRuleCodeSnapshot());
        explanation.setMatchedRuleName(result.getRuleNameSnapshot());
        explanation.setConditionDescription(formatCondition(
                result.getRuleCodeSnapshot(),
                result.getOperatorSnapshot(),
                result.getComparisonValueSnapshot()));
        explanation.setActualValue(result.getActualValueSnapshot());
        explanation.setConfiguredActionDescription(formatConfiguredAction(
                result.getActionTypeSnapshot(),
                result.getActionPayloadSnapshot()));
        explanation.setSummaryText(buildSummary(explanation));
        return explanation;
    }

    static String formatCondition(String ruleCode, DecisionRuleOperator operator, String comparisonValue) {
        if (operator == null) {
            return ruleCode;
        }
        return switch (operator) {
            case IS_TRUE, IS_FALSE -> ruleCode + " " + formatOperator(operator);
            default -> ruleCode + " " + formatOperator(operator) + " " + (comparisonValue == null ? "" : comparisonValue).trim();
        };
    }

    static String formatConfiguredAction(DecisionRuleActionType actionType, String actionPayload) {
        String severity = SuggestedActionPayloadInterpreter.interpret(
                actionType,
                "",
                "",
                actionPayload).severity();
        return switch (actionType) {
            case SUGGEST_ISSUE -> severity == null
                    ? "Suggest Issue."
                    : "Suggest " + severity + " severity Issue.";
            case SUGGEST_SEVERITY -> severity == null
                    ? "Suggest severity classification."
                    : "Suggest " + severity + " severity.";
            case SUGGEST_OPERATIONAL_DECISION -> "Suggest Operational Decision.";
            case FLAG_FOR_REVIEW -> "Flag for review.";
        };
    }

    private static String formatOperator(DecisionRuleOperator operator) {
        return switch (operator) {
            case IS_TRUE -> "is true";
            case IS_FALSE -> "is false";
            case GREATER_THAN -> ">";
            case GREATER_THAN_OR_EQUAL -> ">=";
            case LESS_THAN -> "<";
            case LESS_THAN_OR_EQUAL -> "<=";
            case EQUALS -> "=";
            case NOT_EQUALS -> "!=";
            case CONTAINS -> "contains";
            case STARTS_WITH -> "starts with";
            case ENDS_WITH -> "ends with";
        };
    }

    private static String buildSummary(SuggestedActionExplanationResponse explanation) {
        return "Matched rule "
                + explanation.getMatchedRuleCode()
                + " with condition "
                + explanation.getConditionDescription()
                + " and actual value "
                + (explanation.getActualValue() == null ? "not recorded" : explanation.getActualValue())
                + ".";
    }
}
