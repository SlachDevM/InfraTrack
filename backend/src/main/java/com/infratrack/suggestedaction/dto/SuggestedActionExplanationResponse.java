package com.infratrack.suggestedaction.dto;

public class SuggestedActionExplanationResponse {

    private String matchedRuleCode;
    private String matchedRuleName;
    private String conditionDescription;
    private String actualValue;
    private String configuredActionDescription;
    private String summaryText;

    public String getMatchedRuleCode() {
        return matchedRuleCode;
    }

    public void setMatchedRuleCode(String matchedRuleCode) {
        this.matchedRuleCode = matchedRuleCode;
    }

    public String getMatchedRuleName() {
        return matchedRuleName;
    }

    public void setMatchedRuleName(String matchedRuleName) {
        this.matchedRuleName = matchedRuleName;
    }

    public String getConditionDescription() {
        return conditionDescription;
    }

    public void setConditionDescription(String conditionDescription) {
        this.conditionDescription = conditionDescription;
    }

    public String getActualValue() {
        return actualValue;
    }

    public void setActualValue(String actualValue) {
        this.actualValue = actualValue;
    }

    public String getConfiguredActionDescription() {
        return configuredActionDescription;
    }

    public void setConfiguredActionDescription(String configuredActionDescription) {
        this.configuredActionDescription = configuredActionDescription;
    }

    public String getSummaryText() {
        return summaryText;
    }

    public void setSummaryText(String summaryText) {
        this.summaryText = summaryText;
    }
}
