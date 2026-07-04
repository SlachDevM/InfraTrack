package com.infratrack.mobile.dto;

import com.infratrack.preventivemaintenance.PlanTriggerType;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlan;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;

public class MobileAssetPreventivePlanResponse {

    private boolean exists;
    private Long id;
    private String name;
    private PlanTriggerType triggerType;
    private boolean active;

    public static MobileAssetPreventivePlanResponse from(PreventiveMaintenancePlan plan) {
        MobileAssetPreventivePlanResponse response = new MobileAssetPreventivePlanResponse();
        response.exists = true;
        response.id = plan.getId();
        response.name = plan.getName();
        if (plan.getBusinessTrigger() != null) {
            response.triggerType = plan.getBusinessTrigger().getTriggerType();
        }
        response.active = plan.getStatus() == PreventiveMaintenancePlanStatus.ACTIVE;
        return response;
    }

    public boolean isExists() {
        return exists;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public PlanTriggerType getTriggerType() {
        return triggerType;
    }

    public boolean isActive() {
        return active;
    }
}
