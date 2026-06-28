package com.infratrack.notification;

import com.infratrack.department.Department;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationalEventNotificationServiceTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OperationalEventNotificationService operationalEventNotificationService;

    @Test
    void notifyInspectionAssigned_shouldCreateNotificationWithRoute() {
        operationalEventNotificationService.notifyInspectionAssigned(20L);

        verify(notificationService).create(
                20L,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_TITLE,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_MESSAGE,
                OperationalEventNotificationService.INSPECTIONS_ROUTE);
    }

    @Test
    void notifyWorkOrderAssigned_shouldCreateNotificationWithRoute() {
        operationalEventNotificationService.notifyWorkOrderAssigned(25L);

        verify(notificationService).create(
                25L,
                OperationalEventNotificationService.WORK_ORDER_ASSIGNED_TITLE,
                OperationalEventNotificationService.WORK_ORDER_ASSIGNED_MESSAGE,
                OperationalEventNotificationService.WORK_ORDERS_ROUTE);
    }

    @Test
    void notifyInspectionAssigned_shouldNotPropagateFailure() {
        doThrow(new RuntimeException("FCM unavailable"))
                .when(notificationService)
                .create(anyLong(), anyString(), anyString(), anyString());

        operationalEventNotificationService.notifyInspectionAssigned(20L);

        verify(notificationService).create(
                20L,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_TITLE,
                OperationalEventNotificationService.INSPECTION_ASSIGNED_MESSAGE,
                OperationalEventNotificationService.INSPECTIONS_ROUTE);
    }

    @Test
    void notifyMaintenanceCompleted_shouldNotifyOperationalCoordinatorsInDepartment() {
        Department department = department(1L);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR, department);

        when(userRepository.findByRoleAndDepartmentId(UserRole.OPERATIONAL_COORDINATOR, 1L))
                .thenReturn(List.of(coordinator));

        operationalEventNotificationService.notifyMaintenanceCompleted(department);

        verify(notificationService).create(
                40L,
                OperationalEventNotificationService.MAINTENANCE_COMPLETED_TITLE,
                OperationalEventNotificationService.MAINTENANCE_COMPLETED_MESSAGE,
                OperationalEventNotificationService.WORK_ORDERS_ROUTE);
    }

    @Test
    void notifyMaintenanceCompleted_shouldDoNothingWhenNoRecipients() {
        Department department = department(1L);

        when(userRepository.findByRoleAndDepartmentId(UserRole.OPERATIONAL_COORDINATOR, 1L))
                .thenReturn(List.of());

        operationalEventNotificationService.notifyMaintenanceCompleted(department);

        verify(notificationService, never()).create(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void notifyCompletionReviewRequired_shouldNotifyManagersInDepartment() {
        Department department = department(1L);
        User manager = user(30L, UserRole.MANAGER, department);

        when(userRepository.findByRoleAndDepartmentId(UserRole.MANAGER, 1L))
                .thenReturn(List.of(manager));

        operationalEventNotificationService.notifyCompletionReviewRequired(department);

        verify(notificationService).create(
                30L,
                OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_TITLE,
                OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_MESSAGE,
                OperationalEventNotificationService.WORK_ORDERS_ROUTE);
    }

    @Test
    void notifyCompletionReviewRequired_shouldNotifyEachMatchingManager() {
        Department department = department(1L);
        User managerOne = user(30L, UserRole.MANAGER, department);
        User managerTwo = user(31L, UserRole.MANAGER, department);

        when(userRepository.findByRoleAndDepartmentId(UserRole.MANAGER, 1L))
                .thenReturn(List.of(managerOne, managerTwo));

        operationalEventNotificationService.notifyCompletionReviewRequired(department);

        verify(notificationService).create(
                eq(30L),
                eq(OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_TITLE),
                eq(OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_MESSAGE),
                eq(OperationalEventNotificationService.WORK_ORDERS_ROUTE));
        verify(notificationService).create(
                eq(31L),
                eq(OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_TITLE),
                eq(OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_MESSAGE),
                eq(OperationalEventNotificationService.WORK_ORDERS_ROUTE));
    }

    @Test
    void notifyReworkIssueRequiresOperationalDecision_shouldNotifyEnabledManagersInDepartment() {
        Department department = department(1L);
        User manager = user(30L, UserRole.MANAGER, department);
        manager.setEnabled(true);

        when(userRepository.findByRoleAndDepartmentIdAndEnabledTrueOrderByNameAsc(UserRole.MANAGER, 1L))
                .thenReturn(List.of(manager));

        operationalEventNotificationService.notifyReworkIssueRequiresOperationalDecision(department, 8001L);

        verify(notificationService).create(
                30L,
                OperationalEventNotificationService.REWORK_ISSUE_REQUIRES_DECISION_TITLE,
                OperationalEventNotificationService.REWORK_ISSUE_REQUIRES_DECISION_MESSAGE,
                OperationalEventNotificationService.ISSUES_ROUTE);
    }

    @Test
    void notifyReworkIssueRequiresOperationalDecision_shouldNotNotifyDisabledManagers() {
        Department department = department(1L);

        when(userRepository.findByRoleAndDepartmentIdAndEnabledTrueOrderByNameAsc(UserRole.MANAGER, 1L))
                .thenReturn(List.of());

        operationalEventNotificationService.notifyReworkIssueRequiresOperationalDecision(department, 8001L);

        verify(notificationService, never()).create(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void notifyReworkIssueRequiresOperationalDecision_shouldNotNotifyCrossDepartmentManagers() {
        Department assetDepartment = department(1L);

        when(userRepository.findByRoleAndDepartmentIdAndEnabledTrueOrderByNameAsc(UserRole.MANAGER, 1L))
                .thenReturn(List.of());

        operationalEventNotificationService.notifyReworkIssueRequiresOperationalDecision(assetDepartment, 8001L);

        verify(userRepository, never()).findByRoleAndDepartmentId(any(), eq(2L));
        verify(notificationService, never()).create(anyLong(), anyString(), anyString(), anyString());
    }

    private Department department(Long id) {
        Department department = new Department("Parks");
        department.setId(id);
        return department;
    }

    private User user(Long id, UserRole role, Department department) {
        User user = new User("user" + id + "@test.com", "password", "User " + id, role);
        user.setId(id);
        user.setDepartment(department);
        return user;
    }
}
