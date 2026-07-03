package com.infratrack.organization.policy.notification;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationPolicyServiceTest {

    @Test
    void getPolicy_shouldReturnDefaultNotificationPolicyByDefault() {
        NotificationPolicyService service = new NotificationPolicyService("DEFAULT");

        assertThat(service.getMode()).isEqualTo(NotificationPolicyMode.DEFAULT);
        assertThat(service.getPolicy()).isInstanceOf(DefaultNotificationPolicy.class);
    }

    @Test
    void getPolicy_shouldReturnQuietNotificationPolicy_whenConfigured() {
        NotificationPolicyService service = new NotificationPolicyService("QUIET");

        assertThat(service.getMode()).isEqualTo(NotificationPolicyMode.QUIET);
        assertThat(service.getPolicy()).isInstanceOf(QuietNotificationPolicy.class);
    }

    @Test
    void getPolicy_shouldDefaultToDefault_whenBlank() {
        NotificationPolicyService service = new NotificationPolicyService("  ");

        assertThat(service.getMode()).isEqualTo(NotificationPolicyMode.DEFAULT);
        assertThat(service.getPolicy()).isInstanceOf(DefaultNotificationPolicy.class);
    }

    @Test
    void constructor_shouldFailFast_whenInvalidValueProvided() {
        assertThatThrownBy(() -> new NotificationPolicyService("NOISY"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid notification policy mode");
    }
}
