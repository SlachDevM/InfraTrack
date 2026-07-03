package com.infratrack.organization.policy.notification;

import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.workorder.WorkType;

/**
 * Decides whether operational notifications should be sent for a given business event (BDR-004).
 *
 * <p>Notification delivery remains in {@link com.infratrack.notification.OperationalEventNotificationService};
 * this policy governs only the send/do-not-send decision.
 */
public interface NotificationPolicy {

    /**
     * Whether the inspection assignee should be notified when an inspection is assigned,
     * including inspection creation from an approved preventive candidate.
     */
    boolean shouldNotifyInspectionCompletion();

    boolean shouldNotifyWorkOrderAssignment();

    boolean shouldNotifyMaintenanceCompleted();

    boolean shouldNotifyCompletionReview(WorkType workType);

    boolean shouldNotifyReworkIssueRequiresOperationalDecision(CompletionReviewDecision decision);

    boolean shouldNotifySuggestedAction();

    boolean shouldNotifyPreventiveCandidate();
}
