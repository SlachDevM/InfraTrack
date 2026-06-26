package com.infratrack.operationaldecision;

import com.infratrack.asset.Asset;
import com.infratrack.issue.Issue;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "operational_decisions")
public class OperationalDecision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "issue_id", nullable = false)
    private Issue issue;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OperationalDecisionOutcome outcome;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String rationale;

    @Column(name = "decided_by_user_id", nullable = false)
    private Long decidedByUserId;

    @Column(name = "delegated_authority_id")
    private Long delegatedAuthorityId;

    @Column(name = "decided_at", nullable = false)
    private LocalDateTime decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected OperationalDecision() {
    }

    public OperationalDecision(
            Issue issue,
            Asset asset,
            OperationalDecisionOutcome outcome,
            String rationale,
            Long decidedByUserId,
            LocalDateTime decidedAt) {
        this.issue = issue;
        this.asset = asset;
        this.outcome = outcome;
        this.rationale = rationale;
        this.decidedByUserId = decidedByUserId;
        this.decidedAt = decidedAt;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Issue getIssue() {
        return issue;
    }

    public Asset getAsset() {
        return asset;
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

    public Long getDelegatedAuthorityId() {
        return delegatedAuthorityId;
    }

    public void setDelegatedAuthorityId(Long delegatedAuthorityId) {
        this.delegatedAuthorityId = delegatedAuthorityId;
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
