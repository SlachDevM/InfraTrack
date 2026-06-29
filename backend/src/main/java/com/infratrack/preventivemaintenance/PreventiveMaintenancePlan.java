package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import jakarta.persistence.*;

@Entity
@Table(name = "preventive_maintenance_plans")
public class PreventiveMaintenancePlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(nullable = false)
    private String name;

    @Column(name = "plan_code", nullable = false, updatable = false, length = 100)
    private String planCode;

    @Column(nullable = false)
    private Integer version;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreventiveMaintenancePlanStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreventiveMaintenancePlanPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_action", nullable = false)
    private PlanTargetAction targetAction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_template_id")
    private InspectionTemplate inspectionTemplate;

    @OneToOne(mappedBy = "plan", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private PlanBusinessTrigger businessTrigger;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected PreventiveMaintenancePlan() {
    }

    public PreventiveMaintenancePlan(
            Asset asset,
            String planCode,
            String name,
            String description,
            Integer version,
            PreventiveMaintenancePlanStatus status,
            PreventiveMaintenancePlanPriority priority,
            PlanTargetAction targetAction,
            InspectionTemplate inspectionTemplate) {
        this.asset = asset;
        this.planCode = planCode;
        this.name = name;
        this.description = description;
        this.version = version;
        this.status = status;
        this.priority = priority;
        this.targetAction = targetAction;
        this.inspectionTemplate = inspectionTemplate;
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

    public Asset getAsset() {
        return asset;
    }

    public String getName() {
        return name;
    }

    public String getPlanCode() {
        return planCode;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public void setName(String name) {
        this.name = name;
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

    public InspectionTemplate getInspectionTemplate() {
        return inspectionTemplate;
    }

    public void setInspectionTemplate(InspectionTemplate inspectionTemplate) {
        this.inspectionTemplate = inspectionTemplate;
    }

    public PlanBusinessTrigger getBusinessTrigger() {
        return businessTrigger;
    }

    public void setBusinessTrigger(PlanBusinessTrigger businessTrigger) {
        this.businessTrigger = businessTrigger;
        if (businessTrigger != null) {
            businessTrigger.setPlan(this);
        }
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    void setCreatedAtMillis(long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void touchUpdatedAt() {
        this.updatedAt = System.currentTimeMillis();
    }
}
