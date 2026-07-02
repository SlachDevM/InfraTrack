package com.infratrack.maintenanceactivity;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.completionreview.CompletionReviewAuthorizationService;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.maintenanceactivity.dto.CompleteMaintenanceActivityRequest;
import com.infratrack.notification.NotificationService;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.time.WorkflowClock;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceActivityServiceNotificationTest {

    @Mock
    private MaintenanceActivityRepository maintenanceActivityRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @Mock
    private CompletionReviewRepository completionReviewRepository;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompletionReviewAuthorizationService completionReviewAuthorizationService;

    private MaintenanceActivityService maintenanceActivityService;

    @BeforeEach
    void setUp() {
        OperationalEventNotificationService operationalEventNotificationService =
                new OperationalEventNotificationService(notificationService, userRepository);
        maintenanceActivityService = new MaintenanceActivityService(
                maintenanceActivityRepository,
                workOrderRepository,
                assetHistoryEventRepository,
                userService,
                completionReviewRepository,
                operationalEventNotificationService,
                completionReviewAuthorizationService,
                new WorkflowClock(java.time.Clock.systemDefaultZone()));
    }

    @Test
    void completeMaintenance_shouldNotifyOperationalCoordinatorsInAssetDepartment() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR, workOrder.getAsset().getDepartment());

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> {
            MaintenanceActivity activity = invocation.getArgument(0);
            activity.setId(5000L);
            return activity;
        });
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByRoleAndDepartmentId(UserRole.OPERATIONAL_COORDINATOR, 1L))
                .thenReturn(List.of(coordinator));

        maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        verify(notificationService).create(
                40L,
                OperationalEventNotificationService.MAINTENANCE_COMPLETED_TITLE,
                OperationalEventNotificationService.MAINTENANCE_COMPLETED_MESSAGE,
                OperationalEventNotificationService.WORK_ORDERS_ROUTE);
        verify(userRepository, never()).findByRoleAndDepartmentId(eq(UserRole.MANAGER), anyLong());
    }

    @Test
    void completeMaintenance_shouldSucceedWhenNoOperationalCoordinatorExists() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> {
            MaintenanceActivity activity = invocation.getArgument(0);
            activity.setId(5000L);
            return activity;
        });
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByRoleAndDepartmentId(UserRole.OPERATIONAL_COORDINATOR, 1L))
                .thenReturn(List.of());

        var response = maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        assertThat(response.getWorkOrderStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        verify(notificationService, never()).create(anyLong(), anyString(), anyString(), anyString());
    }

    @Test
    void completeMaintenance_shouldNotifyManagersForContractorWork() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.CONTRACTOR_WORK, 25L);
        User contractor = user(25L, UserRole.CONTRACTOR);
        User manager = user(30L, UserRole.MANAGER, workOrder.getAsset().getDepartment());

        when(userService.getById(25L)).thenReturn(contractor);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> {
            MaintenanceActivity activity = invocation.getArgument(0);
            activity.setId(5001L);
            return activity;
        });
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByRoleAndDepartmentId(UserRole.OPERATIONAL_COORDINATOR, 1L))
                .thenReturn(List.of());
        when(userRepository.findByRoleAndDepartmentId(UserRole.MANAGER, 1L))
                .thenReturn(List.of(manager));

        maintenanceActivityService.completeMaintenance(1000L, request, 25L);

        verify(notificationService).create(
                30L,
                OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_TITLE,
                OperationalEventNotificationService.COMPLETION_REVIEW_REQUIRED_MESSAGE,
                OperationalEventNotificationService.WORK_ORDERS_ROUTE);
    }

    @Test
    void completeMaintenance_shouldNotNotifyManagersForInternalMaintenance() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByRoleAndDepartmentId(UserRole.OPERATIONAL_COORDINATOR, 1L))
                .thenReturn(List.of());

        maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        verify(userRepository, never()).findByRoleAndDepartmentId(eq(UserRole.MANAGER), anyLong());
    }

    @Test
    void completeMaintenance_shouldNotRollbackWhenNotificationFails() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR, workOrder.getAsset().getDepartment());

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> {
            MaintenanceActivity activity = invocation.getArgument(0);
            activity.setId(5000L);
            return activity;
        });
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByRoleAndDepartmentId(UserRole.OPERATIONAL_COORDINATOR, 1L))
                .thenReturn(List.of(coordinator));
        doThrow(new RuntimeException("notification failed"))
                .when(notificationService)
                .create(anyLong(), anyString(), anyString(), anyString());

        var response = maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        assertThat(response.getWorkOrderStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        verify(maintenanceActivityRepository).save(any(MaintenanceActivity.class));
        verify(workOrderRepository).save(any(WorkOrder.class));
        verify(assetHistoryEventRepository).save(any());
    }

    private CompleteMaintenanceActivityRequest validRequest() {
        CompleteMaintenanceActivityRequest request = new CompleteMaintenanceActivityRequest();
        request.setCompletionNotes("Replaced damaged swing chain");
        request.setCompletedAt(LocalDateTime.now().minusMinutes(1));
        return request;
    }

    private WorkOrder assignedWorkOrder(Long id, WorkType workType, Long assignedToUserId) {
        WorkOrder workOrder = createdWorkOrder(id, workType);
        workOrder.assign(assignedToUserId, 40L, LocalDateTime.now().minusMinutes(10));
        return workOrder;
    }

    private WorkOrder createdWorkOrder(Long id, WorkType workType) {
        OperationalDecisionOutcome outcome = workType == WorkType.INTERNAL_MAINTENANCE
                ? OperationalDecisionOutcome.INTERNAL_MAINTENANCE
                : OperationalDecisionOutcome.CONTRACTOR_WORK;
        OperationalDecision decision = decision(900L, outcome);
        WorkOrder workOrder = new WorkOrder(
                decision,
                decision.getAsset(),
                workType,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusHours(1)
        );
        workOrder.setId(id);
        return workOrder;
    }

    private OperationalDecision decision(Long id, OperationalDecisionOutcome outcome) {
        Issue issue = issue(500L);
        OperationalDecision decision = new OperationalDecision(
                issue,
                issue.getAsset(),
                outcome,
                "Decision rationale",
                30L,
                LocalDateTime.now().minusHours(1)
        );
        decision.setId(id);
        return decision;
    }

    private Issue issue(Long id) {
        BusinessTrigger trigger = businessTrigger();
        Inspection inspection = new Inspection(
                trigger.getAsset(),
                trigger,
                20L,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7)
        );
        inspection.setId(100L);
        inspection.complete(
                PhysicalCondition.POOR,
                "Damaged swing chain observed",
                true,
                LocalDateTime.now().minusHours(2),
                20L
        );

        Issue issue = new Issue(
                inspection,
                trigger.getAsset(),
                "Broken swing chain requires replacement",
                IssueSeverity.HIGH,
                20L,
                LocalDateTime.now().minusHours(1).minusMinutes(30)
        );
        issue.setId(id);
        return issue;
    }

    private BusinessTrigger businessTrigger() {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L
        );
        asset.setId(5L);
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
                false,
                10L
        );
        trigger.setId(1L);
        return trigger;
    }

    private User user(Long id, UserRole role) {
        return user(id, role, null);
    }

    private User user(Long id, UserRole role, Department department) {
        User user = new User("user" + id + "@test.com", "password", "User " + id, role);
        user.setId(id);
        user.setEnabled(true);
        user.setDepartment(department);
        return user;
    }
}
