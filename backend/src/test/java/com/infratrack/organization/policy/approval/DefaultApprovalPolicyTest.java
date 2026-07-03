package com.infratrack.organization.policy.approval;

import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultApprovalPolicyTest {

    private DefaultApprovalPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DefaultApprovalPolicy();
    }

    @Test
    void requiresCompletionReview_shouldBeTrueForContractorWork() {
        assertThat(policy.requiresCompletionReview(WorkType.CONTRACTOR_WORK)).isTrue();
    }

    @Test
    void requiresCompletionReview_shouldBeFalseForInternalMaintenance() {
        assertThat(policy.requiresCompletionReview(WorkType.INTERNAL_MAINTENANCE)).isFalse();
    }

    @Test
    void requiresManagerOperationalDecision_shouldBeTrue() {
        assertThat(policy.requiresManagerOperationalDecision()).isTrue();
    }

    @Test
    void requiresSuggestedActionApproval_shouldBeTrue() {
        assertThat(policy.requiresSuggestedActionApproval()).isTrue();
    }

    @Test
    void requiresPreventiveCandidateApproval_shouldBeTrue() {
        assertThat(policy.requiresPreventiveCandidateApproval()).isTrue();
    }
}
