package com.infratrack.inspection.dto;

import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class InspectionResponse {

    private Long id;
    private Long assetId;
    private String assetName;
    private Long businessTriggerId;
    private BusinessTriggerType businessTriggerType;
    private String businessTriggerReason;
    private Long assignedToUserId;
    private String assignedToUserName;
    private Long assignedByUserId;
    private InspectionStatus status;
    private InspectionPriority priority;
    private LocalDate expectedCompletionDate;
    private PhysicalCondition observedCondition;
    private String observations;
    private boolean issueIdentified;
    private LocalDateTime completedAt;
    private Long completedByUserId;
    private Long createdAt;
    private Long updatedAt;

    public static InspectionResponse from(Inspection inspection) {
        return from(inspection, null, null);
    }

    public static InspectionResponse from(Inspection inspection, User assignedToUser, User assignedByUser) {
        InspectionResponse response = new InspectionResponse();
        response.id = inspection.getId();
        response.assetId = inspection.getAsset().getId();
        response.assetName = inspection.getAsset().getName();
        response.businessTriggerId = inspection.getBusinessTrigger().getId();
        response.businessTriggerType = inspection.getBusinessTrigger().getType();
        response.businessTriggerReason = inspection.getBusinessTrigger().getReason();
        response.assignedToUserId = inspection.getAssignedToUserId();
        response.assignedToUserName = assignedToUser != null ? assignedToUser.getName() : null;
        response.assignedByUserId = inspection.getAssignedByUserId();
        response.status = inspection.getStatus();
        response.priority = inspection.getPriority();
        response.expectedCompletionDate = inspection.getExpectedCompletionDate();
        response.observedCondition = inspection.getObservedCondition();
        response.observations = inspection.getObservations();
        response.issueIdentified = inspection.isIssueIdentified();
        response.completedAt = inspection.getCompletedAt();
        response.completedByUserId = inspection.getCompletedByUserId();
        response.createdAt = inspection.getCreatedAt();
        response.updatedAt = inspection.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public Long getBusinessTriggerId() {
        return businessTriggerId;
    }

    public BusinessTriggerType getBusinessTriggerType() {
        return businessTriggerType;
    }

    public String getBusinessTriggerReason() {
        return businessTriggerReason;
    }

    public Long getAssignedToUserId() {
        return assignedToUserId;
    }

    public String getAssignedToUserName() {
        return assignedToUserName;
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
