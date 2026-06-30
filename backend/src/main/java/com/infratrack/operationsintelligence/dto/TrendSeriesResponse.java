package com.infratrack.operationsintelligence.dto;

import java.util.List;

public class TrendSeriesResponse {

    private List<TrendDataPointResponse> inspectionsCompleted;
    private List<TrendDataPointResponse> issuesCreated;
    private List<TrendDataPointResponse> workOrdersCompleted;
    private List<TrendDataPointResponse> preventiveCandidatesGenerated;
    private List<TrendDataPointResponse> suggestedActionsAccepted;

    public List<TrendDataPointResponse> getInspectionsCompleted() {
        return inspectionsCompleted;
    }

    public void setInspectionsCompleted(List<TrendDataPointResponse> inspectionsCompleted) {
        this.inspectionsCompleted = inspectionsCompleted;
    }

    public List<TrendDataPointResponse> getIssuesCreated() {
        return issuesCreated;
    }

    public void setIssuesCreated(List<TrendDataPointResponse> issuesCreated) {
        this.issuesCreated = issuesCreated;
    }

    public List<TrendDataPointResponse> getWorkOrdersCompleted() {
        return workOrdersCompleted;
    }

    public void setWorkOrdersCompleted(List<TrendDataPointResponse> workOrdersCompleted) {
        this.workOrdersCompleted = workOrdersCompleted;
    }

    public List<TrendDataPointResponse> getPreventiveCandidatesGenerated() {
        return preventiveCandidatesGenerated;
    }

    public void setPreventiveCandidatesGenerated(List<TrendDataPointResponse> preventiveCandidatesGenerated) {
        this.preventiveCandidatesGenerated = preventiveCandidatesGenerated;
    }

    public List<TrendDataPointResponse> getSuggestedActionsAccepted() {
        return suggestedActionsAccepted;
    }

    public void setSuggestedActionsAccepted(List<TrendDataPointResponse> suggestedActionsAccepted) {
        this.suggestedActionsAccepted = suggestedActionsAccepted;
    }
}
