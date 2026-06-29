package com.infratrack.suggestedaction.dto;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.suggestedaction.SuggestedAction;
import com.infratrack.suggestedaction.SuggestedActionStatus;
import com.infratrack.suggestedaction.SuggestionConfidence;

public class SuggestedActionResponse {

    private Long id;
    private Long inspectionId;
    private Long reportId;
    private DecisionRuleActionType actionType;
    private String title;
    private String message;
    private String severity;
    private String suggestedPayload;
    private int matchedRuleCount;
    private String sourceRuleCodes;
    private SuggestedActionStatus status;
    private SuggestionConfidence confidence;
    private Long createdIssueId;
    private Long createdAt;
    private Long updatedAt;

    public static SuggestedActionResponse from(SuggestedAction action) {
        SuggestedActionResponse response = new SuggestedActionResponse();
        response.id = action.getId();
        response.inspectionId = action.getInspection().getId();
        response.reportId = action.getReport().getId();
        response.actionType = action.getActionType();
        response.title = action.getTitle();
        response.message = action.getMessage();
        response.severity = action.getSeverity();
        response.suggestedPayload = action.getSuggestedPayload();
        response.matchedRuleCount = action.getMatchedRuleCount();
        response.sourceRuleCodes = action.getSourceRuleCodes();
        response.status = action.getStatus();
        response.confidence = action.getConfidence();
        response.createdIssueId = action.getCreatedIssueId();
        response.createdAt = action.getCreatedAt();
        response.updatedAt = action.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public Long getReportId() {
        return reportId;
    }

    public DecisionRuleActionType getActionType() {
        return actionType;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getSeverity() {
        return severity;
    }

    public String getSuggestedPayload() {
        return suggestedPayload;
    }

    public int getMatchedRuleCount() {
        return matchedRuleCount;
    }

    public String getSourceRuleCodes() {
        return sourceRuleCodes;
    }

    public SuggestedActionStatus getStatus() {
        return status;
    }

    public SuggestionConfidence getConfidence() {
        return confidence;
    }

    public Long getCreatedIssueId() {
        return createdIssueId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
