package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PlanTargetAction;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlan;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanPriority;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;

public class PreventiveMaintenancePlanResponse {

    private Long id;
    private String planCode;
    private String name;
    private String description;
    private Integer version;
    private Long assetId;
    private String assetName;
    private PreventiveMaintenancePlanStatus status;
    private PreventiveMaintenancePlanPriority priority;
    private PlanTargetAction targetAction;
    private Long inspectionTemplateId;
    private String inspectionTemplateName;
    private PlanBusinessTriggerResponse businessTrigger;
    private Long createdAt;
    private Long updatedAt;

    public static PreventiveMaintenancePlanResponse from(PreventiveMaintenancePlan plan) {
        PreventiveMaintenancePlanResponse response = new PreventiveMaintenancePlanResponse();
        response.id = plan.getId();
        response.planCode = plan.getPlanCode();
        response.name = plan.getName();
        response.description = plan.getDescription();
        response.version = plan.getVersion();
        response.assetId = plan.getAsset().getId();
        response.assetName = plan.getAsset().getName();
        response.status = plan.getStatus();
        response.priority = plan.getPriority();
        response.targetAction = plan.getTargetAction();
        if (plan.getInspectionTemplate() != null) {
            response.inspectionTemplateId = plan.getInspectionTemplate().getId();
            response.inspectionTemplateName = plan.getInspectionTemplate().getName();
        }
        if (plan.getBusinessTrigger() != null) {
            response.businessTrigger = PlanBusinessTriggerResponse.from(plan.getBusinessTrigger());
        }
        response.createdAt = plan.getCreatedAt();
        response.updatedAt = plan.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getPlanCode() {
        return planCode;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Integer getVersion() {
        return version;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public PreventiveMaintenancePlanStatus getStatus() {
        return status;
    }

    public PreventiveMaintenancePlanPriority getPriority() {
        return priority;
    }

    public PlanTargetAction getTargetAction() {
        return targetAction;
    }

    public Long getInspectionTemplateId() {
        return inspectionTemplateId;
    }

    public String getInspectionTemplateName() {
        return inspectionTemplateName;
    }

    public PlanBusinessTriggerResponse getBusinessTrigger() {
        return businessTrigger;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
