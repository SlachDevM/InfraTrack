package com.infratrack.preventivemaintenance.dto;

public class ExecutionCandidateGenerationResultResponse {

    private Long planId;
    private String planCode;
    private ExecutionCandidateGenerationOutcome outcome;
    private PreventiveExecutionCandidateResponse candidate;
    private String failureMessage;

    public static ExecutionCandidateGenerationResultResponse notEligible(Long planId, String planCode) {
        ExecutionCandidateGenerationResultResponse response = new ExecutionCandidateGenerationResultResponse();
        response.planId = planId;
        response.planCode = planCode;
        response.outcome = ExecutionCandidateGenerationOutcome.NOT_ELIGIBLE;
        return response;
    }

    public static ExecutionCandidateGenerationResultResponse skipped(
            Long planId,
            String planCode,
            PreventiveExecutionCandidateResponse candidate) {
        ExecutionCandidateGenerationResultResponse response = new ExecutionCandidateGenerationResultResponse();
        response.planId = planId;
        response.planCode = planCode;
        response.outcome = ExecutionCandidateGenerationOutcome.SKIPPED_DUPLICATE;
        response.candidate = candidate;
        return response;
    }

    public static ExecutionCandidateGenerationResultResponse created(
            Long planId,
            String planCode,
            PreventiveExecutionCandidateResponse candidate) {
        ExecutionCandidateGenerationResultResponse response = new ExecutionCandidateGenerationResultResponse();
        response.planId = planId;
        response.planCode = planCode;
        response.outcome = ExecutionCandidateGenerationOutcome.CREATED;
        response.candidate = candidate;
        return response;
    }

    public static ExecutionCandidateGenerationResultResponse failed(
            Long planId,
            String planCode,
            String failureMessage) {
        ExecutionCandidateGenerationResultResponse response = new ExecutionCandidateGenerationResultResponse();
        response.planId = planId;
        response.planCode = planCode;
        response.outcome = ExecutionCandidateGenerationOutcome.FAILED;
        response.failureMessage = failureMessage;
        return response;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public ExecutionCandidateGenerationOutcome getOutcome() {
        return outcome;
    }

    public PreventiveExecutionCandidateResponse getCandidate() {
        return candidate;
    }

    public String getFailureMessage() {
        return failureMessage;
    }
}
