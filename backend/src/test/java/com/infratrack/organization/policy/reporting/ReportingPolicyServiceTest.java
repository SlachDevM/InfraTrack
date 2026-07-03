package com.infratrack.organization.policy.reporting;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReportingPolicyServiceTest {

    @Test
    void getPolicy_shouldReturnDefaultReportingPolicy() {
        ReportingPolicyService service = new ReportingPolicyService();

        assertThat(service.getPolicy()).isInstanceOf(DefaultReportingPolicy.class);
    }
}
