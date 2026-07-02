package com.infratrack.notification;

import com.infratrack.exception.ForbiddenOperationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationAuthorizationServiceTest {

    private NotificationAuthorizationService authorizationService;

    @BeforeEach
    void setUp() {
        authorizationService = new NotificationAuthorizationService();
    }

    @Test
    void requireNotificationOwner_shouldAllowOwner() {
        Notification notification = new Notification(5L, "Title", "Message");

        assertThatCode(() -> authorizationService.requireNotificationOwner(5L, notification))
                .doesNotThrowAnyException();
    }

    @Test
    void requireNotificationOwner_shouldRejectDifferentUser() {
        Notification notification = new Notification(5L, "Title", "Message");

        assertThatThrownBy(() -> authorizationService.requireNotificationOwner(9L, notification))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only mark your own notifications as read.");
    }

    @Test
    void requireNotificationOwner_shouldRejectAdministratorForAnotherUsersNotification() {
        Notification notification = new Notification(5L, "Title", "Message");

        assertThatThrownBy(() -> authorizationService.requireNotificationOwner(1L, notification))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only mark your own notifications as read.");
    }
}
