package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PlanTriggerType;
import com.infratrack.preventivemaintenance.TriggerSummary;

public class TriggerSummaryResponse {

    private String title;
    private String description;
    private PlanTriggerType triggerType;

    public static TriggerSummaryResponse from(TriggerSummary summary) {
        TriggerSummaryResponse response = new TriggerSummaryResponse();
        response.title = summary.getTitle();
        response.description = summary.getDescription();
        response.triggerType = summary.getTriggerType();
        return response;
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
