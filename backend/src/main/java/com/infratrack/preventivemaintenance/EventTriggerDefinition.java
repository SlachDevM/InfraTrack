package com.infratrack.preventivemaintenance;

import com.fasterxml.jackson.databind.JsonNode;

final class EventTriggerDefinition implements TriggerDefinition {

    private final PlanEventType event;

    EventTriggerDefinition(PlanEventType event) {
        this.event = event;
    }

    static EventTriggerDefinition fromJson(JsonNode root) {
        return new EventTriggerDefinition(PlanEventType.valueOf(root.get("event").asText()));
    }

    @Override
    public PlanTriggerType getTriggerType() {
        return PlanTriggerType.EVENT;
    }

    @Override
    public TriggerSummary buildSummary() {
        String title = "After " + eventLabel(event);
        String description = "Eligible after the "
                + eventLabel(event)
                + " business event occurs.";
        return new TriggerSummary(title, description, PlanTriggerType.EVENT);
    }

    @Override
    public TriggerEvaluationOutcome evaluate(TriggerEvaluationContext context) {
        return new TriggerEvaluationOutcome(false, "Business event evaluation is not implemented yet.");
    }

    private static String eventLabel(PlanEventType event) {
        return switch (event) {
            case COMPLETION_REVIEW -> "Completion Review";
            case INSPECTION_COMPLETED -> "Inspection Completed";
            case WORK_ORDER_COMPLETED -> "Work Order Completed";
            case MAINTENANCE_COMPLETED -> "Maintenance Completed";
        };
    }
}
