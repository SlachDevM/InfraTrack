package com.infratrack.organization.policy.notification;

import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.workorder.WorkType;

/**
 * Default notification policy matching the original fixed InfraTrack behaviour.
 */
public class DefaultNotificationPolicy implements NotificationPolicy {

    @Override
    public boolean shouldNotifyInspectionCompletion() {
        return true;
    }

    @Override
    public boolean shouldNotifyWorkOrderAssignment() {
        return true;
    }

    @Override
    public boolean shouldNotifyMaintenanceCompleted() {
        return true;
    }

    @Override
    public boolean shouldNotifyCompletionReview(WorkType workType) {
        return workType == WorkType.CONTRACTOR_WORK;
    }

    @Override
    public boolean shouldNotifyReworkIssueRequiresOperationalDecision(CompletionReviewDecision decision) {
        return decision == CompletionReviewDecision.REWORK_REQUIRED;
    }

    @Override
    public boolean shouldNotifySuggestedAction() {
        return false;
    }

    @Override
    public boolean shouldNotifyPreventiveCandidate() {
        return false;
    }
}
