package com.infratrack.ruleevaluation.dto;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import com.infratrack.ruleevaluation.RuleEvaluationResult;

public class RuleEvaluationResultResponse {

    private Long id;
    private Long ruleIdSnapshot;
    private String ruleCodeSnapshot;
    private String ruleNameSnapshot;
    private DecisionRuleConditionType conditionTypeSnapshot;
    private DecisionRuleOperator operatorSnapshot;
    private String comparisonValueSnapshot;
    private String actualValueSnapshot;
    private boolean matched;
    private DecisionRuleActionType actionTypeSnapshot;
    private String actionPayloadSnapshot;
    private int prioritySnapshot;
    private Long evaluatedAt;
    private long evaluationDurationMs;

    public static RuleEvaluationResultResponse from(RuleEvaluationResult result) {
        RuleEvaluationResultResponse response = new RuleEvaluationResultResponse();
        response.id = result.getId();
        response.ruleIdSnapshot = result.getRuleIdSnapshot();
        response.ruleCodeSnapshot = result.getRuleCodeSnapshot();
        response.ruleNameSnapshot = result.getRuleNameSnapshot();
        response.conditionTypeSnapshot = result.getConditionTypeSnapshot();
        response.operatorSnapshot = result.getOperatorSnapshot();
        response.comparisonValueSnapshot = result.getComparisonValueSnapshot();
        response.actualValueSnapshot = result.getActualValueSnapshot();
        response.matched = result.isMatched();
        response.actionTypeSnapshot = result.getActionTypeSnapshot();
        response.actionPayloadSnapshot = result.getActionPayloadSnapshot();
        response.prioritySnapshot = result.getPrioritySnapshot();
        response.evaluatedAt = result.getEvaluatedAt();
        response.evaluationDurationMs = result.getEvaluationDurationMs();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getRuleIdSnapshot() {
        return ruleIdSnapshot;
    }

    public String getRuleCodeSnapshot() {
        return ruleCodeSnapshot;
    }

    public String getRuleNameSnapshot() {
        return ruleNameSnapshot;
    }

    public DecisionRuleConditionType getConditionTypeSnapshot() {
        return conditionTypeSnapshot;
    }

    public DecisionRuleOperator getOperatorSnapshot() {
        return operatorSnapshot;
    }

    public String getComparisonValueSnapshot() {
        return comparisonValueSnapshot;
    }

    public String getActualValueSnapshot() {
        return actualValueSnapshot;
    }

    public boolean isMatched() {
        return matched;
    }

    public DecisionRuleActionType getActionTypeSnapshot() {
        return actionTypeSnapshot;
    }

    public String getActionPayloadSnapshot() {
        return actionPayloadSnapshot;
    }

    public int getPrioritySnapshot() {
        return prioritySnapshot;
    }

    public Long getEvaluatedAt() {
        return evaluatedAt;
    }

    public long getEvaluationDurationMs() {
        return evaluationDurationMs;
    }
}
