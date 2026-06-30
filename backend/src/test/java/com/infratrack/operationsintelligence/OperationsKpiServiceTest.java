package com.infratrack.operationsintelligence;

import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.IssueType;
import com.infratrack.operationsintelligence.dto.OperationsKpiResponse;
import com.infratrack.preventivemaintenance.ExecutionCandidateStatus;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlan;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanRepository;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;
import com.infratrack.preventivemaintenance.TriggerEvaluationService;
import com.infratrack.preventivemaintenance.dto.TriggerEvaluationResultResponse;
import com.infratrack.suggestedaction.SuggestedActionStatus;
import com.infratrack.workorder.WorkOrderStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsKpiServiceTest {

    @Mock
    private OperationsKpiAuthorizationService authorizationService;

    @Mock
    private OperationsKpiAggregationRepository aggregationRepository;

    @Mock
    private PreventiveMaintenancePlanRepository planRepository;

    @Mock
    private TriggerEvaluationService triggerEvaluationService;

    @InjectMocks
    private OperationsKpiService kpiService;

    private final OperationsKpiScope globalScope = OperationsKpiScope.global();
    private final OperationsKpiScope departmentScope = OperationsKpiScope.forDepartment(10L);

    @Test
    void getKpis_shouldAggregateAllGroupsForAdministrator() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        stubGlobalCounts();

        OperationsKpiResponse response = kpiService.getKpis(1L);

        assertThat(response.getAssets().getTotalAssets()).isEqualTo(12L);
        assertThat(response.getAssets().getAssetsByDepartment()).containsEntry(10L, 8L);
        assertThat(response.getInspections().getAssignedInspections()).isEqualTo(7L);
        assertThat(response.getInspections().getOverdueInspections()).isEqualTo(2L);
        assertThat(response.getIssues().getOpenIssues()).isEqualTo(4L);
        assertThat(response.getIssues().getResolvedIssues()).isEqualTo(3L);
        assertThat(response.getWorkOrders().getOpenWorkOrders()).isEqualTo(5L);
        assertThat(response.getWorkOrders().getOverdueWorkOrders()).isZero();
        assertThat(response.getPreventive().getPendingExecutionCandidates()).isEqualTo(2L);
        assertThat(response.getPreventive().getSchedulerRunsToday()).isEqualTo(1L);
        assertThat(response.getPreventive().getEligiblePlansNow()).isEqualTo(1L);
        assertThat(response.getDecisionEngine().getSuggestedActionsPending()).isEqualTo(6L);
        assertThat(response.getDecisionEngine().getMatchedRuleResults()).isEqualTo(9L);
    }

    @Test
    void getKpis_shouldUseDepartmentScopeForManager() {
        when(authorizationService.resolveScope(2L)).thenReturn(departmentScope);
        when(aggregationRepository.countAssets(departmentScope)).thenReturn(3L);
        when(aggregationRepository.countAssetsByDepartment(departmentScope)).thenReturn(Map.of(10L, 3L));
        when(aggregationRepository.countAssetsByCategory(departmentScope)).thenReturn(Map.of());
        when(aggregationRepository.countAssetsWithoutCategory(departmentScope)).thenReturn(0L);
        when(aggregationRepository.countAssetsWithoutDepartment(departmentScope)).thenReturn(0L);
        stubRemainingCounts(departmentScope);
        when(planRepository.findAllByStatusAndAsset_Department_Id(
                PreventiveMaintenancePlanStatus.ACTIVE, 10L)).thenReturn(List.of());

        OperationsKpiResponse response = kpiService.getKpis(2L);

        assertThat(response.getAssets().getTotalAssets()).isEqualTo(3L);
        verify(aggregationRepository).countAssets(departmentScope);
        verify(planRepository).findAllByStatusAndAsset_Department_Id(
                PreventiveMaintenancePlanStatus.ACTIVE, 10L);
        verify(planRepository, never()).findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE);
    }

    @Test
    void getKpis_shouldRemainReadOnly() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        stubGlobalCounts();

        kpiService.getKpis(1L);

        verify(planRepository, never()).save(any());
        verify(triggerEvaluationService, never()).evaluateAllPlans();
        verify(aggregationRepository).countAssets(globalScope);
    }

    private void stubGlobalCounts() {
        when(aggregationRepository.countAssets(globalScope)).thenReturn(12L);
        when(aggregationRepository.countAssetsByDepartment(globalScope)).thenReturn(Map.of(10L, 8L));
        when(aggregationRepository.countAssetsByCategory(globalScope)).thenReturn(Map.of(5L, 12L));
        when(aggregationRepository.countAssetsWithoutCategory(globalScope)).thenReturn(1L);
        when(aggregationRepository.countAssetsWithoutDepartment(globalScope)).thenReturn(0L);
        stubRemainingCounts(globalScope);
    }

    private void stubRemainingCounts(OperationsKpiScope scope) {
        LocalDate today = LocalDate.now();
        when(aggregationRepository.countInProgressInspections(scope, today)).thenReturn(5L);
        when(aggregationRepository.countOverdueInspections(scope, today)).thenReturn(2L);
        when(aggregationRepository.countInspectionsByStatus(scope, InspectionStatus.COMPLETED)).thenReturn(20L);
        when(aggregationRepository.countOpenIssues(scope)).thenReturn(4L);
        when(aggregationRepository.countResolvedIssues(scope)).thenReturn(3L);
        when(aggregationRepository.countIssuesByType(scope, IssueType.NORMAL)).thenReturn(6L);
        when(aggregationRepository.countIssuesByType(scope, IssueType.REWORK)).thenReturn(1L);
        when(aggregationRepository.countIssuesBySeverity(scope)).thenReturn(Map.of("HIGH", 2L));
        when(aggregationRepository.countIssuesByTypeGrouped(scope)).thenReturn(Map.of("NORMAL", 6L));
        when(aggregationRepository.countWorkOrdersByStatus(scope, WorkOrderStatus.CREATED)).thenReturn(5L);
        when(aggregationRepository.countWorkOrdersByStatus(scope, WorkOrderStatus.ASSIGNED)).thenReturn(3L);
        when(aggregationRepository.countWorkOrdersByStatus(scope, WorkOrderStatus.COMPLETED)).thenReturn(11L);
        stubPreventiveAndDecisionCounts(scope);
    }

    private void stubPreventiveAndDecisionCounts(OperationsKpiScope scope) {
        when(aggregationRepository.countPreventivePlansByStatus(scope, PreventiveMaintenancePlanStatus.ACTIVE))
                .thenReturn(4L);
        when(aggregationRepository.countPreventivePlansByStatus(scope, PreventiveMaintenancePlanStatus.PAUSED))
                .thenReturn(1L);
        when(aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.PENDING))
                .thenReturn(2L);
        when(aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.APPROVED))
                .thenReturn(1L);
        when(aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.REJECTED))
                .thenReturn(0L);
        when(aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.DISMISSED))
                .thenReturn(0L);
        when(aggregationRepository.countSchedulerRunsBetween(any(Long.class), any(Long.class))).thenReturn(1L);
        when(aggregationRepository.countRuleEvaluationReports(scope)).thenReturn(7L);
        when(aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.PENDING)).thenReturn(6L);
        when(aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.ACCEPTED)).thenReturn(2L);
        when(aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.REJECTED)).thenReturn(1L);
        when(aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.DISMISSED)).thenReturn(0L);
        when(aggregationRepository.sumMatchedRuleResults(scope)).thenReturn(9L);

        if (scope.isGlobal()) {
            PreventiveMaintenancePlan eligiblePlan = mock(PreventiveMaintenancePlan.class);
            when(eligiblePlan.getId()).thenReturn(200L);
            when(planRepository.findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE))
                    .thenReturn(List.of(eligiblePlan));
            TriggerEvaluationResultResponse eligibleResult = mock(TriggerEvaluationResultResponse.class);
            when(eligibleResult.isEligible()).thenReturn(true);
            when(triggerEvaluationService.evaluatePlan(200L)).thenReturn(eligibleResult);
        }
    }
}
