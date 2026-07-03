package com.infratrack.workorder;

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
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.workorder.dto.AssignWorkOrderRequest;
import com.infratrack.workorder.dto.CreateWorkOrderRequest;
import com.infratrack.workorder.dto.WorkOrderSummaryResponse;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.organization.policy.approval.ApprovalPolicyService;
import com.infratrack.organization.policy.notification.NotificationPolicyService;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkOrderServiceTest {

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
    private OperationalEventNotificationService operationalEventNotificationService;

    private WorkOrderService workOrderService;

    @BeforeEach
    void setUp() {
        WorkOrderAuthorizationService authorizationService = new WorkOrderAuthorizationService(userService);
        WorkOrderHistoryRecorder historyRecorder = new WorkOrderHistoryRecorder(assetHistoryEventRepository);
        workOrderService = new WorkOrderService(
                workOrderRepository,
                operationalDecisionRepository,
                authorizationService,
                historyRecorder,
                userService,
                userNameLookup,
                operationalEventNotificationService,
                new NotificationPolicyService("DEFAULT"),
                new ApprovalPolicyService());
    }

    @Test
    void createWorkOrder_shouldCreateWorkOrderFromInternalMaintenanceDecision() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder workOrder = invocation.getArgument(0);
            workOrder.setId(1000L);
            return workOrder;
        });

        var response = workOrderService.createWorkOrder(request, 40L);

        assertThat(response.getId()).isEqualTo(1000L);
        assertThat(response.getOperationalDecisionId()).isEqualTo(900L);
        assertThat(response.getAssetId()).isEqualTo(5L);
        assertThat(response.getWorkType()).isEqualTo(WorkType.INTERNAL_MAINTENANCE);
        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.CREATED);

        verify(workOrderRepository).save(argThat(order -> order.getWorkType() == WorkType.INTERNAL_MAINTENANCE));
    }

    @Test
    void createWorkOrder_shouldCreateWorkOrderFromContractorWorkDecision() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.CONTRACTOR_WORK);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder workOrder = invocation.getArgument(0);
            workOrder.setId(1000L);
            return workOrder;
        });

        var response = workOrderService.createWorkOrder(request, 40L);

        assertThat(response.getWorkType()).isEqualTo(WorkType.CONTRACTOR_WORK);
    }

    @Test
    void createWorkOrder_shouldCreateHistoryEventAndLinkAsset() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder workOrder = invocation.getArgument(0);
            workOrder.setId(1000L);
            return workOrder;
        });

        workOrderService.createWorkOrder(request, 40L);

        ArgumentCaptor<WorkOrder> workOrderCaptor = ArgumentCaptor.forClass(WorkOrder.class);
        verify(workOrderRepository).save(workOrderCaptor.capture());
        assertThat(workOrderCaptor.getValue().getOperationalDecision().getId()).isEqualTo(900L);
        assertThat(workOrderCaptor.getValue().getAsset().getId()).isEqualTo(5L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.WORK_ORDER_CREATED);
    }

    @Test
    void createWorkOrder_shouldRejectMissingOperationalDecisionId() {
        CreateWorkOrderRequest request = validRequest();
        request.setOperationalDecisionId(null);
        User coordinator = coordinatorInDepartment(40L, 1L);
        when(userService.getById(40L)).thenReturn(coordinator);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectInvalidOperationalDecision() {
        CreateWorkOrderRequest request = validRequest();
        User coordinator = coordinatorInDepartment(40L, 1L);
        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectContinueMonitoringOutcome() {
        assertRejectedOutcome(OperationalDecisionOutcome.CONTINUE_MONITORING);
    }

    @Test
    void createWorkOrder_shouldRejectRenewalRecommendationOutcome() {
        assertRejectedOutcome(OperationalDecisionOutcome.RENEWAL_RECOMMENDATION);
    }

    @Test
    void createWorkOrder_shouldRejectDecommissionRecommendationOutcome() {
        assertRejectedOutcome(OperationalDecisionOutcome.DECOMMISSION_RECOMMENDATION);
    }

    @Test
    void createWorkOrder_shouldRejectDuplicateWorkOrderForSameDecision() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(true);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ConflictException.class);

        verify(workOrderRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void createWorkOrder_shouldRejectBlankDescription() {
        CreateWorkOrderRequest request = validRequest();
        request.setDescription("  ");
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectMissingPriority() {
        CreateWorkOrderRequest request = validRequest();
        request.setPriority(null);
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectMissingCreatedAtBusinessDate() {
        CreateWorkOrderRequest request = validRequest();
        request.setCreatedAtBusinessDate(null);
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectCreatedAtBusinessDateBeforeDecision() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        request.setCreatedAtBusinessDate(decision.getDecidedAt().minusMinutes(30));
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectFutureCreatedAtBusinessDate() {
        CreateWorkOrderRequest request = validRequest();
        request.setCreatedAtBusinessDate(LocalDateTime.now().plusDays(1));
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void createWorkOrder_shouldAllowOperationalCoordinator() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder workOrder = invocation.getArgument(0);
            workOrder.setId(1000L);
            return workOrder;
        });

        assertThatCode(() -> workOrderService.createWorkOrder(request, 40L))
                .doesNotThrowAnyException();
    }

    @Test
    void createWorkOrder_shouldRejectManager() {
        CreateWorkOrderRequest request = validRequest();
        User manager = user(40L, UserRole.MANAGER);
        when(userService.getById(40L)).thenReturn(manager);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectAdministrator() {
        CreateWorkOrderRequest request = validRequest();
        User administrator = user(40L, UserRole.ADMINISTRATOR);
        when(userService.getById(40L)).thenReturn(administrator);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectFieldEmployee() {
        CreateWorkOrderRequest request = validRequest();
        User fieldEmployee = user(40L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(40L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createWorkOrder_shouldRejectContractor() {
        CreateWorkOrderRequest request = validRequest();
        User contractor = user(40L, UserRole.CONTRACTOR);
        when(userService.getById(40L)).thenReturn(contractor);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void createWorkOrder_shouldNotCreateMaintenanceActivity() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> {
            WorkOrder workOrder = invocation.getArgument(0);
            workOrder.setId(1000L);
            return workOrder;
        });

        workOrderService.createWorkOrder(request, 40L);

        verify(workOrderRepository).save(any(WorkOrder.class));
        verify(assetHistoryEventRepository).save(any(AssetHistoryEvent.class));
        verify(operationalDecisionRepository, never()).save(any());
        verifyNoMoreInteractions(workOrderRepository, operationalDecisionRepository, assetHistoryEventRepository);
    }

    @Test
    void assignWorkOrder_shouldAssignInternalMaintenanceToFieldEmployee() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = workOrderService.assignWorkOrder(1000L, request, 40L);

        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.ASSIGNED);
        assertThat(response.getAssignedToUserId()).isEqualTo(20L);
        assertThat(response.getAssignedByUserId()).isEqualTo(40L);
        assertThat(response.getAssignedAt()).isEqualTo(request.getAssignedAt());

        verify(assetHistoryEventRepository).save(argThat(event ->
                event.getEventType() == AssetHistoryEventType.WORK_ORDER_ASSIGNED));
        verify(operationalEventNotificationService).notifyWorkOrderAssigned(20L);
    }

    @Test
    void assignWorkOrder_shouldAssignContractorWorkToContractor() {
        AssignWorkOrderRequest request = validAssignRequest(25L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.CONTRACTOR_WORK);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User contractor = userInDepartment(25L, UserRole.CONTRACTOR, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(25L)).thenReturn(contractor);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = workOrderService.assignWorkOrder(1000L, request, 40L);

        assertThat(response.getStatus()).isEqualTo(WorkOrderStatus.ASSIGNED);
        assertThat(response.getWorkType()).isEqualTo(WorkType.CONTRACTOR_WORK);
    }

    @Test
    void assignWorkOrder_shouldRejectContractorForInternalMaintenance() {
        AssignWorkOrderRequest request = validAssignRequest(25L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User contractor = userInDepartment(25L, UserRole.CONTRACTOR, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(25L)).thenReturn(contractor);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(BusinessValidationException.class);

        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void assignWorkOrder_shouldRejectFieldEmployeeForContractorWork() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.CONTRACTOR_WORK);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectInvalidWorkOrder() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectAlreadyAssignedWorkOrder() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        workOrder.assign(20L, 40L, LocalDateTime.now().minusMinutes(10));
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectMissingAssignedUser() {
        AssignWorkOrderRequest request = validAssignRequest(null);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectMissingAssignedAt() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        request.setAssignedAt(null);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectAssignedAtBeforeWorkOrderCreation() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        request.setAssignedAt(workOrder.getCreatedAtBusinessDate().minusMinutes(30));
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectFutureAssignedAt() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        request.setAssignedAt(LocalDateTime.now().plusDays(1));
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignWorkOrder_shouldAllowOperationalCoordinator() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        assertThatCode(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .doesNotThrowAnyException();
    }

    @Test
    void assignWorkOrder_shouldRejectManager() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        User manager = user(40L, UserRole.MANAGER);
        when(userService.getById(40L)).thenReturn(manager);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectAdministrator() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        User administrator = user(40L, UserRole.ADMINISTRATOR);
        when(userService.getById(40L)).thenReturn(administrator);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectFieldEmployee() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        User fieldEmployee = user(40L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(40L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void assignWorkOrder_shouldRejectContractor() {
        AssignWorkOrderRequest request = validAssignRequest(25L);
        User contractor = user(40L, UserRole.CONTRACTOR);
        when(userService.getById(40L)).thenReturn(contractor);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void assignWorkOrder_shouldNotCreateMaintenanceActivity() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.save(any(WorkOrder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        workOrderService.assignWorkOrder(1000L, request, 40L);

        verify(workOrderRepository).save(any(WorkOrder.class));
        verify(assetHistoryEventRepository).save(any(AssetHistoryEvent.class));
        verify(operationalDecisionRepository, never()).save(any());
        verifyNoMoreInteractions(workOrderRepository, operationalDecisionRepository, assetHistoryEventRepository);
    }

    @Test
    void createWorkOrder_shouldRejectCrossDepartmentOperationalDecision() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 2L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void assignWorkOrder_shouldRejectDisabledFieldEmployee() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User disabledFieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);
        disabledFieldEmployee.setEnabled(false);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(20L)).thenReturn(disabledFieldEmployee);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void assignWorkOrder_shouldRejectCrossDepartmentFieldEmployee() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User coordinator = coordinatorInDepartment(40L, 1L);
        User crossDepartmentFieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 2L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));
        when(userService.getById(20L)).thenReturn(crossDepartmentFieldEmployee);

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void assignWorkOrder_shouldRejectCoordinatorFromAnotherDepartment() {
        AssignWorkOrderRequest request = validAssignRequest(20L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        User crossDepartmentCoordinator = coordinatorInDepartment(40L, 2L);
        User fieldEmployee = userInDepartment(20L, UserRole.FIELD_EMPLOYEE, 1L);

        when(userService.getById(40L)).thenReturn(crossDepartmentCoordinator);
        when(workOrderRepository.findDetailedById(1000L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> workOrderService.assignWorkOrder(1000L, request, 40L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(userService, never()).getById(20L);
        verify(workOrderRepository, never()).save(any());
    }

    @Test
    void listEligibleForAssignmentPage_shouldReturnCreatedWorkOrdersForCoordinatorDepartment() {
        User coordinator = coordinatorInDepartment(40L, 1L);
        WorkOrder workOrder = createdWorkOrder(1000L, WorkType.INTERNAL_MAINTENANCE);
        Pageable pageable = PageRequest.of(0, 20);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(workOrderRepository.findEligibleForAssignment(eq(1L), eq(pageable)))
                .thenReturn(new PageImpl<>(List.of(workOrder), pageable, 1));

        Page<WorkOrderSummaryResponse> page = workOrderService.listEligibleForAssignmentPage(40L, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(1000L);
    }

    private void assertRejectedOutcome(OperationalDecisionOutcome outcome) {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, outcome);
        User coordinator = coordinatorInDepartment(40L, 1L);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findDetailedById(900L)).thenReturn(Optional.of(decision));

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(BusinessValidationException.class);

        verify(workOrderRepository, never()).save(any());
    }

    private CreateWorkOrderRequest validRequest() {
        CreateWorkOrderRequest request = new CreateWorkOrderRequest();
        request.setOperationalDecisionId(900L);
        request.setDescription("Replace damaged swing chain");
        request.setPriority(WorkOrderPriority.HIGH);
        request.setCreatedAtBusinessDate(LocalDateTime.now().minusMinutes(5));
        return request;
    }

    private AssignWorkOrderRequest validAssignRequest(Long assignedToUserId) {
        AssignWorkOrderRequest request = new AssignWorkOrderRequest();
        request.setAssignedToUserId(assignedToUserId);
        request.setAssignedAt(LocalDateTime.now().minusMinutes(2));
        return request;
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

    private User coordinatorInDepartment(Long id, Long departmentId) {
        User coordinator = user(id, UserRole.OPERATIONAL_COORDINATOR);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        coordinator.setDepartment(department);
        return coordinator;
    }

    private User userInDepartment(Long id, UserRole role, Long departmentId) {
        User worker = user(id, role);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        worker.setDepartment(department);
        return worker;
    }
}
