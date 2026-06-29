package com.infratrack.preventivemaintenance.dto;

import com.infratrack.inspection.dto.InspectionResponse;

public class ApprovePreventiveCandidateResponse {

    private PreventiveExecutionCandidateResponse candidate;
    private InspectionResponse inspection;

    public ApprovePreventiveCandidateResponse(
            PreventiveExecutionCandidateResponse candidate,
            InspectionResponse inspection) {
        this.candidate = candidate;
        this.inspection = inspection;
    }

    public PreventiveExecutionCandidateResponse getCandidate() {
        return candidate;
    }

    public InspectionResponse getInspection() {
        return inspection;
    }
}
