package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PreventiveSchedulerRunStatus;

public class PreventiveSchedulerRunResultResponse {

    private Long runId;
    private PreventiveSchedulerRunStatus status;
    private int plansEvaluatedCount;
    private int candidatesCreatedCount;
    private int candidatesSkippedDuplicateCount;
    private int plansNotEligibleCount;
    private Long durationMs;

    public PreventiveSchedulerRunResultResponse(
            Long runId,
            PreventiveSchedulerRunStatus status,
            int plansEvaluatedCount,
            int candidatesCreatedCount,
            int candidatesSkippedDuplicateCount,
            int plansNotEligibleCount,
            Long durationMs) {
        this.runId = runId;
        this.status = status;
        this.plansEvaluatedCount = plansEvaluatedCount;
        this.candidatesCreatedCount = candidatesCreatedCount;
        this.candidatesSkippedDuplicateCount = candidatesSkippedDuplicateCount;
        this.plansNotEligibleCount = plansNotEligibleCount;
        this.durationMs = durationMs;
    }

    public Long getRunId() {
        return runId;
    }

    public PreventiveSchedulerRunStatus getStatus() {
        return status;
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

    public Long getDurationMs() {
        return durationMs;
    }
}
