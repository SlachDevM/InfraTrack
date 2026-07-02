package com.infratrack.suggestedaction;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.ruleevaluation.RuleEvaluationReport;
import com.infratrack.ruleevaluation.RuleEvaluationResult;
import jakarta.persistence.*;

@Entity
@Table(name = "suggested_actions")
public class SuggestedAction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id", nullable = false)
    private Inspection inspection;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private RuleEvaluationReport report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rule_evaluation_result_id")
    private RuleEvaluationResult ruleEvaluationResult;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private DecisionRuleActionType actionType;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(length = 50)
    private String severity;

    @Column(name = "suggested_payload", columnDefinition = "TEXT")
    private String suggestedPayload;

    @Column(name = "matched_rule_count", nullable = false)
    private int matchedRuleCount;

    @Column(name = "source_rule_codes", nullable = false, length = 500)
    private String sourceRuleCodes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SuggestedActionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SuggestionConfidence confidence = SuggestionConfidence.LOW;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "dismiss_comment", columnDefinition = "TEXT")
    private String dismissComment;

    @Column(name = "created_issue_id")
    private Long createdIssueId;

    @Column(name = "decided_by_user_id")
    private Long decidedByUserId;

    @Column(name = "decided_at")
    private Long decidedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected SuggestedAction() {
    }

    public SuggestedAction(
            Inspection inspection,
            RuleEvaluationReport report,
            RuleEvaluationResult ruleEvaluationResult,
            DecisionRuleActionType actionType,
            String title,
            String message,
            String severity,
            String suggestedPayload,
            int matchedRuleCount,
            String sourceRuleCodes,
            SuggestionConfidence confidence) {
        this.inspection = inspection;
        this.report = report;
        this.ruleEvaluationResult = ruleEvaluationResult;
        this.actionType = actionType;
        this.title = title;
        this.message = message;
        this.severity = severity;
        this.suggestedPayload = suggestedPayload;
        this.matchedRuleCount = matchedRuleCount;
        this.sourceRuleCodes = sourceRuleCodes;
        this.confidence = confidence;
        this.status = SuggestedActionStatus.PENDING;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void requirePending() {
        if (status != SuggestedActionStatus.PENDING) {
            throw new BusinessValidationException(
                    "Only pending suggested actions can be reviewed");
        }
    }

    public void markAccepted(Long managerId, Long issueId, long decidedAt) {
        requirePending();
        this.status = SuggestedActionStatus.ACCEPTED;
        this.createdIssueId = issueId;
        this.decidedByUserId = managerId;
        this.decidedAt = decidedAt;
        this.updatedAt = decidedAt;
    }

    public void markRejected(Long managerId, String reason, long decidedAt) {
        requirePending();
        this.status = SuggestedActionStatus.REJECTED;
        this.rejectionReason = reason;
        this.decidedByUserId = managerId;
        this.decidedAt = decidedAt;
        this.updatedAt = decidedAt;
    }

    public void markDismissed(Long managerId, String comment, long decidedAt) {
        requirePending();
        this.status = SuggestedActionStatus.DISMISSED;
        this.dismissComment = comment;
        this.decidedByUserId = managerId;
        this.decidedAt = decidedAt;
        this.updatedAt = decidedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Inspection getInspection() {
        return inspection;
    }

    public RuleEvaluationReport getReport() {
        return report;
    }

    public RuleEvaluationResult getRuleEvaluationResult() {
        return ruleEvaluationResult;
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

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getDismissComment() {
        return dismissComment;
    }

    public Long getCreatedIssueId() {
        return createdIssueId;
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
}
