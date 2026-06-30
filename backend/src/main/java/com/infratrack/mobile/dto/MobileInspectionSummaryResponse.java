package com.infratrack.mobile.dto;

import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionStatus;

import java.time.LocalDate;

public class MobileInspectionSummaryResponse {

    private Long inspectionId;
    private Long assetId;
    private String assetName;
    private String assetCategoryName;
    private InspectionStatus status;
    private InspectionPriority priority;
    private LocalDate expectedCompletionDate;
    private Long templateId;
    private String templateName;
    private boolean hasChecklist;
    private boolean issueIdentified;

    public static MobileInspectionSummaryResponse from(Inspection inspection) {
        MobileInspectionSummaryResponse response = new MobileInspectionSummaryResponse();
        response.inspectionId = inspection.getId();
        response.assetId = inspection.getAsset().getId();
        response.assetName = inspection.getAsset().getName();
        if (inspection.getAsset().getAssetCategory() != null) {
            response.assetCategoryName = inspection.getAsset().getAssetCategory().getName();
        }
        response.status = inspection.getStatus();
        response.priority = inspection.getPriority();
        response.expectedCompletionDate = inspection.getExpectedCompletionDate();
        response.issueIdentified = inspection.isIssueIdentified();
        if (inspection.getInspectionTemplate() != null) {
            response.templateId = inspection.getInspectionTemplate().getId();
            response.templateName = inspection.getInspectionTemplate().getName();
            response.hasChecklist = true;
        } else {
            response.hasChecklist = false;
        }
        return response;
    }

    public Long getInspectionId() {
        return inspectionId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public String getAssetCategoryName() {
        return assetCategoryName;
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

    public Long getTemplateId() {
        return templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public boolean isHasChecklist() {
        return hasChecklist;
    }

    public boolean isIssueIdentified() {
        return issueIdentified;
    }
}
