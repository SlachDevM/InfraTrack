package com.infratrack.preventivemaintenance;

import jakarta.persistence.*;

@Entity
@Table(name = "preventive_execution_reports")
public class PreventiveExecutionReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private PreventiveExecutionCandidate candidate;

    @Column(name = "preventive_maintenance_plan_id_snapshot", nullable = false)
    private Long preventiveMaintenancePlanIdSnapshot;

    @Column(name = "plan_code_snapshot", nullable = false, length = 100)
    private String planCodeSnapshot;

    @Column(name = "plan_version_snapshot", nullable = false)
    private Integer planVersionSnapshot;

    @Column(name = "plan_name_snapshot", nullable = false)
    private String planNameSnapshot;

    @Column(name = "asset_id_snapshot", nullable = false)
    private Long assetIdSnapshot;

    @Column(name = "asset_name_snapshot", nullable = false)
    private String assetNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_action_snapshot", nullable = false)
    private PlanTargetAction targetActionSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type_snapshot", nullable = false)
    private PlanTriggerType triggerTypeSnapshot;

    @Column(name = "trigger_summary_title_snapshot", nullable = false)
    private String triggerSummaryTitleSnapshot;

    @Column(name = "trigger_summary_description_snapshot", nullable = false, columnDefinition = "TEXT")
    private String triggerSummaryDescriptionSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "decision_source", nullable = false)
    private DecisionSource decisionSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_status", nullable = false)
    private ExecutionReportStatus reportStatus;

    @Column(name = "generated_at", nullable = false)
    private Long generatedAt;

    @Column(name = "review_started_at")
    private Long reviewStartedAt;

    @Column(name = "approved_at")
    private Long approvedAt;

    @Column(name = "rejected_at")
    private Long rejectedAt;

    @Column(name = "dismissed_at")
    private Long dismissedAt;

    @Column(name = "inspection_created_at")
    private Long inspectionCreatedAt;

    @Column(name = "created_inspection_id")
    private Long createdInspectionId;

    @Column(name = "decided_by_user_id")
    private Long decidedByUserId;

    @Column(name = "decision_reason", columnDefinition = "TEXT")
    private String decisionReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    protected PreventiveExecutionReport() {
    }

    public PreventiveExecutionReport(PreventiveExecutionCandidate candidate) {
        this.candidate = candidate;
        this.preventiveMaintenancePlanIdSnapshot = candidate.getPreventiveMaintenancePlan().getId();
        this.planCodeSnapshot = candidate.getPlanCodeSnapshot();
        this.planVersionSnapshot = candidate.getPlanVersionSnapshot();
        this.planNameSnapshot = candidate.getPlanNameSnapshot();
        this.assetIdSnapshot = candidate.getAsset().getId();
        this.assetNameSnapshot = candidate.getAsset().getName();
        this.targetActionSnapshot = candidate.getTargetActionSnapshot();
        this.triggerTypeSnapshot = candidate.getTriggerType();
        this.triggerSummaryTitleSnapshot = candidate.getTriggerSummaryTitleSnapshot();
        this.triggerSummaryDescriptionSnapshot = candidate.getTriggerSummaryDescriptionSnapshot();
        this.decisionSource = DecisionSource.PREVENTIVE_ENGINE;
        this.reportStatus = ExecutionReportStatus.GENERATED;
        this.generatedAt = candidate.getCreatedAt();
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void markApproved(Long decidedByUserId) {
        long now = System.currentTimeMillis();
        this.reportStatus = ExecutionReportStatus.APPROVED;
        this.approvedAt = now;
        this.decidedByUserId = decidedByUserId;
        this.updatedAt = now;
    }

    public void markInspectionCreated(Long inspectionId, Long decidedByUserId) {
        long now = System.currentTimeMillis();
        this.reportStatus = ExecutionReportStatus.INSPECTION_CREATED;
        this.createdInspectionId = inspectionId;
        this.inspectionCreatedAt = now;
        this.decidedByUserId = decidedByUserId;
        if (this.approvedAt == null) {
            this.approvedAt = now;
        }
        this.updatedAt = now;
    }

    public void markRejected(Long decidedByUserId, String reason) {
        long now = System.currentTimeMillis();
        this.reportStatus = ExecutionReportStatus.REJECTED;
        this.rejectedAt = now;
        this.decidedByUserId = decidedByUserId;
        this.decisionReason = reason;
        this.updatedAt = now;
    }

    public void markDismissed(Long decidedByUserId, String comment) {
        long now = System.currentTimeMillis();
        this.reportStatus = ExecutionReportStatus.DISMISSED;
        this.dismissedAt = now;
        this.decidedByUserId = decidedByUserId;
        this.decisionReason = comment;
        this.updatedAt = now;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PreventiveExecutionCandidate getCandidate() {
        return candidate;
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
