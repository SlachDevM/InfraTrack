package com.infratrack.preventivemaintenance.dto;

public class PreventiveSchedulerStatusResponse {

    private boolean enabled;

    public PreventiveSchedulerStatusResponse(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
