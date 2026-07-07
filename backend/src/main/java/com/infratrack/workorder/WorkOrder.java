package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.operationaldecision.OperationalDecision;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "work_orders")
public class WorkOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "operational_decision_id", nullable = false)
    private OperationalDecision operationalDecision;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "work_type", nullable = false)
    private WorkType workType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderStatus status;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WorkOrderPriority priority;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at_business_date", nullable = false)
    private LocalDateTime createdAtBusinessDate;

    @Column(name = "assigned_to_user_id")
    private Long assignedToUserId;

    @Column(name = "assigned_by_user_id")
    private Long assignedByUserId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "draft_completion_notes", columnDefinition = "TEXT")
    private String draftCompletionNotes;

    protected WorkOrder() {
    }

    public WorkOrder(
            OperationalDecision operationalDecision,
            Asset asset,
            WorkType workType,
            String description,
            WorkOrderPriority priority,
            Long createdByUserId,
            LocalDateTime createdAtBusinessDate) {
        this.operationalDecision = operationalDecision;
        this.asset = asset;
        this.workType = workType;
        this.status = WorkOrderStatus.CREATED;
        this.description = description;
        this.priority = priority;
        this.createdByUserId = createdByUserId;
        this.createdAtBusinessDate = createdAtBusinessDate;
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

    public OperationalDecision getOperationalDecision() {
        return operationalDecision;
    }

    public Asset getAsset() {
        return asset;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public String getDescription() {
        return description;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public LocalDateTime getCreatedAtBusinessDate() {
        return createdAtBusinessDate;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public Long getAssignedByUserId() {
        return assignedByUserId;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void assign(Long assignedToUserId, Long assignedByUserId, LocalDateTime assignedAt) {
        this.assignedToUserId = assignedToUserId;
        this.assignedByUserId = assignedByUserId;
        this.assignedAt = assignedAt;
        this.status = WorkOrderStatus.ASSIGNED;
        this.updatedAt = System.currentTimeMillis();
    }

    public void complete() {
        this.status = WorkOrderStatus.COMPLETED;
        this.updatedAt = System.currentTimeMillis();
    }

    public String getDraftCompletionNotes() {
        return draftCompletionNotes;
    }

    public void saveDraftCompletionNotes(String draftCompletionNotes) {
        this.draftCompletionNotes = draftCompletionNotes;
        this.updatedAt = System.currentTimeMillis();
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
