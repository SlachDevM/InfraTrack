package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PlanBusinessTrigger;
import com.infratrack.preventivemaintenance.PlanTriggerType;
import com.infratrack.preventivemaintenance.TriggerDefinitionSummaryBuilder;

public class PlanBusinessTriggerResponse {

    private Long id;
    private PlanTriggerType triggerType;
    private String configurationJson;
    private TriggerSummaryResponse triggerSummary;
    private boolean active;
    private Long createdAt;
    private Long updatedAt;

    public static PlanBusinessTriggerResponse from(PlanBusinessTrigger trigger) {
        PlanBusinessTriggerResponse response = new PlanBusinessTriggerResponse();
        response.id = trigger.getId();
        response.triggerType = trigger.getTriggerType();
        response.configurationJson = trigger.getConfigurationJson();
        response.triggerSummary = TriggerSummaryResponse.from(
                TriggerDefinitionSummaryBuilder.buildSummary(
                        trigger.getTriggerType(),
                        trigger.getConfigurationJson()));
        response.active = trigger.isActive();
        response.createdAt = trigger.getCreatedAt();
        response.updatedAt = trigger.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public PlanTriggerType getTriggerType() {
        return triggerType;
    }

    public String getConfigurationJson() {
        return configurationJson;
    }

    public TriggerSummaryResponse getTriggerSummary() {
        return triggerSummary;
    }

    public boolean isActive() {
        return active;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
