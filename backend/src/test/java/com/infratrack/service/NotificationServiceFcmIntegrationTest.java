package com.infratrack.service;

import com.infratrack.model.Notification;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.repository.NotificationRepository;
import com.infratrack.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceFcmIntegrationTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseNotificationService firebaseNotificationService;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void create_shouldPersistNotificationAndSendFcm() {
        User user = new User("user@test.com", "password", "Test User", UserRole.FIELD_EMPLOYEE);
        user.setId(1L);
        user.setFcmToken("valid-fcm-token");

        Notification notification = new Notification(1L, "Test Notification", "You have a notification");
        notification.setId(1L);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Notification result = notificationService.create(1L, "Test Notification", "You have a notification");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getMessage()).isEqualTo("You have a notification");

        verify(notificationRepository).save(any(Notification.class));
        verify(userRepository).findById(1L);
        verify(firebaseNotificationService).sendToUser(eq(user), anyString(), anyString(), anyMap());
    }

    @Test
    void create_shouldPersistNotificationEvenIfFcmFails() {
        User user = new User("user@test.com", "password", "Test User", UserRole.FIELD_EMPLOYEE);
        user.setId(1L);
        user.setFcmToken("valid-fcm-token");

        Notification notification = new Notification(1L, "Test Notification", "You have a notification");
        notification.setId(1L);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new RuntimeException("Firebase error")).when(firebaseNotificationService).sendToUser(any(User.class), anyString(), anyString(), anyMap());

        Notification result = notificationService.create(1L, "Test Notification", "You have a notification");

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);

        verify(notificationRepository).save(any(Notification.class));
        verify(firebaseNotificationService).sendToUser(any(User.class), anyString(), anyString(), anyMap());
    }

    @Test
    void create_shouldSkipFcmIfUserNotFound() {
        Notification notification = new Notification(1L, "Test Notification", "You have a notification");
        notification.setId(1L);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Notification result = notificationService.create(1L, "Test Notification", "You have a notification");

        assertThat(result).isNotNull();

        verify(notificationRepository).save(any(Notification.class));
        verify(firebaseNotificationService, never()).sendToUser(any(User.class), anyString(), anyString(), anyMap());
    }

    @Test
    void create_shouldSendCustomTitleAndMessage() {
        User user = new User("user@test.com", "password", "Test User", UserRole.FIELD_EMPLOYEE);
        user.setId(1L);
        user.setFcmToken("valid-fcm-token");

        Notification notification = new Notification(1L, "Custom Title", "Custom notification message");
        notification.setId(2L);

        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        notificationService.create(1L, "Custom Title", "Custom notification message");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        ArgumentCaptor<String> titleCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(firebaseNotificationService).sendToUser(userCaptor.capture(), titleCaptor.capture(), bodyCaptor.capture(), anyMap());

        assertThat(titleCaptor.getValue()).isEqualTo("Custom Title");
        assertThat(bodyCaptor.getValue()).isEqualTo("Custom notification message");
    }
}
