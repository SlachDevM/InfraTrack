package com.infratrack.issue.dto;

import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;

import java.time.LocalDateTime;

public class IssueResponse {

    private Long id;
    private Long inspectionId;
    private Long assetId;
    private String assetName;
    private String description;
    private IssueSeverity severity;
    private Long recordedByUserId;
    private LocalDateTime recordedAt;
    private Long createdAt;
    private Long updatedAt;

    public static IssueResponse from(Issue issue) {
        IssueResponse response = new IssueResponse();
        response.id = issue.getId();
        response.inspectionId = issue.getInspection().getId();
        response.assetId = issue.getAsset().getId();
        response.assetName = issue.getAsset().getName();
        response.description = issue.getDescription();
        response.severity = issue.getSeverity();
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

    public String getDescription() {
        return description;
    }

    public IssueSeverity getSeverity() {
        return severity;
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
