package com.infratrack.notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationalEventNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private OperationalEventNotificationService operationalEventNotificationService;

    @Test
    void notifyInspectionAssigned_shouldCreateNotification() {
        operationalEventNotificationService.notifyInspectionAssigned(20L);

        verify(notificationService).create(
                20L,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_TITLE,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_MESSAGE);
    }

    @Test
    void notifyWorkOrderAssigned_shouldCreateNotification() {
        operationalEventNotificationService.notifyWorkOrderAssigned(25L);

        verify(notificationService).create(
                25L,
                OperationalEventNotificationService.WORK_ORDER_ASSIGNED_TITLE,
                OperationalEventNotificationService.WORK_ORDER_ASSIGNED_MESSAGE);
    }

    @Test
    void notifyInspectionAssigned_shouldNotPropagateFailure() {
        doThrow(new RuntimeException("FCM unavailable"))
                .when(notificationService)
                .create(anyLong(), anyString(), anyString());

        operationalEventNotificationService.notifyInspectionAssigned(20L);

        verify(notificationService).create(
                20L,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_TITLE,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_MESSAGE);
    }
}
