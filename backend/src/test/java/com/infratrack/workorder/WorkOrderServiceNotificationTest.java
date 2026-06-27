package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.notification.NotificationService;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.dto.AssignWorkOrderRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceNotificationTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private OperationalDecisionRepository operationalDecisionRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserNameLookup userNameLookup;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserRepository userRepository;

    private WorkOrderService workOrderService;

    @BeforeEach
    void setUp() {
        OperationalEventNotificationService operationalEventNotificationService =
                new OperationalEventNotificationService(notificationService, userRepository);
        WorkOrderAuthorizationService authorizationService = new WorkOrderAuthorizationService(userService);
        WorkOrderHistoryRecorder historyRecorder = new WorkOrderHistoryRecorder(assetHistoryEventRepository);
        workOrderService = new WorkOrderService(
                workOrderRepository,
                operationalDecisionRepository,
                authorizationService,
                historyRecorder,
                userService,
                userNameLookup,
                operationalEventNotificationService);
    }

    @Test
    void assignWorkOrder_shouldNotRollbackWhenNotificationFails() {
        AssignWorkOrderRequest request = new AssignWorkOrderRequest();
        request.setAssignedToUserId(20L);
        request.setAssignedAt(LocalDateTime.now().minusMinutes(5));

        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(notificationService.create(anyLong(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("notification failed"));

        var response = workOrderService.assignWorkOrder(1000L, request, 40L);

        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.ASSIGNED);
        verify(workOrderRepository).save(any(WorkOrder.class));
        verify(assetHistoryEventRepository).save(any());
        verify(notificationService).create(
                eq(20L),
                eq(OperationalEventNotificationService.WORK_ORDER_ASSIGNED_TITLE),
                eq(OperationalEventNotificationService.WORK_ORDER_ASSIGNED_MESSAGE),
                eq(OperationalEventNotificationService.WORK_ORDERS_ROUTE));
    }

    private WorkOrder createdWorkOrder(Long id, WorkType workType) {
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        WorkOrder workOrder = new WorkOrder(
                decision,
                decision.getAsset(),
                workType,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusHours(1));
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
                LocalDateTime.now().minusHours(2));
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
                LocalDate.now().plusDays(7));
        inspection.setId(100L);
        inspection.complete(
                PhysicalCondition.POOR,
                "Damaged swing chain observed",
                true,
                LocalDateTime.now().minusHours(3),
                20L);
        Issue issue = new Issue(
                inspection,
                trigger.getAsset(),
                "Broken swing chain requires replacement",
                IssueSeverity.HIGH,
                20L,
                LocalDateTime.now().minusHours(2).minusMinutes(30));
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
                10L);
        asset.setId(5L);
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
                false,
                10L);
        trigger.setId(1L);
        return trigger;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
