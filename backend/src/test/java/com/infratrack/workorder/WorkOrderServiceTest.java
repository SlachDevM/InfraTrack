package com.infratrack.workorder;

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
import com.infratrack.workorder.dto.CreateWorkOrderRequest;
import com.infratrack.model.User;
import com.infratrack.model.UserRole;
import com.infratrack.service.UserService;
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
class WorkOrderServiceTest {

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private OperationalDecisionRepository operationalDecisionRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private WorkOrderService workOrderService;

    @Test
    void createWorkOrder_shouldCreateWorkOrderFromInternalMaintenanceDecision() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
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
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
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
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
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
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(40L)).thenReturn(coordinator);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createWorkOrder_shouldRejectInvalidOperationalDecision() {
        CreateWorkOrderRequest request = validRequest();
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
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
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(true);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.CONFLICT);

        verify(workOrderRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void createWorkOrder_shouldRejectBlankDescription() {
        CreateWorkOrderRequest request = validRequest();
        request.setDescription("  ");
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createWorkOrder_shouldRejectMissingPriority() {
        CreateWorkOrderRequest request = validRequest();
        request.setPriority(null);
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createWorkOrder_shouldRejectMissingCreatedAtBusinessDate() {
        CreateWorkOrderRequest request = validRequest();
        request.setCreatedAtBusinessDate(null);
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createWorkOrder_shouldRejectCreatedAtBusinessDateBeforeDecision() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        request.setCreatedAtBusinessDate(decision.getDecidedAt().minusMinutes(30));
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createWorkOrder_shouldRejectFutureCreatedAtBusinessDate() {
        CreateWorkOrderRequest request = validRequest();
        request.setCreatedAtBusinessDate(LocalDateTime.now().plusDays(1));
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
        when(workOrderRepository.existsByOperationalDecisionId(900L)).thenReturn(false);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void createWorkOrder_shouldAllowOperationalCoordinator() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
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
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void createWorkOrder_shouldRejectAdministrator() {
        CreateWorkOrderRequest request = validRequest();
        User administrator = user(40L, UserRole.ADMINISTRATOR);
        when(userService.getById(40L)).thenReturn(administrator);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void createWorkOrder_shouldRejectFieldEmployee() {
        CreateWorkOrderRequest request = validRequest();
        User fieldEmployee = user(40L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(40L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void createWorkOrder_shouldRejectContractor() {
        CreateWorkOrderRequest request = validRequest();
        User contractor = user(40L, UserRole.CONTRACTOR);
        when(userService.getById(40L)).thenReturn(contractor);

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.FORBIDDEN);
    }

    @Test
    void createWorkOrder_shouldNotCreateMaintenanceActivity() {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));
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

    private void assertRejectedOutcome(OperationalDecisionOutcome outcome) {
        CreateWorkOrderRequest request = validRequest();
        OperationalDecision decision = decision(900L, outcome);
        User coordinator = user(40L, UserRole.OPERATIONAL_COORDINATOR);

        when(userService.getById(40L)).thenReturn(coordinator);
        when(operationalDecisionRepository.findById(900L)).thenReturn(Optional.of(decision));

        assertThatThrownBy(() -> workOrderService.createWorkOrder(request, 40L))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);

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
