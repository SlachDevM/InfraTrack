package com.infratrack.preventivemaintenance.dto;

import com.infratrack.preventivemaintenance.DecisionSource;
import com.infratrack.preventivemaintenance.ExecutionReportStatus;
import com.infratrack.preventivemaintenance.PlanTargetAction;
import com.infratrack.preventivemaintenance.PlanTriggerType;
import com.infratrack.preventivemaintenance.PreventiveExecutionReport;

public class PreventiveExecutionReportResponse {

    private Long id;
    private Long candidateId;
    private Long preventiveMaintenancePlanIdSnapshot;
    private String planCodeSnapshot;
    private Integer planVersionSnapshot;
    private String planNameSnapshot;
    private Long assetIdSnapshot;
    private String assetNameSnapshot;
    private PlanTargetAction targetActionSnapshot;
    private PlanTriggerType triggerTypeSnapshot;
    private String triggerSummaryTitleSnapshot;
    private String triggerSummaryDescriptionSnapshot;
    private DecisionSource decisionSource;
    private ExecutionReportStatus reportStatus;
    private Long generatedAt;
    private Long reviewStartedAt;
    private Long approvedAt;
    private Long rejectedAt;
    private Long dismissedAt;
    private Long inspectionCreatedAt;
    private Long createdInspectionId;
    private Long decidedByUserId;
    private String decisionReason;
    private Long createdAt;
    private Long updatedAt;

    public static PreventiveExecutionReportResponse from(PreventiveExecutionReport report) {
        PreventiveExecutionReportResponse response = new PreventiveExecutionReportResponse();
        response.id = report.getId();
        response.candidateId = report.getCandidate().getId();
        response.preventiveMaintenancePlanIdSnapshot = report.getPreventiveMaintenancePlanIdSnapshot();
        response.planCodeSnapshot = report.getPlanCodeSnapshot();
        response.planVersionSnapshot = report.getPlanVersionSnapshot();
        response.planNameSnapshot = report.getPlanNameSnapshot();
        response.assetIdSnapshot = report.getAssetIdSnapshot();
        response.assetNameSnapshot = report.getAssetNameSnapshot();
        response.targetActionSnapshot = report.getTargetActionSnapshot();
        response.triggerTypeSnapshot = report.getTriggerTypeSnapshot();
        response.triggerSummaryTitleSnapshot = report.getTriggerSummaryTitleSnapshot();
        response.triggerSummaryDescriptionSnapshot = report.getTriggerSummaryDescriptionSnapshot();
        response.decisionSource = report.getDecisionSource();
        response.reportStatus = report.getReportStatus();
        response.generatedAt = report.getGeneratedAt();
        response.reviewStartedAt = report.getReviewStartedAt();
        response.approvedAt = report.getApprovedAt();
        response.rejectedAt = report.getRejectedAt();
        response.dismissedAt = report.getDismissedAt();
        response.inspectionCreatedAt = report.getInspectionCreatedAt();
        response.createdInspectionId = report.getCreatedInspectionId();
        response.decidedByUserId = report.getDecidedByUserId();
        response.decisionReason = report.getDecisionReason();
        response.createdAt = report.getCreatedAt();
        response.updatedAt = report.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getCandidateId() {
        return candidateId;
    }

    public Long getPreventiveMaintenancePlanIdSnapshot() {
        return preventiveMaintenancePlanIdSnapshot;
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

    public Long getAssetIdSnapshot() {
        return assetIdSnapshot;
    }

    public String getAssetNameSnapshot() {
        return assetNameSnapshot;
    }

    public PlanTargetAction getTargetActionSnapshot() {
        return targetActionSnapshot;
    }

    public PlanTriggerType getTriggerTypeSnapshot() {
        return triggerTypeSnapshot;
    }

    public String getTriggerSummaryTitleSnapshot() {
        return triggerSummaryTitleSnapshot;
    }

    public String getTriggerSummaryDescriptionSnapshot() {
        return triggerSummaryDescriptionSnapshot;
    }

    public DecisionSource getDecisionSource() {
        return decisionSource;
    }

    public ExecutionReportStatus getReportStatus() {
        return reportStatus;
    }

    public Long getGeneratedAt() {
        return generatedAt;
    }

    public Long getReviewStartedAt() {
        return reviewStartedAt;
    }

    public Long getApprovedAt() {
        return approvedAt;
    }

    public Long getRejectedAt() {
        return rejectedAt;
    }

    public Long getDismissedAt() {
        return dismissedAt;
    }

    public Long getInspectionCreatedAt() {
        return inspectionCreatedAt;
    }

    public Long getCreatedInspectionId() {
        return createdInspectionId;
    }

    public Long getDecidedByUserId() {
        return decidedByUserId;
    }

    public String getDecisionReason() {
        return decisionReason;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
