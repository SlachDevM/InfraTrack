package com.infratrack.inspection.dto;

import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.PhysicalCondition;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

public class InspectionSummaryResponse {

    private Long id;
    private String assetName;
    private Long assetCategoryId;
    private Long inspectionTemplateId;
    private String inspectionTemplateName;
    private Long businessTriggerId;
    private BusinessTriggerType businessTriggerType;
    private String businessTriggerReason;
    private Long assignedToUserId;
    private String assignedToUserName;
    private InspectionStatus status;
    private InspectionPriority priority;
    private LocalDate expectedCompletionDate;
    private PhysicalCondition observedCondition;
    private String observations;
    private boolean issueIdentified;
    private LocalDateTime completedAt;
    private Long completedByUserId;
    private Long createdAt;

    public static InspectionSummaryResponse from(Inspection inspection, Map<Long, String> userNamesById) {
        InspectionSummaryResponse response = new InspectionSummaryResponse();
        response.id = inspection.getId();
        response.assetName = inspection.getAsset().getName();
        response.assetCategoryId = inspection.getAsset().getAssetCategory().getId();
        if (inspection.getInspectionTemplate() != null) {
            response.inspectionTemplateId = inspection.getInspectionTemplate().getId();
            response.inspectionTemplateName = inspection.getInspectionTemplate().getName();
        }
        response.businessTriggerId = inspection.getBusinessTrigger().getId();
        response.businessTriggerType = inspection.getBusinessTrigger().getType();
        response.businessTriggerReason = inspection.getBusinessTrigger().getReason();
        response.assignedToUserId = inspection.getAssignedToUserId();
        response.assignedToUserName = userNamesById.get(inspection.getAssignedToUserId());
        response.status = inspection.getStatus();
        response.priority = inspection.getPriority();
        response.expectedCompletionDate = inspection.getExpectedCompletionDate();
        response.observedCondition = inspection.getObservedCondition();
        response.observations = inspection.getObservations();
        response.issueIdentified = inspection.isIssueIdentified();
        response.completedAt = inspection.getCompletedAt();
        response.completedByUserId = inspection.getCompletedByUserId();
        response.createdAt = inspection.getCreatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getAssetName() {
        return assetName;
    }

    public Long getAssetCategoryId() {
        return assetCategoryId;
    }

    public Long getInspectionTemplateId() {
        return inspectionTemplateId;
    }

    public String getInspectionTemplateName() {
        return inspectionTemplateName;
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
}
