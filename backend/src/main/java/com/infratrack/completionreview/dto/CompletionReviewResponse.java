package com.infratrack.completionreview.dto;

import com.infratrack.completionreview.CompletionReview;
import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.workorder.WorkOrderStatus;

import java.time.LocalDateTime;

public class CompletionReviewResponse {

    private Long id;
    private Long maintenanceActivityId;
    private Long workOrderId;
    private Long assetId;
    private String assetName;
    private CompletionReviewDecision decision;
    private String reviewNotes;
    private Long reviewedByUserId;
    private LocalDateTime reviewedAt;
    private WorkOrderStatus workOrderStatus;
    private Long reworkIssueId;
    private Long createdAt;
    private Long updatedAt;

    public static CompletionReviewResponse from(CompletionReview completionReview) {
        return from(completionReview, null);
    }

    public static CompletionReviewResponse from(CompletionReview completionReview, Long reworkIssueId) {
        CompletionReviewResponse response = new CompletionReviewResponse();
        response.id = completionReview.getId();
        response.maintenanceActivityId = completionReview.getMaintenanceActivity().getId();
        response.workOrderId = completionReview.getMaintenanceActivity().getWorkOrder().getId();
        response.assetId = completionReview.getAsset().getId();
        response.assetName = completionReview.getAsset().getName();
        response.decision = completionReview.getDecision();
        response.reviewNotes = completionReview.getReviewNotes();
        response.reviewedByUserId = completionReview.getReviewedByUserId();
        response.reviewedAt = completionReview.getReviewedAt();
        response.workOrderStatus = completionReview.getMaintenanceActivity().getWorkOrder().getStatus();
        response.reworkIssueId = reworkIssueId;
        response.createdAt = completionReview.getCreatedAt();
        response.updatedAt = completionReview.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getMaintenanceActivityId() {
        return maintenanceActivityId;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
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

    public WorkOrderStatus getWorkOrderStatus() {
        return workOrderStatus;
    }

    public Long getReworkIssueId() {
        return reworkIssueId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
