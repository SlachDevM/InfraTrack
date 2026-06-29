package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PreventiveSchedulerRun;
import com.infratrack.preventivemaintenance.PreventiveSchedulerRunStatus;
import com.infratrack.preventivemaintenance.PreventiveSchedulerTriggeredBy;

public class PreventiveSchedulerRunResponse {

    private Long id;
    private Long startedAt;
    private Long finishedAt;
    private Long durationMs;
    private PreventiveSchedulerRunStatus status;
    private PreventiveSchedulerTriggeredBy triggeredBy;
    private Long triggeredByUserId;
    private int plansEvaluatedCount;
    private int candidatesCreatedCount;
    private int candidatesSkippedDuplicateCount;
    private int plansNotEligibleCount;
    private String errorMessage;
    private Long createdAt;

    public static PreventiveSchedulerRunResponse from(PreventiveSchedulerRun run) {
        PreventiveSchedulerRunResponse response = new PreventiveSchedulerRunResponse();
        response.id = run.getId();
        response.startedAt = run.getStartedAt();
        response.finishedAt = run.getFinishedAt();
        response.durationMs = run.getDurationMs();
        response.status = run.getStatus();
        response.triggeredBy = run.getTriggeredBy();
        response.triggeredByUserId = run.getTriggeredByUserId();
        response.plansEvaluatedCount = run.getPlansEvaluatedCount();
        response.candidatesCreatedCount = run.getCandidatesCreatedCount();
        response.candidatesSkippedDuplicateCount = run.getCandidatesSkippedDuplicateCount();
        response.plansNotEligibleCount = run.getPlansNotEligibleCount();
        response.errorMessage = run.getErrorMessage();
        response.createdAt = run.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
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
