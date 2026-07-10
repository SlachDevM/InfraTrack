package com.infratrack.maintenanceactivity;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.exception.ConflictException;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
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
import com.infratrack.completionreview.CompletionReview;
import com.infratrack.completionreview.CompletionReviewAuthorizationService;
import com.infratrack.completionreview.CompletionReviewDecision;
import com.infratrack.completionreview.CompletionReviewRepository;
import com.infratrack.maintenanceactivity.dto.CompleteMaintenanceActivityRequest;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.organization.policy.approval.DefaultApprovalPolicy;
import com.infratrack.organization.policy.notification.DefaultNotificationPolicy;
import com.infratrack.organization.policy.approval.ApprovalPolicyService;
import com.infratrack.organization.policy.notification.NotificationPolicyService;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.time.WorkflowClock;
import com.infratrack.user.User;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.Mockito.lenient;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceActivityServiceTest {

    private static final LocalDateTime FIXED_NOW = LocalDateTime.of(2026, 7, 2, 10, 0);

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
    private OperationalEventNotificationService operationalEventNotificationService;

    @Mock
    private CompletionReviewAuthorizationService completionReviewAuthorizationService;

    @Mock
    private WorkflowClock workflowClock;

    @Mock
    private MaintenanceActivityAuthorizationService maintenanceActivityAuthorizationService;

    @Mock
    private NotificationPolicyService notificationPolicyService;

    @Mock
    private ApprovalPolicyService approvalPolicyService;

    @InjectMocks
    private MaintenanceActivityService maintenanceActivityService;

    @BeforeEach
    void setUpClock() {
        lenient().when(workflowClock.now()).thenReturn(FIXED_NOW);
        lenient().when(notificationPolicyService.getPolicy()).thenReturn(new DefaultNotificationPolicy());
        lenient().when(approvalPolicyService.getPolicy()).thenReturn(new DefaultApprovalPolicy());
    }

    @Test
    void completeMaintenance_shouldAllowAssignedFieldEmployeeForInternalMaintenance() {
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

        var response = maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        assertThat(response.getWorkOrderId()).isEqualTo(1000L);
        assertThat(response.getAssetId()).isEqualTo(5L);
        assertThat(response.getPerformedByUserId()).isEqualTo(20L);
        assertThat(response.getWorkOrderStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
    }

    @Test
    void completeMaintenance_shouldAllowAssignedContractorForContractorWork() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.CONTRACTOR_WORK, 25L);
        User contractor = user(25L, UserRole.CONTRACTOR);

        when(userService.getById(25L)).thenReturn(contractor);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> {
            MaintenanceActivity activity = invocation.getArgument(0);
            activity.setId(5001L);
            return activity;
        });
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = maintenanceActivityService.completeMaintenance(1000L, request, 25L);

        assertThat(response.getWorkOrderStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(response.getPerformedByUserId()).isEqualTo(25L);
    }

    @Test
    void completeMaintenance_shouldLinkMaintenanceActivityToWorkOrderAndAsset() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        ArgumentCaptor<MaintenanceActivity> activityCaptor = ArgumentCaptor.forClass(MaintenanceActivity.class);
        verify(maintenanceActivityRepository).save(activityCaptor.capture());
        MaintenanceActivity saved = activityCaptor.getValue();
        assertThat(saved.getWorkOrder().getId()).isEqualTo(1000L);
        assertThat(saved.getAsset().getId()).isEqualTo(5L);
        assertThat(saved.getCompletionNotes()).isEqualTo("Replaced damaged swing chain");
    }

    @Test
    void completeMaintenance_shouldSetWorkOrderStatusToCompleted() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        ArgumentCaptor<WorkOrder> workOrderCaptor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(workOrderCaptor.capture());
        assertThat(workOrderCaptor.getValue().getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
    }

    @Test
    void completeMaintenance_shouldCreateAssetHistoryEvent() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.MAINTENANCE_COMPLETED);
        assertThat(historyCaptor.getValue().getAsset().getId()).isEqualTo(5L);
    }

    @Test
    void completeMaintenance_shouldRejectUnassignedUser() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User otherFieldEmployee = user(99L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(99L)).thenReturn(otherFieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 99L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(maintenanceActivityRepository, never()).save(any());
    }

    @Test
    void completeMaintenance_shouldRejectWrongRoleForWorkType() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 25L);
        User contractor = user(25L, UserRole.CONTRACTOR);

        when(userService.getById(25L)).thenReturn(contractor);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 25L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void completeMaintenance_shouldRejectAdministrator() {
        assertRejectedRole(UserRole.ADMINISTRATOR, 40L);
    }

    @Test
    void completeMaintenance_shouldRejectManager() {
        assertRejectedRole(UserRole.MANAGER, 30L);
    }

    @Test
    void completeMaintenance_shouldRejectOperationalCoordinator() {
        assertRejectedRole(UserRole.OPERATIONAL_COORDINATOR, 40L);
    }

    @Test
    void completeMaintenance_shouldRejectWorkOrderNotAssigned() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void completeMaintenance_shouldRejectAlreadyCompletedWorkOrder() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        workOrder.complete();
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void completeMaintenance_shouldRejectDuplicateMaintenanceActivity() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(true);

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void completeMaintenance_shouldRejectBlankCompletionNotes() {
        CompleteMaintenanceActivityRequest request = validRequest();
        request.setCompletionNotes("   ");
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void completeMaintenance_shouldGenerateCompletedAtFromServer_whenClientOmitsTimestamp() {
        CompleteMaintenanceActivityRequest request = validRequest();
        request.setCompletedAt(null);
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        assertThat(response.getCompletedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void completeMaintenance_shouldIgnoreClientProvidedCompletedAt() {
        CompleteMaintenanceActivityRequest request = validRequest();
        request.setCompletedAt(LocalDateTime.now().minusDays(30));
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

        var response = maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        assertThat(response.getCompletedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void completeMaintenance_shouldRejectMissingWorkOrder() {
        CompleteMaintenanceActivityRequest request = validRequest();
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void completeMaintenance_shouldNotChangeAssetStatus() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        AssetStatus originalStatus = workOrder.getAsset().getStatus();
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);
        when(maintenanceActivityRepository.save(any(MaintenanceActivity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        maintenanceActivityService.completeMaintenance(1000L, request, 20L);

        assertThat(workOrder.getAsset().getStatus()).isEqualTo(originalStatus);
    }

    private void assertRejectedRole(UserRole role, Long userId) {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, userId);
        User actor = user(userId, role);

        when(userService.getById(userId)).thenReturn(actor);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, userId))
                .satisfiesAnyOf(
                        thrown -> assertThat(thrown).isInstanceOf(ForbiddenOperationException.class),
                        thrown -> assertThat(thrown).isInstanceOf(ConflictException.class)
                );

        verify(maintenanceActivityRepository, never()).save(any());
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
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }

    private static final Pageable DEFAULT_PAGEABLE = PageRequest.of(0, 20);

    private static Page<MaintenanceActivity> pageOf(MaintenanceActivity... activities) {
        return new PageImpl<>(java.util.List.of(activities), DEFAULT_PAGEABLE, activities.length);
    }

    @Test
    void listPage_shouldReturnAllActivitiesForAdministrator() {
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        MaintenanceActivity activity = completedMaintenanceActivity(5000L, 1000L);

        when(userService.getById(1L)).thenReturn(administrator);
        when(maintenanceActivityRepository.findAllByOrderByCompletedAtDesc(DEFAULT_PAGEABLE))
                .thenReturn(pageOf(activity));
        when(completionReviewRepository.findByMaintenanceActivityIdIn(any()))
                .thenReturn(java.util.List.of());

        var responses = maintenanceActivityService.listPage(1L, DEFAULT_PAGEABLE);

        assertThat(responses.getContent()).hasSize(1);
        verify(maintenanceActivityRepository).findAllByOrderByCompletedAtDesc(DEFAULT_PAGEABLE);
        verify(maintenanceActivityRepository, never()).findAllVisibleToManager(any(), any(), any(), any());
        verify(completionReviewRepository).findByMaintenanceActivityIdIn(any());
        verify(completionReviewRepository, never()).findByMaintenanceActivityId(any());
    }

    @Test
    void listPage_shouldBatchLoadCompletionReviewDecisions() {
        User administrator = user(1L, UserRole.ADMINISTRATOR);
        MaintenanceActivity activity = completedMaintenanceActivity(5000L, 1000L);
        CompletionReview review = new CompletionReview(
                activity,
                activity.getAsset(),
                CompletionReviewDecision.APPROVED,
                "Looks good",
                30L,
                LocalDateTime.of(2026, 7, 2, 11, 0));

        when(userService.getById(1L)).thenReturn(administrator);
        when(maintenanceActivityRepository.findAllByOrderByCompletedAtDesc(DEFAULT_PAGEABLE))
                .thenReturn(pageOf(activity));
        when(completionReviewRepository.findByMaintenanceActivityIdIn(any()))
                .thenReturn(java.util.List.of(review));

        var responses = maintenanceActivityService.listPage(1L, DEFAULT_PAGEABLE);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getCompletionReviewDecision())
                .isEqualTo(CompletionReviewDecision.APPROVED);
        verify(completionReviewRepository).findByMaintenanceActivityIdIn(any());
        verify(completionReviewRepository, never()).findByMaintenanceActivityId(any());
    }

    @Test
    void listPage_shouldReturnDepartmentActivitiesForManager() {
        User manager = managerInDepartment(30L, 1L);
        MaintenanceActivity activity = completedMaintenanceActivity(5000L, 1000L);

        when(userService.getById(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findAllVisibleToManager(
                eq(30L), eq(1L), any(LocalDateTime.class), eq(DEFAULT_PAGEABLE)))
                .thenReturn(pageOf(activity));
        when(completionReviewRepository.findByMaintenanceActivityIdIn(any()))
                .thenReturn(java.util.List.of());

        var responses = maintenanceActivityService.listPage(30L, DEFAULT_PAGEABLE);

        assertThat(responses.getContent()).hasSize(1);
        verify(maintenanceActivityRepository, never()).findAllByOrderByCompletedAtDesc(any());
    }

    @Test
    void listPage_shouldReturnDepartmentActivitiesForOperationalCoordinator() {
        User coordinator = managerInDepartment(40L, 1L);
        coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);
        Department department = new Department("Department 1");
        department.setId(1L);
        coordinator.setDepartment(department);
        MaintenanceActivity activity = completedMaintenanceActivity(5000L, 1000L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(maintenanceActivityRepository.findAllByAsset_Department_IdOrderByCompletedAtDesc(
                eq(1L), eq(DEFAULT_PAGEABLE)))
                .thenReturn(pageOf(activity));
        when(completionReviewRepository.findByMaintenanceActivityIdIn(any()))
                .thenReturn(java.util.List.of());

        var responses = maintenanceActivityService.listPage(40L, DEFAULT_PAGEABLE);

        assertThat(responses.getContent()).hasSize(1);
    }

    @Test
    void listPage_shouldReturnAssignedActivitiesForFieldEmployee() {
        User fieldEmployee = managerInDepartment(20L, 1L);
        fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Department department = new Department("Department 1");
        department.setId(1L);
        fieldEmployee.setDepartment(department);
        MaintenanceActivity activity = completedMaintenanceActivity(5000L, 1000L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(maintenanceActivityRepository.findAllVisibleToAssignee(eq(20L), eq(DEFAULT_PAGEABLE)))
                .thenReturn(pageOf(activity));
        when(completionReviewRepository.findByMaintenanceActivityIdIn(any()))
                .thenReturn(java.util.List.of());

        var responses = maintenanceActivityService.listPage(20L, DEFAULT_PAGEABLE);

        assertThat(responses.getContent()).hasSize(1);
        verify(maintenanceActivityRepository, never()).findAllByOrderByCompletedAtDesc(any());
    }

    @Test
    void getById_shouldReturnActivityWhenAuthorized() {
        User manager = managerInDepartment(30L, 1L);
        MaintenanceActivity activity = completedMaintenanceActivity(5000L, 1000L);

        when(userService.getById(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findDetailedById(5000L)).thenReturn(Optional.of(activity));

        var response = maintenanceActivityService.getById(5000L, 30L);

        assertThat(response.getId()).isEqualTo(5000L);
        verify(maintenanceActivityAuthorizationService).requireCanViewMaintenanceActivity(manager, activity);
    }

    @Test
    void getById_shouldReturn404WhenActivityNotFound() {
        User manager = managerInDepartment(30L, 1L);
        when(userService.getById(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findDetailedById(5000L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceActivityService.getById(5000L, 30L))
                .isInstanceOf(NotFoundException.class);

        verify(maintenanceActivityAuthorizationService, never()).requireCanViewMaintenanceActivity(any(), any());
    }

    @Test
    void getById_shouldReturn403WhenUserCannotViewActivity() {
        User fieldEmployee = user(21L, UserRole.FIELD_EMPLOYEE);
        MaintenanceActivity activity = completedMaintenanceActivity(5000L, 1000L);

        when(userService.getById(21L)).thenReturn(fieldEmployee);
        when(maintenanceActivityRepository.findDetailedById(5000L)).thenReturn(Optional.of(activity));
        doThrow(new ForbiddenOperationException(
                "You may only view maintenance activities for work orders assigned to you."))
                .when(maintenanceActivityAuthorizationService)
                .requireCanViewMaintenanceActivity(fieldEmployee, activity);

        assertThatThrownBy(() -> maintenanceActivityService.getById(5000L, 21L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void listEligibleForCompletionReviewPage_shouldReturnActivitiesForManagerDepartment() {
        User manager = managerInDepartment(30L, 1L);
        MaintenanceActivity eligibleActivity = completedMaintenanceActivity(5000L, 1000L);

        when(completionReviewAuthorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findEligibleForCompletionReview(
                eq(30L), eq(1L), any(LocalDateTime.class), eq(DEFAULT_PAGEABLE)))
                .thenReturn(pageOf(eligibleActivity));

        var responses = maintenanceActivityService.listEligibleForCompletionReviewPage(30L, DEFAULT_PAGEABLE);

        assertThat(responses.getContent()).hasSize(1);
        assertThat(responses.getContent().get(0).getId()).isEqualTo(5000L);
        assertThat(responses.getContent().get(0).getCompletionReviewDecision()).isNull();
    }

    @Test
    void listEligibleForCompletionReviewPage_shouldRejectNonManager() {
        when(completionReviewAuthorizationService.requireManager(30L))
                .thenThrow(new ForbiddenOperationException("Only managers can record completion reviews"));

        assertThatThrownBy(() -> maintenanceActivityService.listEligibleForCompletionReviewPage(30L, DEFAULT_PAGEABLE))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(maintenanceActivityRepository, never())
                .findEligibleForCompletionReview(any(), any(), any(), any());
    }

    private User managerInDepartment(Long id, Long departmentId) {
        User manager = user(id, UserRole.MANAGER);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        manager.setDepartment(department);
        return manager;
    }

    private MaintenanceActivity completedMaintenanceActivity(Long id, Long workOrderId) {
        WorkOrder workOrder = assignedWorkOrder(workOrderId, WorkType.INTERNAL_MAINTENANCE, 20L);
        workOrder.complete();
        MaintenanceActivity maintenanceActivity = new MaintenanceActivity(
                workOrder,
                workOrder.getAsset(),
                20L,
                "Replaced damaged swing chain",
                LocalDateTime.now().minusHours(2)
        );
        maintenanceActivity.setId(id);
        return maintenanceActivity;
    }
}
