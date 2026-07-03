package com.infratrack.organization.policy.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPolicyServiceTest {

    @Test
    void getPolicy_shouldReturnDefaultNotificationPolicy() {
        NotificationPolicyService service = new NotificationPolicyService();

        assertThat(service.getPolicy()).isInstanceOf(DefaultNotificationPolicy.class);
    }
}
