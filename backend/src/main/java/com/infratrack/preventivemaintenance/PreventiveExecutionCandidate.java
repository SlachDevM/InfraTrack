package com.infratrack.preventivemaintenance;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.asset.Asset;
import jakarta.persistence.*;

@Entity
@Table(name = "preventive_execution_candidates")
public class PreventiveExecutionCandidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "preventive_plan_id", nullable = false)
    private PreventiveMaintenancePlan preventiveMaintenancePlan;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "asset_id", nullable = false)
    private Asset asset;

    @Enumerated(EnumType.STRING)
    @Column(name = "candidate_status", nullable = false)
    private ExecutionCandidateStatus candidateStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "trigger_type", nullable = false)
    private PlanTriggerType triggerType;

    @Column(name = "eligibility_reason", nullable = false, columnDefinition = "TEXT")
    private String eligibilityReason;

    @Column(name = "evaluated_at", nullable = false)
    private Long evaluatedAt;

    @Column(name = "next_eligible_at")
    private Long nextEligibleAt;

    @Column(name = "plan_code_snapshot", nullable = false, length = 100)
    private String planCodeSnapshot;

    @Column(name = "plan_version_snapshot", nullable = false)
    private Integer planVersionSnapshot;

    @Column(name = "plan_name_snapshot", nullable = false)
    private String planNameSnapshot;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_action_snapshot", nullable = false)
    private PlanTargetAction targetActionSnapshot;

    @Column(name = "trigger_summary_title_snapshot", nullable = false)
    private String triggerSummaryTitleSnapshot;

    @Column(name = "trigger_summary_description_snapshot", nullable = false, columnDefinition = "TEXT")
    private String triggerSummaryDescriptionSnapshot;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Long createdAt;

    @Column(name = "updated_at", nullable = false)
    private Long updatedAt;

    @Column(name = "decided_by_user_id")
    private Long decidedByUserId;

    @Column(name = "decided_at")
    private Long decidedAt;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Column(name = "dismiss_comment", columnDefinition = "TEXT")
    private String dismissComment;

    @Column(name = "created_inspection_id")
    private Long createdInspectionId;

    @Column(name = "decision_notes", columnDefinition = "TEXT")
    private String decisionNotes;

    protected PreventiveExecutionCandidate() {
    }

    public PreventiveExecutionCandidate(
            PreventiveMaintenancePlan preventiveMaintenancePlan,
            Asset asset,
            PlanTriggerType triggerType,
            String eligibilityReason,
            Long evaluatedAt,
            Long nextEligibleAt,
            String planCodeSnapshot,
            Integer planVersionSnapshot,
            String planNameSnapshot,
            PlanTargetAction targetActionSnapshot,
            String triggerSummaryTitleSnapshot,
            String triggerSummaryDescriptionSnapshot) {
        this.preventiveMaintenancePlan = preventiveMaintenancePlan;
        this.asset = asset;
        this.candidateStatus = ExecutionCandidateStatus.PENDING;
        this.triggerType = triggerType;
        this.eligibilityReason = eligibilityReason;
        this.evaluatedAt = evaluatedAt;
        this.nextEligibleAt = nextEligibleAt;
        this.planCodeSnapshot = planCodeSnapshot;
        this.planVersionSnapshot = planVersionSnapshot;
        this.planNameSnapshot = planNameSnapshot;
        this.targetActionSnapshot = targetActionSnapshot;
        this.triggerSummaryTitleSnapshot = triggerSummaryTitleSnapshot;
        this.triggerSummaryDescriptionSnapshot = triggerSummaryDescriptionSnapshot;
        long now = System.currentTimeMillis();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void requirePending() {
        if (candidateStatus != ExecutionCandidateStatus.PENDING) {
            throw new BusinessValidationException(
                    "Only pending execution candidates can be reviewed");
        }
    }

    public void markApproved(Long managerId, Long inspectionId, String decisionNotes) {
        requirePending();
        this.candidateStatus = ExecutionCandidateStatus.APPROVED;
        this.createdInspectionId = inspectionId;
        this.decisionNotes = decisionNotes;
        this.decidedByUserId = managerId;
        this.decidedAt = System.currentTimeMillis();
        this.updatedAt = this.decidedAt;
    }

    public void markRejected(Long managerId, String reason) {
        requirePending();
        this.candidateStatus = ExecutionCandidateStatus.REJECTED;
        this.rejectionReason = reason;
        this.decidedByUserId = managerId;
        this.decidedAt = System.currentTimeMillis();
        this.updatedAt = this.decidedAt;
    }

    public void markDismissed(Long managerId, String comment) {
        requirePending();
        this.candidateStatus = ExecutionCandidateStatus.DISMISSED;
        this.dismissComment = comment;
        this.decidedByUserId = managerId;
        this.decidedAt = System.currentTimeMillis();
        this.updatedAt = this.decidedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PreventiveMaintenancePlan getPreventiveMaintenancePlan() {
        return preventiveMaintenancePlan;
    }

    public Asset getAsset() {
        return asset;
    }

    public ExecutionCandidateStatus getCandidateStatus() {
        return candidateStatus;
    }

    public PlanTriggerType getTriggerType() {
        return triggerType;
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
