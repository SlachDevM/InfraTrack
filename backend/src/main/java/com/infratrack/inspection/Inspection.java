package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspections")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "business_trigger_id", nullable = false)
    private BusinessTrigger businessTrigger;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inspection_template_id")
    private InspectionTemplate inspectionTemplate;

    @Column(name = "assigned_to_user_id", nullable = false)
    private Long assignedToUserId;

    @Column(name = "assigned_by_user_id", nullable = false)
    private Long assignedByUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionPriority priority;

    @Column(name = "expected_completion_date")
    private LocalDate expectedCompletionDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "observed_condition")
    private PhysicalCondition observedCondition;

    @Column(columnDefinition = "TEXT")
    private String observations;

    @Column(name = "issue_identified", nullable = false)
    private boolean issueIdentified;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "completed_by_user_id")
    private Long completedByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected Inspection() {
    }

    public Inspection(
            Asset asset,
            BusinessTrigger businessTrigger,
            Long assignedToUserId,
            Long assignedByUserId,
            InspectionPriority priority,
            LocalDate expectedCompletionDate) {
        this.asset = asset;
        this.businessTrigger = businessTrigger;
        this.assignedToUserId = assignedToUserId;
        this.assignedByUserId = assignedByUserId;
        this.status = InspectionStatus.ASSIGNED;
        this.priority = priority;
        this.expectedCompletionDate = expectedCompletionDate;
        this.issueIdentified = false;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void complete(
            PhysicalCondition observedCondition,
            String observations,
            boolean issueIdentified,
            LocalDateTime completedAt,
            Long completedByUserId) {
        this.observedCondition = observedCondition;
        this.observations = observations;
        this.issueIdentified = issueIdentified;
        this.completedAt = completedAt;
        this.completedByUserId = completedByUserId;
        this.status = InspectionStatus.COMPLETED;
        this.updatedAt = System.currentTimeMillis();
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

    public BusinessTrigger getBusinessTrigger() {
        return businessTrigger;
    }

    public InspectionTemplate getInspectionTemplate() {
        return inspectionTemplate;
    }

    public void setInspectionTemplate(InspectionTemplate inspectionTemplate) {
        this.inspectionTemplate = inspectionTemplate;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public Long getAssignedByUserId() {
        return assignedByUserId;
    }

    public InspectionStatus getStatus() {
        return status;
    }

    public InspectionPriority getPriority() {
        return priority;
    }

    public LocalDate getExpectedCompletionDate() {
        return expectedCompletionDate;
    }

    public PhysicalCondition getObservedCondition() {
        return observedCondition;
    }

    public String getObservations() {
        return observations;
    }

    public boolean isIssueIdentified() {
        return issueIdentified;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getCompletedByUserId() {
        return completedByUserId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
