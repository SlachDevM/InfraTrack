package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRule;

public class InspectionTemplateQuestionRuleResponse {

    private Long id;
    private Long questionId;
    private String ruleCode;
    private String ruleName;
    private String description;
    private DecisionRuleConditionType conditionType;
    private DecisionRuleOperator operator;
    private String comparisonValue;
    private DecisionRuleActionType actionType;
    private String actionPayload;
    private boolean active;
    private int priority;
    private String disabledReason;
    private Long createdAt;
    private Long updatedAt;

    public static InspectionTemplateQuestionRuleResponse from(InspectionTemplateQuestionRule rule) {
        InspectionTemplateQuestionRuleResponse response = new InspectionTemplateQuestionRuleResponse();
        response.id = rule.getId();
        response.questionId = rule.getQuestion().getId();
        response.ruleCode = rule.getRuleCode();
        response.ruleName = rule.getRuleName();
        response.description = rule.getDescription();
        response.conditionType = rule.getConditionType();
        response.operator = rule.getOperator();
        response.comparisonValue = rule.getComparisonValue();
        response.actionType = rule.getActionType();
        response.actionPayload = rule.getActionPayload();
        response.active = rule.isActive();
        response.priority = rule.getPriority();
        response.disabledReason = rule.getDisabledReason();
        response.createdAt = rule.getCreatedAt();
        response.updatedAt = rule.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public String getDescription() {
        return description;
    }

    public DecisionRuleConditionType getConditionType() {
        return conditionType;
    }

    public DecisionRuleOperator getOperator() {
        return operator;
    }

    public String getComparisonValue() {
        return comparisonValue;
    }

    public DecisionRuleActionType getActionType() {
        return actionType;
    }

    public String getActionPayload() {
        return actionPayload;
    }

    public boolean isActive() {
        return active;
    }

    public int getPriority() {
        return priority;
    }

    public String getDisabledReason() {
        return disabledReason;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
