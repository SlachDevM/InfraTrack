package com.infratrack.mobile.dto;

import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.PhysicalCondition;

import java.time.LocalDate;

public class MobileInspectionDetailResponse {

    private Long id;
    private String status;
    private InspectionPriority priority;
    private LocalDate expectedCompletionDate;
    private PhysicalCondition observedCondition;
    private String observations;
    private boolean issueIdentified;

    public static MobileInspectionDetailResponse from(Inspection inspection) {
        MobileInspectionDetailResponse response = new MobileInspectionDetailResponse();
        response.id = inspection.getId();
        response.status = inspection.getStatus() != null ? inspection.getStatus().name() : null;
        response.priority = inspection.getPriority();
        response.expectedCompletionDate = inspection.getExpectedCompletionDate();
        response.observedCondition = inspection.getObservedCondition();
        response.observations = inspection.getObservations();
        response.issueIdentified = inspection.isIssueIdentified();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getStatus() {
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
}
