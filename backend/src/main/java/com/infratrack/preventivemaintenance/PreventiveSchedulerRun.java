package com.infratrack.preventivemaintenance;

import jakarta.persistence.*;

@Entity
@Table(name = "preventive_scheduler_runs")
public class PreventiveSchedulerRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Long startedAt;

    @Column(name = "finished_at", nullable = false)
    private Long finishedAt;

    @Column(name = "duration_ms", nullable = false)
    private Long durationMs;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PreventiveSchedulerRunStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "triggered_by", nullable = false)
    private PreventiveSchedulerTriggeredBy triggeredBy;

    @Column(name = "triggered_by_user_id")
    private Long triggeredByUserId;

    @Column(name = "plans_evaluated_count", nullable = false)
    private int plansEvaluatedCount;

    @Column(name = "candidates_created_count", nullable = false)
    private int candidatesCreatedCount;

    @Column(name = "candidates_skipped_duplicate_count", nullable = false)
    private int candidatesSkippedDuplicateCount;

    @Column(name = "plans_not_eligible_count", nullable = false)
    private int plansNotEligibleCount;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    protected PreventiveSchedulerRun() {
    }

    public PreventiveSchedulerRun(
            Long startedAt,
            PreventiveSchedulerTriggeredBy triggeredBy,
            Long triggeredByUserId) {
        this.startedAt = startedAt;
        this.triggeredBy = triggeredBy;
        this.triggeredByUserId = triggeredByUserId;
        this.createdAt = startedAt;
    }

    public void complete(
            Long finishedAt,
            PreventiveSchedulerRunStatus status,
            int plansEvaluatedCount,
            int candidatesCreatedCount,
            int candidatesSkippedDuplicateCount,
            int plansNotEligibleCount,
            String errorMessage) {
        this.finishedAt = finishedAt;
        this.durationMs = finishedAt - startedAt;
        this.status = status;
        this.plansEvaluatedCount = plansEvaluatedCount;
        this.candidatesCreatedCount = candidatesCreatedCount;
        this.candidatesSkippedDuplicateCount = candidatesSkippedDuplicateCount;
        this.plansNotEligibleCount = plansNotEligibleCount;
        this.errorMessage = errorMessage;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public Long getFinishedAt() {
        return finishedAt;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public PreventiveSchedulerRunStatus getStatus() {
        return status;
    }

    public PreventiveSchedulerTriggeredBy getTriggeredBy() {
        return triggeredBy;
    }

    public Long getTriggeredByUserId() {
        return triggeredByUserId;
    }

    public int getPlansEvaluatedCount() {
        return plansEvaluatedCount;
    }

    public int getCandidatesCreatedCount() {
        return candidatesCreatedCount;
    }

    public int getCandidatesSkippedDuplicateCount() {
        return candidatesSkippedDuplicateCount;
    }

    public int getPlansNotEligibleCount() {
        return plansNotEligibleCount;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Long getCreatedAt() {
        return createdAt;
    }
}
