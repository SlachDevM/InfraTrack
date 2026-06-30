package com.infratrack.operationsintelligence;

import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.IssueType;
import com.infratrack.operationsintelligence.dto.AssetKpiResponse;
import com.infratrack.operationsintelligence.dto.DecisionEngineKpiResponse;
import com.infratrack.operationsintelligence.dto.InspectionKpiResponse;
import com.infratrack.operationsintelligence.dto.IssueKpiResponse;
import com.infratrack.operationsintelligence.dto.OperationsKpiResponse;
import com.infratrack.operationsintelligence.dto.PreventiveKpiResponse;
import com.infratrack.operationsintelligence.dto.WorkOrderKpiResponse;
import com.infratrack.preventivemaintenance.ExecutionCandidateStatus;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlan;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanRepository;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;
import com.infratrack.preventivemaintenance.TriggerEvaluationService;
import com.infratrack.preventivemaintenance.dto.TriggerEvaluationResultResponse;
import com.infratrack.suggestedaction.SuggestedActionStatus;
import com.infratrack.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
public class OperationsKpiService {

    private final OperationsKpiAuthorizationService authorizationService;
    private final OperationsKpiAggregationRepository aggregationRepository;
    private final PreventiveMaintenancePlanRepository planRepository;
    private final TriggerEvaluationService triggerEvaluationService;

    public OperationsKpiService(
            OperationsKpiAuthorizationService authorizationService,
            OperationsKpiAggregationRepository aggregationRepository,
            PreventiveMaintenancePlanRepository planRepository,
            TriggerEvaluationService triggerEvaluationService) {
        this.authorizationService = authorizationService;
        this.aggregationRepository = aggregationRepository;
        this.planRepository = planRepository;
        this.triggerEvaluationService = triggerEvaluationService;
    }

    @Transactional(readOnly = true)
    public OperationsKpiResponse getKpis(Long userId) {
        OperationsKpiScope scope = authorizationService.resolveScope(userId);
        LocalDate today = LocalDate.now();

        OperationsKpiResponse response = new OperationsKpiResponse();
        response.setAssets(buildAssetKpis(scope));
        response.setInspections(buildInspectionKpis(scope, today));
        response.setIssues(buildIssueKpis(scope));
        response.setWorkOrders(buildWorkOrderKpis(scope));
        response.setPreventive(buildPreventiveKpis(scope));
        response.setDecisionEngine(buildDecisionEngineKpis(scope));
        return response;
    }

    private AssetKpiResponse buildAssetKpis(OperationsKpiScope scope) {
        AssetKpiResponse assets = new AssetKpiResponse();
        assets.setTotalAssets(aggregationRepository.countAssets(scope));
        assets.setAssetsByDepartment(aggregationRepository.countAssetsByDepartment(scope));
        assets.setAssetsByCategory(aggregationRepository.countAssetsByCategory(scope));
        assets.setAssetsWithoutCategory(aggregationRepository.countAssetsWithoutCategory(scope));
        assets.setAssetsWithoutDepartment(aggregationRepository.countAssetsWithoutDepartment(scope));
        return assets;
    }

    private InspectionKpiResponse buildInspectionKpis(OperationsKpiScope scope, LocalDate today) {
        long inProgress = aggregationRepository.countInProgressInspections(scope, today);
        long overdue = aggregationRepository.countOverdueInspections(scope, today);

        InspectionKpiResponse inspections = new InspectionKpiResponse();
        inspections.setInProgressInspections(inProgress);
        inspections.setOverdueInspections(overdue);
        inspections.setAssignedInspections(inProgress + overdue);
        inspections.setCompletedInspections(
                aggregationRepository.countInspectionsByStatus(scope, InspectionStatus.COMPLETED));
        return inspections;
    }

    private IssueKpiResponse buildIssueKpis(OperationsKpiScope scope) {
        IssueKpiResponse issues = new IssueKpiResponse();
        issues.setOpenIssues(aggregationRepository.countOpenIssues(scope));
        issues.setResolvedIssues(aggregationRepository.countResolvedIssues(scope));
        issues.setNormalIssues(aggregationRepository.countIssuesByType(scope, IssueType.NORMAL));
        issues.setReworkIssues(aggregationRepository.countIssuesByType(scope, IssueType.REWORK));
        issues.setIssuesBySeverity(aggregationRepository.countIssuesBySeverity(scope));
        issues.setIssuesByType(aggregationRepository.countIssuesByTypeGrouped(scope));
        return issues;
    }

    private WorkOrderKpiResponse buildWorkOrderKpis(OperationsKpiScope scope) {
        WorkOrderKpiResponse workOrders = new WorkOrderKpiResponse();
        workOrders.setOpenWorkOrders(aggregationRepository.countWorkOrdersByStatus(scope, WorkOrderStatus.CREATED));
        workOrders.setInProgressWorkOrders(
                aggregationRepository.countWorkOrdersByStatus(scope, WorkOrderStatus.ASSIGNED));
        workOrders.setCompletedWorkOrders(
                aggregationRepository.countWorkOrdersByStatus(scope, WorkOrderStatus.COMPLETED));
        workOrders.setOverdueWorkOrders(0);
        return workOrders;
    }

    private PreventiveKpiResponse buildPreventiveKpis(OperationsKpiScope scope) {
        ZoneId zone = ZoneId.systemDefault();
        long startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli();
        long startOfTomorrow = LocalDate.now(zone).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli();

        PreventiveKpiResponse preventive = new PreventiveKpiResponse();
        preventive.setActivePreventivePlans(
                aggregationRepository.countPreventivePlansByStatus(scope, PreventiveMaintenancePlanStatus.ACTIVE));
        preventive.setPausedPreventivePlans(
                aggregationRepository.countPreventivePlansByStatus(scope, PreventiveMaintenancePlanStatus.PAUSED));
        preventive.setPendingExecutionCandidates(
                aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.PENDING));
        preventive.setApprovedExecutionCandidates(
                aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.APPROVED));
        preventive.setRejectedExecutionCandidates(
                aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.REJECTED));
        preventive.setDismissedExecutionCandidates(
                aggregationRepository.countExecutionCandidatesByStatus(scope, ExecutionCandidateStatus.DISMISSED));
        preventive.setSchedulerRunsToday(
                aggregationRepository.countSchedulerRunsBetween(startOfDay, startOfTomorrow));
        preventive.setEligiblePlansNow(countEligiblePlansNow(scope));
        return preventive;
    }

    private long countEligiblePlansNow(OperationsKpiScope scope) {
        List<PreventiveMaintenancePlan> activePlans = scope.isGlobal()
                ? planRepository.findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE)
                : planRepository.findAllByStatusAndAsset_Department_Id(
                        PreventiveMaintenancePlanStatus.ACTIVE,
                        scope.departmentId());

        return activePlans.stream()
                .map(plan -> triggerEvaluationService.evaluatePlan(plan.getId()))
                .filter(TriggerEvaluationResultResponse::isEligible)
                .count();
    }

    private DecisionEngineKpiResponse buildDecisionEngineKpis(OperationsKpiScope scope) {
        DecisionEngineKpiResponse decisionEngine = new DecisionEngineKpiResponse();
        decisionEngine.setRuleEvaluationReports(aggregationRepository.countRuleEvaluationReports(scope));
        decisionEngine.setSuggestedActionsPending(
                aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.PENDING));
        decisionEngine.setSuggestedActionsAccepted(
                aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.ACCEPTED));
        decisionEngine.setSuggestedActionsRejected(
                aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.REJECTED));
        decisionEngine.setSuggestedActionsDismissed(
                aggregationRepository.countSuggestedActionsByStatus(scope, SuggestedActionStatus.DISMISSED));
        decisionEngine.setMatchedRuleResults(aggregationRepository.sumMatchedRuleResults(scope));
        return decisionEngine;
    }
}
