package com.infratrack.maintenanceactivity;

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
import com.infratrack.maintenanceactivity.dto.CompleteMaintenanceActivityRequest;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MaintenanceActivityServiceTest {

    @Mock
    private MaintenanceActivityRepository maintenanceActivityRepository;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private MaintenanceActivityService maintenanceActivityService;

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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);

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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);
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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);
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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);
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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void completeMaintenance_shouldRejectMissingCompletedAt() {
        CompleteMaintenanceActivityRequest request = validRequest();
        request.setCompletedAt(null);
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void completeMaintenance_shouldRejectCompletedAtBeforeAssignedAt() {
        CompleteMaintenanceActivityRequest request = validRequest();
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        request.setCompletedAt(workOrder.getAssignedAt().minusMinutes(30));
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void completeMaintenance_shouldRejectFutureCompletedAt() {
        CompleteMaintenanceActivityRequest request = validRequest();
        request.setCompletedAt(LocalDateTime.now().plusDays(1));
        WorkOrder workOrder = assignedWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.existsByWorkOrderId(1000L)).thenReturn(false);

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void completeMaintenance_shouldRejectMissingWorkOrder() {
        CompleteMaintenanceActivityRequest request = validRequest();
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findById(1000L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> maintenanceActivityService.completeMaintenance(1000L, request, 20L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.NOT_FOUND);
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
                .isInstanceOf(ResponseStatusException.class)
                .satisfiesAnyOf(
                        thrown -> assertThat(((ResponseStatusException) thrown).getStatusCode())
                                .isEqualTo(HttpStatus.FORBIDDEN),
                        thrown -> assertThat(((ResponseStatusException) thrown).getStatusCode())
                                .isEqualTo(HttpStatus.CONFLICT)
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
}
