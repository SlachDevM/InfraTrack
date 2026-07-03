package com.infratrack.organization.policy.approval;

import com.infratrack.workorder.WorkType;

/**
 * Default approval policy matching the original fixed InfraTrack behaviour.
 */
public class DefaultApprovalPolicy implements ApprovalPolicy {

    @Override
    public boolean requiresCompletionReview(WorkType workType) {
        return workType == WorkType.CONTRACTOR_WORK;
    }

    @Override
    public boolean requiresManagerOperationalDecision() {
        return true;
    }

    @Override
    public boolean requiresSuggestedActionApproval() {
        return true;
    }

    @Override
    public boolean requiresPreventiveCandidateApproval() {
        return true;
    }
}
