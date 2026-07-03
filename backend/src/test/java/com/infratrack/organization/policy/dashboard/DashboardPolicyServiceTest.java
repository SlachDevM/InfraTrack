package com.infratrack.organization.policy.dashboard;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardPolicyServiceTest {

    @Test
    void getPolicy_shouldReturnDefaultDashboardPolicy() {
        DashboardPolicyService service = new DashboardPolicyService();

        assertThat(service.getPolicy()).isInstanceOf(DefaultDashboardPolicy.class);
    }
}
