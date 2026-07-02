package com.infratrack.notification;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private FirebaseNotificationService firebaseNotificationService;

    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                userRepository,
                firebaseNotificationService,
                new NotificationAuthorizationService());
    }

    @Test
    void getUnreadCount_shouldReturnRepositoryCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);

        long count = notificationService.getUnreadCount(1L);

        assertThat(count).isEqualTo(3L);
    }

    @Test
    void markAsRead_shouldSetNotificationAsRead_whenOwnerMarksOwnNotification() {
        Notification notification = new Notification(1L, "Test Title", "Test message");
        notification.setId(5L);
        notification.setIsRead(false);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(5L, 1L);

        assertThat(result.getIsRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldRemainIdempotent_whenNotificationAlreadyRead() {
        Notification notification = new Notification(1L, "Test Title", "Test message");
        notification.setId(5L);
        notification.setIsRead(true);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(notification)).thenReturn(notification);

        Notification result = notificationService.markAsRead(5L, 1L);

        assertThat(result.getIsRead()).isTrue();
        verify(notificationRepository).save(notification);
    }

    @Test
    void markAsRead_shouldThrowForbidden_whenDifferentUserAttemptsMarkAsRead() {
        Notification notification = new Notification(1L, "Test Title", "Test message");
        notification.setId(5L);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(5L, 2L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only mark your own notifications as read.");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_shouldThrowForbidden_whenAdministratorMarksAnotherUsersNotification() {
        Notification notification = new Notification(10L, "Test Title", "Test message");
        notification.setId(5L);

        when(notificationRepository.findById(5L)).thenReturn(Optional.of(notification));

        assertThatThrownBy(() -> notificationService.markAsRead(5L, 1L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only mark your own notifications as read.");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void markAsRead_shouldThrowNotFound_whenNotificationDoesNotExist() {
        when(notificationRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(99L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("404");

        verify(notificationRepository, never()).save(any());
    }

    @Test
    void create_shouldCreateUnreadNotification() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.create(
                1L,
                "Test Title",
                "You have been assigned"
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();

        assertThat(saved.getUserId()).isEqualTo(1L);
        assertThat(saved.getTitle()).isEqualTo("Test Title");
        assertThat(saved.getMessage()).isEqualTo("You have been assigned");
        assertThat(saved.getIsRead()).isFalse();
        assertThat(saved.getTargetRoute()).isNull();

        assertThat(result.getIsRead()).isFalse();
    }

    @Test
    void create_shouldPersistTargetRouteWhenProvided() {
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.create(
                1L,
                "Work Order Assigned",
                "You have been assigned a work order.",
                "/work-orders"
        );

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        assertThat(captor.getValue().getTargetRoute()).isEqualTo("/work-orders");
        assertThat(result.getTargetRoute()).isEqualTo("/work-orders");
    }

    @Test
    void markAllAsRead_shouldMarkEveryUnreadNotificationAsRead() {
        Notification first = new Notification(1L, "Title 1", "First");
        Notification second = new Notification(1L, "Title 2", "Second");

        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(first, second));

        notificationService.markAllAsRead(1L);

        assertThat(first.getIsRead()).isTrue();
        assertThat(second.getIsRead()).isTrue();

        verify(notificationRepository).saveAll(List.of(first, second));
    }
}
