package com.infratrack.mobile.dto;

import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.PhysicalCondition;

import java.time.LocalDateTime;

public class MobileAssetLastInspectionResponse {

    private Long id;
    private InspectionStatus status;
    private LocalDateTime completedAt;
    private PhysicalCondition observedCondition;
    private boolean issueIdentified;

    public static MobileAssetLastInspectionResponse from(Inspection inspection) {
        MobileAssetLastInspectionResponse response = new MobileAssetLastInspectionResponse();
        response.id = inspection.getId();
        response.status = inspection.getStatus();
        response.completedAt = inspection.getCompletedAt();
        response.observedCondition = inspection.getObservedCondition();
        response.issueIdentified = inspection.isIssueIdentified();
        return response;
    }

    public Long getId() {
        return id;
    }

    public InspectionStatus getStatus() {
        return status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public PhysicalCondition getObservedCondition() {
        return observedCondition;
    }

    public boolean isIssueIdentified() {
        return issueIdentified;
    }
}
