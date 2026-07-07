package com.infratrack.mobile.sync.dto;

import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * Compact work order record for mobile sync delta download (M6.1-BE2).
 */
public class SyncWorkOrderDeltaResponse {

    private Long workOrderId;

    private WorkOrderStatus status;

    private WorkOrderPriority priority;

    private WorkType workType;

    private String description;

    private Long assetId;

    private String assetName;

    private String assetCategoryName;

    private Long assignedTo;

    private String assignedToName;

    private LocalDateTime assignedAt;

    @Schema(description = "Work order creation time (epoch millis)")
    private Long createdAt;

    @Schema(description = "Server-side last update time (epoch millis)")
    private Long updatedAt;

    private String draftCompletionNotes;

    private boolean completionEligible;

    private Long operationalDecisionId;

    public static SyncWorkOrderDeltaResponse from(
            WorkOrder workOrder,
            String assignedToName,
            boolean completionEligible) {
        SyncWorkOrderDeltaResponse response = new SyncWorkOrderDeltaResponse();
        response.setWorkOrderId(workOrder.getId());
        response.setStatus(workOrder.getStatus());
        response.setPriority(workOrder.getPriority());
        response.setWorkType(workOrder.getWorkType());
        response.setDescription(workOrder.getDescription());
        response.setAssetId(workOrder.getAsset().getId());
        response.setAssetName(workOrder.getAsset().getName());
        if (workOrder.getAsset().getAssetCategory() != null) {
            response.setAssetCategoryName(workOrder.getAsset().getAssetCategory().getName());
        }
        response.setAssignedTo(workOrder.getAssignedToUserId());
        response.setAssignedToName(assignedToName);
        response.setAssignedAt(workOrder.getAssignedAt());
        response.setCreatedAt(workOrder.getCreatedAt());
        response.setUpdatedAt(workOrder.getUpdatedAt());
        response.setDraftCompletionNotes(workOrder.getDraftCompletionNotes());
        response.setCompletionEligible(completionEligible);
        response.setOperationalDecisionId(workOrder.getOperationalDecision().getId());
        return response;
    }

    public Long getWorkOrderId() {
        return workOrderId;
    }

    public void setWorkOrderId(Long workOrderId) {
        this.workOrderId = workOrderId;
    }

    public WorkOrderStatus getStatus() {
        return status;
    }

    public void setStatus(WorkOrderStatus status) {
        this.status = status;
    }

    public WorkOrderPriority getPriority() {
        return priority;
    }

    public void setPriority(WorkOrderPriority priority) {
        this.priority = priority;
    }

    public WorkType getWorkType() {
        return workType;
    }

    public void setWorkType(WorkType workType) {
        this.workType = workType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetCategoryName() {
        return assetCategoryName;
    }

    public void setAssetCategoryName(String assetCategoryName) {
        this.assetCategoryName = assetCategoryName;
    }

    public Long getAssignedTo() {
        return assignedTo;
    }

    public void setAssignedTo(Long assignedTo) {
        this.assignedTo = assignedTo;
    }

    public String getAssignedToName() {
        return assignedToName;
    }

    public void setAssignedToName(String assignedToName) {
        this.assignedToName = assignedToName;
    }

    public LocalDateTime getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(LocalDateTime assignedAt) {
        this.assignedAt = assignedAt;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getDraftCompletionNotes() {
        return draftCompletionNotes;
    }

    public void setDraftCompletionNotes(String draftCompletionNotes) {
        this.draftCompletionNotes = draftCompletionNotes;
    }

    public boolean isCompletionEligible() {
        return completionEligible;
    }

    public void setCompletionEligible(boolean completionEligible) {
        this.completionEligible = completionEligible;
    }

    public Long getOperationalDecisionId() {
        return operationalDecisionId;
    }

    public void setOperationalDecisionId(Long operationalDecisionId) {
        this.operationalDecisionId = operationalDecisionId;
    }
}
