package com.infratrack.issue.dto;

import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.issue.IssueType;

import java.time.LocalDateTime;

public class IssueResponse {

    private Long id;
    private Long inspectionId;
    private Long assetId;
    private String assetName;
    private Long sourceCompletionReviewId;
    private IssueType issueType;
    private String description;
    private IssueSeverity severity;
    private String rootCause;
    private String correctiveAction;
    private String preventiveAction;
    private String lessonsLearned;
    private Long recordedByUserId;
    private LocalDateTime recordedAt;
    private Long createdAt;
    private Long updatedAt;

    public static IssueResponse from(Issue issue) {
        IssueResponse response = new IssueResponse();
        response.id = issue.getId();
        response.inspectionId = issue.getInspection() != null ? issue.getInspection().getId() : null;
        response.assetId = issue.getAsset().getId();
        response.assetName = issue.getAsset().getName();
        response.sourceCompletionReviewId = issue.getSourceCompletionReview() != null
                ? issue.getSourceCompletionReview().getId()
                : null;
        response.issueType = issue.getIssueType();
        response.description = issue.getDescription();
        response.severity = issue.getSeverity();
        response.rootCause = issue.getRootCause();
        response.correctiveAction = issue.getCorrectiveAction();
        response.preventiveAction = issue.getPreventiveAction();
        response.lessonsLearned = issue.getLessonsLearned();
        response.recordedByUserId = issue.getRecordedByUserId();
        response.recordedAt = issue.getRecordedAt();
        response.createdAt = issue.getCreatedAt();
        response.updatedAt = issue.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public Long getSourceCompletionReviewId() {
        return sourceCompletionReviewId;
    }

    public IssueType getIssueType() {
        return issueType;
    }

    public String getDescription() {
        return description;
    }

    public IssueSeverity getSeverity() {
        return severity;
    }

    public String getRootCause() {
        return rootCause;
    }

    public String getCorrectiveAction() {
        return correctiveAction;
    }

    public String getPreventiveAction() {
        return preventiveAction;
    }

    public String getLessonsLearned() {
        return lessonsLearned;
    }

    public Long getRecordedByUserId() {
        return recordedByUserId;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
