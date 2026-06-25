package com.infratrack.operationaldecision.dto;

import com.infratrack.operationaldecision.OperationalDecisionOutcome;

import java.time.LocalDateTime;

public class CreateOperationalDecisionRequest {

    private Long issueId;
    private OperationalDecisionOutcome outcome;
    private String rationale;
    private LocalDateTime decidedAt;

    public Long getIssueId() {
        return issueId;
    }

    public void setIssueId(Long issueId) {
        this.issueId = issueId;
    }

    public OperationalDecisionOutcome getOutcome() {
        return outcome;
    }

    public void setOutcome(OperationalDecisionOutcome outcome) {
        this.outcome = outcome;
    }

    public String getRationale() {
        return rationale;
    }

    public void setRationale(String rationale) {
        this.rationale = rationale;
    }

    public LocalDateTime getDecidedAt() {
        return decidedAt;
    }

    public void setDecidedAt(LocalDateTime decidedAt) {
        this.decidedAt = decidedAt;
    }
}
