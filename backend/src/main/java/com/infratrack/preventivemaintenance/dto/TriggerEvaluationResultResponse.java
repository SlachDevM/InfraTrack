package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PlanTriggerType;
import com.infratrack.preventivemaintenance.TriggerEvaluationOutcome;
import com.infratrack.preventivemaintenance.TriggerSummary;

public class TriggerEvaluationResultResponse {

    private Long planId;
    private String planCode;
    private PlanTriggerType triggerType;
    private boolean eligible;
    private String evaluationReason;
    private Long evaluatedAt;
    private long evaluationDurationMs;
    private TriggerSummaryResponse triggerSummary;

    public static TriggerEvaluationResultResponse from(
            Long planId,
            String planCode,
            PlanTriggerType triggerType,
            TriggerEvaluationOutcome outcome,
            TriggerSummary summary,
            long evaluatedAt,
            long evaluationDurationMs) {
        TriggerEvaluationResultResponse response = new TriggerEvaluationResultResponse();
        response.planId = planId;
        response.planCode = planCode;
        response.triggerType = triggerType;
        response.eligible = outcome.isEligible();
        response.evaluationReason = outcome.getEvaluationReason();
        response.evaluatedAt = evaluatedAt;
        response.evaluationDurationMs = evaluationDurationMs;
        response.triggerSummary = TriggerSummaryResponse.from(summary);
        return response;
    }

    public Long getPlanId() {
        return planId;
    }

    public String getPlanCode() {
        return planCode;
    }

    public PlanTriggerType getTriggerType() {
        return triggerType;
    }

    public boolean isEligible() {
        return eligible;
    }

    public String getEvaluationReason() {
        return evaluationReason;
    }

    public Long getEvaluatedAt() {
        return evaluatedAt;
    }

    public long getEvaluationDurationMs() {
        return evaluationDurationMs;
    }

    public TriggerSummaryResponse getTriggerSummary() {
        return triggerSummary;
    }
}
