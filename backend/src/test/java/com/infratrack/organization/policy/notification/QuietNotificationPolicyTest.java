package com.infratrack.organization.policy.notification;

import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class QuietNotificationPolicyTest {

    private QuietNotificationPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new QuietNotificationPolicy();
    }

    @Test
    void shouldNotifyInspectionCompletion_shouldReturnTrue() {
        assertThat(policy.shouldNotifyInspectionCompletion()).isTrue();
    }

    @Test
    void shouldNotifyWorkOrderAssignment_shouldReturnTrue() {
        assertThat(policy.shouldNotifyWorkOrderAssignment()).isTrue();
    }

    @Test
    void shouldNotifyMaintenanceCompleted_shouldReturnFalse() {
        assertThat(policy.shouldNotifyMaintenanceCompleted()).isFalse();
    }

    @Test
    void shouldNotifyCompletionReview_shouldReturnTrueForContractorWork() {
        assertThat(policy.shouldNotifyCompletionReview(WorkType.CONTRACTOR_WORK)).isTrue();
    }

    @Test
    void shouldNotifyCompletionReview_shouldReturnFalseForInternalMaintenance() {
        assertThat(policy.shouldNotifyCompletionReview(WorkType.INTERNAL_MAINTENANCE)).isFalse();
    }

    @Test
    void shouldNotifyReworkIssueRequiresOperationalDecision_shouldReturnTrueForReworkRequired() {
        assertThat(policy.shouldNotifyReworkIssueRequiresOperationalDecision(
                CompletionReviewDecision.REWORK_REQUIRED)).isTrue();
    }

    @Test
    void shouldNotifySuggestedAction_shouldReturnFalse() {
        assertThat(policy.shouldNotifySuggestedAction()).isFalse();
    }

    @Test
    void shouldNotifyPreventiveCandidate_shouldReturnFalse() {
        assertThat(policy.shouldNotifyPreventiveCandidate()).isFalse();
    }
}
