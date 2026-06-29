package com.infratrack.preventivemaintenance;

import jakarta.persistence.*;

/**
 * Plan-side business trigger configuration (when something should happen).
 * Distinct from operational {@link com.infratrack.businesstrigger.BusinessTrigger} records.
 */
@Entity
@Table(name = "preventive_plan_business_triggers")
public class PlanBusinessTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preventive_plan_id", nullable = false, unique = true)
    private PreventiveMaintenancePlan plan;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private PlanTriggerType triggerType;

    @Column(name = "configuration_json", nullable = false, columnDefinition = "TEXT")
    private String configurationJson;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected PlanBusinessTrigger() {
    }

    public PlanBusinessTrigger(
            PlanTriggerType triggerType,
            String configurationJson,
            boolean active) {
        this.triggerType = triggerType;
        this.configurationJson = configurationJson;
        this.active = active;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PreventiveMaintenancePlan getPlan() {
        return plan;
    }

    void setPlan(PreventiveMaintenancePlan plan) {
        this.plan = plan;
    }

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

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = System.currentTimeMillis();
    }
}
