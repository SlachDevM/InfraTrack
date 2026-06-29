package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.PlanTargetAction;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanPriority;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class UpdatePreventiveMaintenancePlanRequest {

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    @Positive
    private Integer version;

    @Size(max = 4000)
    private String description;

    @NotNull
    private PreventiveMaintenancePlanStatus status;

    @NotNull
    private PreventiveMaintenancePlanPriority priority;

    @NotNull
    private PlanTargetAction targetAction;

    @Positive
    @Schema(description = "Optional inspection template; omit or null to clear")
    private Long inspectionTemplateId;

    @NotNull
    @Valid
    private PlanBusinessTriggerRequest businessTrigger;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PreventiveMaintenancePlanStatus getStatus() {
        return status;
    }

    public void setStatus(PreventiveMaintenancePlanStatus status) {
        this.status = status;
    }

    public PreventiveMaintenancePlanPriority getPriority() {
        return priority;
    }

    public void setPriority(PreventiveMaintenancePlanPriority priority) {
        this.priority = priority;
    }

    public PlanTargetAction getTargetAction() {
        return targetAction;
    }

    public void setTargetAction(PlanTargetAction targetAction) {
        this.targetAction = targetAction;
    }

    public Long getInspectionTemplateId() {
        return inspectionTemplateId;
    }

    public void setInspectionTemplateId(Long inspectionTemplateId) {
        this.inspectionTemplateId = inspectionTemplateId;
    }

    public PlanBusinessTriggerRequest getBusinessTrigger() {
        return businessTrigger;
    }

    public void setBusinessTrigger(PlanBusinessTriggerRequest businessTrigger) {
        this.businessTrigger = businessTrigger;
    }
}
