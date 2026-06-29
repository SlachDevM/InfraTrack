package com.infratrack.preventivemaintenance;

import com.fasterxml.jackson.databind.JsonNode;

final class MeterTriggerDefinition implements TriggerDefinition {

    private final PlanMeterType meter;
    private final int every;

    MeterTriggerDefinition(PlanMeterType meter, int every) {
        this.meter = meter;
        this.every = every;
    }

    static MeterTriggerDefinition fromJson(JsonNode root) {
        return new MeterTriggerDefinition(
                PlanMeterType.valueOf(root.get("meter").asText()),
                root.get("every").asInt());
    }

    @Override
    public PlanTriggerType getTriggerType() {
        return PlanTriggerType.METER;
    }

    @Override
    public TriggerSummary buildSummary() {
        String title = "Every " + every + " " + meterLabel(meter);
        String description = "Eligible once every "
                + every
                + " "
                + meterLabel(meter)
                + " when meter readings are available.";
        return new TriggerSummary(title, description, PlanTriggerType.METER);
    }

    @Override
    public TriggerEvaluationOutcome evaluate(TriggerEvaluationContext context) {
        return new TriggerEvaluationOutcome(false, "Meter values are not available yet.");
    }

    private static String meterLabel(PlanMeterType meter) {
        return switch (meter) {
            case OPERATING_HOURS -> "operating hours";
        };
    }
}
