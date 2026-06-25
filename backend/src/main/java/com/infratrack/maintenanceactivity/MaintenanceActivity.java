package com.infratrack.maintenanceactivity;

import com.infratrack.asset.Asset;
import com.infratrack.workorder.WorkOrder;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "maintenance_activities")
public class MaintenanceActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "work_order_id", nullable = false, unique = true)
    private WorkOrder workOrder;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Column(name = "performed_by_user_id", nullable = false)
    private Long performedByUserId;

    @Column(name = "completion_notes", nullable = false, columnDefinition = "TEXT")
    private String completionNotes;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected MaintenanceActivity() {
    }

    public MaintenanceActivity(
            WorkOrder workOrder,
            Asset asset,
            Long performedByUserId,
            String completionNotes,
            LocalDateTime completedAt) {
        this.workOrder = workOrder;
        this.asset = asset;
        this.performedByUserId = performedByUserId;
        this.completionNotes = completionNotes;
        this.completedAt = completedAt;
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

    public WorkOrder getWorkOrder() {
        return workOrder;
    }

    public Asset getAsset() {
        return asset;
    }

    public Long getPerformedByUserId() {
        return performedByUserId;
    }

    public String getCompletionNotes() {
        return completionNotes;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
