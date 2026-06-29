package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;

public class DecisionRuleEvaluationResult {

    private Long ruleId;
    private String ruleCode;
    private String ruleName;
    private boolean matched;
    private DecisionRuleConditionType conditionType;
    private DecisionRuleOperator operator;
    private String comparisonValue;
    private String actualValue;
    private DecisionRuleActionType actionType;
    private String actionPayload;
    private int priority;
    private Long evaluatedAt;
    private long evaluationDurationMs;

    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public boolean isMatched() {
        return matched;
    }

    public void setMatched(boolean matched) {
        this.matched = matched;
    }

    public DecisionRuleConditionType getConditionType() {
        return conditionType;
    }

    public void setConditionType(DecisionRuleConditionType conditionType) {
        this.conditionType = conditionType;
    }

    public DecisionRuleOperator getOperator() {
        return operator;
    }

    public void setOperator(DecisionRuleOperator operator) {
        this.operator = operator;
    }

    public String getComparisonValue() {
        return comparisonValue;
    }

    public void setComparisonValue(String comparisonValue) {
        this.comparisonValue = comparisonValue;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public DecisionRuleActionType getActionType() {
        return actionType;
    }

    public void setActionType(DecisionRuleActionType actionType) {
        this.actionType = actionType;
    }

    public String getActionPayload() {
        return actionPayload;
    }

    public void setActionPayload(String actionPayload) {
        this.actionPayload = actionPayload;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public Long getEvaluatedAt() {
        return evaluatedAt;
    }

    public void setEvaluatedAt(Long evaluatedAt) {
        this.evaluatedAt = evaluatedAt;
    }

    public long getEvaluationDurationMs() {
        return evaluationDurationMs;
    }

    public void setEvaluationDurationMs(long evaluationDurationMs) {
        this.evaluationDurationMs = evaluationDurationMs;
    }
}
