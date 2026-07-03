package com.infratrack.organization.policy.approval;

import com.infratrack.workorder.WorkType;

/**
 * Organizational approval defaults (BDR-004).
 *
 * <p>Approval policy determines whether an approval step is required before a workflow continues.
 * It does not change maintenance execution, inspection execution, issue creation, or audit trails.
 */
public interface ApprovalPolicy {

    boolean requiresCompletionReview(WorkType workType);

    boolean requiresManagerOperationalDecision();

    boolean requiresSuggestedActionApproval();

    boolean requiresPreventiveCandidateApproval();
}
