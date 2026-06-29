package com.infratrack.preventivemaintenance;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.preventive.scheduler")
public class PreventiveSchedulerProperties {

    private boolean enabled = false;
    private String cron = "0 0 6 * * *";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getCron() {
        return cron;
    }

    public void setCron(String cron) {
        this.cron = cron;
    }
}
