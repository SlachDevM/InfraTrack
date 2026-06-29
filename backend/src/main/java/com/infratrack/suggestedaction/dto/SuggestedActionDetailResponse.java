package com.infratrack.suggestedaction.dto;

import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.ruleevaluation.RuleEvaluationStatus;
import com.infratrack.suggestedaction.SuggestedAction;
import com.infratrack.suggestedaction.SuggestedActionExplainabilityBuilder;
import com.infratrack.suggestedaction.SuggestedActionStatus;
import com.infratrack.suggestedaction.SuggestionConfidence;

public class SuggestedActionDetailResponse {

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
    private String rejectionReason;
    private String dismissComment;
    private Long decidedByUserId;
    private Long decidedAt;
    private Long createdAt;
    private Long updatedAt;
    private Long evaluationReportEvaluatedAt;
    private Integer evaluationReportTemplateVersion;
    private RuleEvaluationStatus evaluationReportStatus;
    private SuggestedActionExplanationResponse explanation;

    public static SuggestedActionDetailResponse from(SuggestedAction action) {
        SuggestedActionDetailResponse response = new SuggestedActionDetailResponse();
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
        response.rejectionReason = action.getRejectionReason();
        response.dismissComment = action.getDismissComment();
        response.decidedByUserId = action.getDecidedByUserId();
        response.decidedAt = action.getDecidedAt();
        response.createdAt = action.getCreatedAt();
        response.updatedAt = action.getUpdatedAt();
        response.evaluationReportEvaluatedAt = action.getReport().getEvaluatedAt();
        response.evaluationReportTemplateVersion = action.getReport().getTemplateVersionSnapshot();
        response.evaluationReportStatus = action.getReport().getEvaluationStatus();
        if (action.getRuleEvaluationResult() != null) {
            response.explanation = SuggestedActionExplainabilityBuilder.fromResult(
                    action.getRuleEvaluationResult());
        }
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getDismissComment() {
        return dismissComment;
    }

    public Long getDecidedByUserId() {
        return decidedByUserId;
    }

    public Long getDecidedAt() {
        return decidedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Long getEvaluationReportEvaluatedAt() {
        return evaluationReportEvaluatedAt;
    }

    public Integer getEvaluationReportTemplateVersion() {
        return evaluationReportTemplateVersion;
    }

    public RuleEvaluationStatus getEvaluationReportStatus() {
        return evaluationReportStatus;
    }

    public SuggestedActionExplanationResponse getExplanation() {
        return explanation;
    }
}
