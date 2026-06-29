package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PlanTriggerType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class PlanBusinessTriggerRequest {

    @NotNull
    @Schema(description = "Trigger type: TIME, METER, or EVENT")
    private PlanTriggerType triggerType;

    @NotNull
    @Schema(description = "Trigger configuration as JSON text")
    private String configurationJson;

    @NotNull
    private Boolean active;

    public PlanTriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(PlanTriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public String getConfigurationJson() {
        return configurationJson;
    }

    public void setConfigurationJson(String configurationJson) {
        this.configurationJson = configurationJson;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
