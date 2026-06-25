package com.infratrack.completionreview;

import com.infratrack.asset.Asset;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "completion_reviews")
public class CompletionReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "maintenance_activity_id", nullable = false, unique = true)
    private MaintenanceActivity maintenanceActivity;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision", nullable = false)
    private CompletionReviewDecision decision;

    @Column(name = "review_notes", nullable = false, columnDefinition = "TEXT")
    private String reviewNotes;

    @Column(name = "reviewed_by_user_id", nullable = false)
    private Long reviewedByUserId;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected CompletionReview() {
    }

    public CompletionReview(
            MaintenanceActivity maintenanceActivity,
            Asset asset,
            CompletionReviewDecision decision,
            String reviewNotes,
            Long reviewedByUserId,
            LocalDateTime reviewedAt) {
        this.maintenanceActivity = maintenanceActivity;
        this.asset = asset;
        this.decision = decision;
        this.reviewNotes = reviewNotes;
        this.reviewedByUserId = reviewedByUserId;
        this.reviewedAt = reviewedAt;
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

    public MaintenanceActivity getMaintenanceActivity() {
        return maintenanceActivity;
    }

    public Asset getAsset() {
        return asset;
    }

    public CompletionReviewDecision getDecision() {
        return decision;
    }

    public String getReviewNotes() {
        return reviewNotes;
    }

    public Long getReviewedByUserId() {
        return reviewedByUserId;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
