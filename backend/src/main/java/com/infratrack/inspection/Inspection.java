package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.businesstrigger.BusinessTrigger;
import jakarta.persistence.*;

import java.time.LocalDate;

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

    public BusinessTrigger getBusinessTrigger() {
        return businessTrigger;
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

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
