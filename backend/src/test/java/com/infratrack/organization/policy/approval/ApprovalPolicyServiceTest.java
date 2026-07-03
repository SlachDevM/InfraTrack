package com.infratrack.organization.policy.approval;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApprovalPolicyServiceTest {

    @Test
    void getPolicy_shouldReturnDefaultApprovalPolicy() {
        ApprovalPolicyService service = new ApprovalPolicyService();

        assertThat(service.getPolicy()).isInstanceOf(DefaultApprovalPolicy.class);
    }
}
