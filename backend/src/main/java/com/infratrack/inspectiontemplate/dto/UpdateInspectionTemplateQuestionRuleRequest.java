package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UpdateInspectionTemplateQuestionRuleRequest {

    @NotBlank
    @Size(max = 200)
    private String ruleName;

    @Size(max = 2000)
    private String description;

    @NotNull
    private DecisionRuleConditionType conditionType;

    @NotNull
    private DecisionRuleOperator operator;

    @Size(max = 500)
    private String comparisonValue;

    @NotNull
    private DecisionRuleActionType actionType;

    private String actionPayload;

    @NotNull
    @Min(1)
    private Integer priority;

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}
