package com.infratrack.preventivemaintenance;

/**
 * Domain abstraction for a parsed, validated trigger definition (V2 Phase B Sprint B1.3).
 */
public interface TriggerDefinition {

    PlanTriggerType getTriggerType();

    TriggerSummary buildSummary();

    TriggerEvaluationOutcome evaluate(TriggerEvaluationContext context);
}
