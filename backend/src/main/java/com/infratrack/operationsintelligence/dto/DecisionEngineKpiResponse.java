package com.infratrack.operationsintelligence.dto;

public class DecisionEngineKpiResponse {

    private long ruleEvaluationReports;
    private long suggestedActionsPending;
    private long suggestedActionsAccepted;
    private long suggestedActionsRejected;
    private long suggestedActionsDismissed;
    private long matchedRuleResults;

    public long getRuleEvaluationReports() {
        return ruleEvaluationReports;
    }

    public void setRuleEvaluationReports(long ruleEvaluationReports) {
        this.ruleEvaluationReports = ruleEvaluationReports;
    }

    public long getSuggestedActionsPending() {
        return suggestedActionsPending;
    }

    public void setSuggestedActionsPending(long suggestedActionsPending) {
        this.suggestedActionsPending = suggestedActionsPending;
    }

    public long getSuggestedActionsAccepted() {
        return suggestedActionsAccepted;
    }

    public void setSuggestedActionsAccepted(long suggestedActionsAccepted) {
        this.suggestedActionsAccepted = suggestedActionsAccepted;
    }

    public long getSuggestedActionsRejected() {
        return suggestedActionsRejected;
    }

    public void setSuggestedActionsRejected(long suggestedActionsRejected) {
        this.suggestedActionsRejected = suggestedActionsRejected;
    }

    public long getSuggestedActionsDismissed() {
        return suggestedActionsDismissed;
    }

    public void setSuggestedActionsDismissed(long suggestedActionsDismissed) {
        this.suggestedActionsDismissed = suggestedActionsDismissed;
    }

    public long getMatchedRuleResults() {
        return matchedRuleResults;
    }

    public void setMatchedRuleResults(long matchedRuleResults) {
        this.matchedRuleResults = matchedRuleResults;
    }
}
