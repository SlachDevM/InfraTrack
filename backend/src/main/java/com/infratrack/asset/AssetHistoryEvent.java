package com.infratrack.asset;

import jakarta.persistence.*;

import java.time.LocalDate;

@Entity
@Table(name = "asset_history_events")
public class AssetHistoryEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private AssetHistoryEventType eventType;

    @Column(name = "performed_by_user_id", nullable = false)
    private Long performedByUserId;

    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    protected AssetHistoryEvent() {
    }

    public AssetHistoryEvent(
            Asset asset,
            AssetHistoryEventType eventType,
            Long performedByUserId,
            LocalDate eventDate) {
        this.asset = asset;
        this.eventType = eventType;
        this.performedByUserId = performedByUserId;
        this.eventDate = eventDate;
        this.createdAt = System.currentTimeMillis();
    }

    public Long getId() {
        return id;
    }

    public Asset getAsset() {
        return asset;
    }

    public AssetHistoryEventType getEventType() {
        return eventType;
    }

    public Long getPerformedByUserId() {
        return performedByUserId;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public Long getCreatedAt() {
        return createdAt;
    }
}
