package com.infratrack.preventivemaintenance;

/**
 * Generates structured trigger summaries from validated configuration (V2 Phase B).
 */
public final class TriggerDefinitionSummaryBuilder {

    private TriggerDefinitionSummaryBuilder() {
    }

    public static TriggerSummary buildSummary(PlanTriggerType triggerType, String configurationJson) {
        return TriggerDefinitionFactory.from(triggerType, configurationJson).buildSummary();
    }
}
