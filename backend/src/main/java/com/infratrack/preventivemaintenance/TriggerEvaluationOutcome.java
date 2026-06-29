package com.infratrack.preventivemaintenance;

public class TriggerEvaluationOutcome {

    private final boolean eligible;
    private final String evaluationReason;
    private final Long nextEligibleAt;

    public TriggerEvaluationOutcome(boolean eligible, String evaluationReason) {
        this(eligible, evaluationReason, null);
    }

    public TriggerEvaluationOutcome(boolean eligible, String evaluationReason, Long nextEligibleAt) {
        this.eligible = eligible;
        this.evaluationReason = evaluationReason;
        this.nextEligibleAt = nextEligibleAt;
    }

    public boolean isEligible() {
        return eligible;
    }

    public String getEvaluationReason() {
        return evaluationReason;
    }

    public Long getNextEligibleAt() {
        return nextEligibleAt;
    }
}
