package com.infratrack.preventivemaintenance;

public class TriggerSummary {

    private final String title;
    private final String description;
    private final PlanTriggerType triggerType;

    public TriggerSummary(String title, String description, PlanTriggerType triggerType) {
        this.title = title;
        this.description = description;
        this.triggerType = triggerType;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public PlanTriggerType getTriggerType() {
        return triggerType;
    }
}
