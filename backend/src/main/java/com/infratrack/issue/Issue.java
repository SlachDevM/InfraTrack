package com.infratrack.issue;

import com.infratrack.asset.Asset;
import com.infratrack.completionreview.CompletionReview;
import com.infratrack.inspection.Inspection;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * An operational issue identified from an inspection or created as rework
 * after a completion review.
 */
@Entity
@Table(name = "issues")
public class Issue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_id")
    private Inspection inspection;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_completion_review_id", unique = true)
    private CompletionReview sourceCompletionReview;

    @Enumerated(EnumType.STRING)
    @Column(name = "issue_type", nullable = false)
    private IssueType issueType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private IssueSeverity severity;

    @Column(name = "root_cause", columnDefinition = "TEXT")
    private String rootCause;

    @Column(name = "corrective_action", columnDefinition = "TEXT")
    private String correctiveAction;

    @Column(name = "preventive_action", columnDefinition = "TEXT")
    private String preventiveAction;

    /**
     * Organisational learning captured after resolving an issue.
     * Intended to feed future Knowledge Base, Analytics, AI recommendations,
     * and Preventive Maintenance improvements. Not yet consumed by those capabilities.
     */
    @Column(name = "lessons_learned", columnDefinition = "TEXT")
    private String lessonsLearned;

    @Column(name = "recorded_by_user_id", nullable = false)
    private Long recordedByUserId;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected Issue() {
    }

    public Issue(
            Inspection inspection,
            Asset asset,
            String description,
            IssueSeverity severity,
            Long recordedByUserId,
            LocalDateTime recordedAt) {
        this.inspection = inspection;
        this.asset = asset;
        this.issueType = IssueType.NORMAL;
        this.description = description;
        this.severity = severity;
        this.recordedByUserId = recordedByUserId;
        this.recordedAt = recordedAt;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static Issue forRework(
            Asset asset,
            CompletionReview sourceCompletionReview,
            String description,
            IssueSeverity severity,
            Long recordedByUserId,
            LocalDateTime recordedAt,
            String rootCause,
            String correctiveAction,
            String preventiveAction) {
        Issue issue = new Issue();
        issue.asset = asset;
        issue.sourceCompletionReview = sourceCompletionReview;
        issue.issueType = IssueType.REWORK;
        issue.description = description;
        issue.severity = severity;
        issue.rootCause = rootCause;
        issue.correctiveAction = correctiveAction;
        issue.preventiveAction = preventiveAction;
        issue.recordedByUserId = recordedByUserId;
        issue.recordedAt = recordedAt;
        long now = System.currentTimeMillis();
        issue.createdAt = now;
        issue.updatedAt = now;
        return issue;
    }

    public void applyCapaMetadata(
            String rootCause,
            String correctiveAction,
            String preventiveAction,
            String lessonsLearned) {
        this.rootCause = rootCause;
        this.correctiveAction = correctiveAction;
        this.preventiveAction = preventiveAction;
        this.lessonsLearned = lessonsLearned;
        this.updatedAt = System.currentTimeMillis();
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

    public Asset getAsset() {
        return asset;
    }

    public CompletionReview getSourceCompletionReview() {
        return sourceCompletionReview;
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

    public boolean isReworkIssue() {
        return issueType == IssueType.REWORK;
    }
}
