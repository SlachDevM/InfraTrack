package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.ExecutionCandidateStatus;
import com.infratrack.preventivemaintenance.PlanTargetAction;
import com.infratrack.preventivemaintenance.PlanTriggerType;
import com.infratrack.preventivemaintenance.PreventiveExecutionCandidate;

public class PreventiveExecutionCandidateResponse {

    private Long id;
    private Long planId;
    private Long assetId;
    private String assetName;
    private PlanTriggerType triggerType;
    private ExecutionCandidateStatus candidateStatus;
    private String eligibilityReason;
    private Long evaluatedAt;
    private Long nextEligibleAt;
    private String planCodeSnapshot;
    private Integer planVersionSnapshot;
    private String planNameSnapshot;
    private PlanTargetAction targetActionSnapshot;
    private String triggerSummaryTitleSnapshot;
    private String triggerSummaryDescriptionSnapshot;
    private Long createdAt;
    private Long updatedAt;
    private Long decidedByUserId;
    private Long decidedAt;
    private String rejectionReason;
    private String dismissComment;
    private Long createdInspectionId;
    private String decisionNotes;

    public static PreventiveExecutionCandidateResponse from(PreventiveExecutionCandidate candidate) {
        PreventiveExecutionCandidateResponse response = new PreventiveExecutionCandidateResponse();
        response.id = candidate.getId();
        response.planId = candidate.getPreventiveMaintenancePlan().getId();
        response.assetId = candidate.getAsset().getId();
        response.assetName = candidate.getAsset().getName();
        response.triggerType = candidate.getTriggerType();
        response.candidateStatus = candidate.getCandidateStatus();
        response.eligibilityReason = candidate.getEligibilityReason();
        response.evaluatedAt = candidate.getEvaluatedAt();
        response.nextEligibleAt = candidate.getNextEligibleAt();
        response.planCodeSnapshot = candidate.getPlanCodeSnapshot();
        response.planVersionSnapshot = candidate.getPlanVersionSnapshot();
        response.planNameSnapshot = candidate.getPlanNameSnapshot();
        response.targetActionSnapshot = candidate.getTargetActionSnapshot();
        response.triggerSummaryTitleSnapshot = candidate.getTriggerSummaryTitleSnapshot();
        response.triggerSummaryDescriptionSnapshot = candidate.getTriggerSummaryDescriptionSnapshot();
        response.createdAt = candidate.getCreatedAt();
        response.updatedAt = candidate.getUpdatedAt();
        response.decidedByUserId = candidate.getDecidedByUserId();
        response.decidedAt = candidate.getDecidedAt();
        response.rejectionReason = candidate.getRejectionReason();
        response.dismissComment = candidate.getDismissComment();
        response.createdInspectionId = candidate.getCreatedInspectionId();
        response.decisionNotes = candidate.getDecisionNotes();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getPlanId() {
        return planId;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public PlanTriggerType getTriggerType() {
        return triggerType;
    }

    public ExecutionCandidateStatus getCandidateStatus() {
        return candidateStatus;
    }

    public String getEligibilityReason() {
        return eligibilityReason;
    }

    public Long getEvaluatedAt() {
        return evaluatedAt;
    }

    public Long getNextEligibleAt() {
        return nextEligibleAt;
    }

    public String getPlanCodeSnapshot() {
        return planCodeSnapshot;
    }

    public Integer getPlanVersionSnapshot() {
        return planVersionSnapshot;
    }

    public String getPlanNameSnapshot() {
        return planNameSnapshot;
    }

    public PlanTargetAction getTargetActionSnapshot() {
        return targetActionSnapshot;
    }

    public String getTriggerSummaryTitleSnapshot() {
        return triggerSummaryTitleSnapshot;
    }

    public String getTriggerSummaryDescriptionSnapshot() {
        return triggerSummaryDescriptionSnapshot;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public Long getDecidedByUserId() {
        return decidedByUserId;
    }

    public Long getDecidedAt() {
        return decidedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getDismissComment() {
        return dismissComment;
    }

    public Long getCreatedInspectionId() {
        return createdInspectionId;
    }

    public String getDecisionNotes() {
        return decisionNotes;
    }
}
