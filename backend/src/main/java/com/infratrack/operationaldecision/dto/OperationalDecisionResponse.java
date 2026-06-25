package com.infratrack.operationaldecision.dto;

import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;

import java.time.LocalDateTime;

public class OperationalDecisionResponse {

    private Long id;
    private Long issueId;
    private Long assetId;
    private String assetName;
    private OperationalDecisionOutcome outcome;
    private String rationale;
    private Long decidedByUserId;
    private LocalDateTime decidedAt;
    private Long createdAt;
    private Long updatedAt;

    public static OperationalDecisionResponse from(OperationalDecision decision) {
        OperationalDecisionResponse response = new OperationalDecisionResponse();
        response.id = decision.getId();
        response.issueId = decision.getIssue().getId();
        response.assetId = decision.getAsset().getId();
        response.assetName = decision.getAsset().getName();
        response.outcome = decision.getOutcome();
        response.rationale = decision.getRationale();
        response.decidedByUserId = decision.getDecidedByUserId();
        response.decidedAt = decision.getDecidedAt();
        response.createdAt = decision.getCreatedAt();
        response.updatedAt = decision.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getIssueId() {
        return issueId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public OperationalDecisionOutcome getOutcome() {
        return outcome;
    }

    public String getRationale() {
        return rationale;
    }

    public Long getDecidedByUserId() {
        return decidedByUserId;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
