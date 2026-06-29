package com.infratrack.preventivemaintenance;

public class TriggerEvaluationOutcome {

    private final boolean eligible;
    private final String evaluationReason;

    public TriggerEvaluationOutcome(boolean eligible, String evaluationReason) {
        this.eligible = eligible;
        this.evaluationReason = evaluationReason;
    }

    public boolean isEligible() {
        return eligible;
    }

    public String getEvaluationReason() {
        return evaluationReason;
    }
}
