package com.infratrack.businesstrigger;

import com.infratrack.asset.Asset;
import jakarta.persistence.*;

@Entity
@Table(name = "business_triggers")
public class BusinessTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BusinessTriggerType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(nullable = false)
    private boolean urgent;

    @Column(name = "created_by_user_id", nullable = false)
    private Long createdByUserId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected BusinessTrigger() {
    }

    public BusinessTrigger(
            Asset asset,
            BusinessTriggerType type,
            String reason,
            boolean urgent,
            Long createdByUserId) {
        this.asset = asset;
        this.type = type;
        this.reason = reason;
        this.urgent = urgent;
        this.createdByUserId = createdByUserId;
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

    public BusinessTriggerType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
