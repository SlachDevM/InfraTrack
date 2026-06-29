package com.infratrack.preventivemaintenance;

import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationResultResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveExecutionCandidateResponse;
import com.infratrack.preventivemaintenance.dto.TriggerEvaluationResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Generates and retrieves preventive maintenance execution candidates (V2 Phase B Sprint B2).
 * No Inspections, Work Orders, Maintenance Activities, or notifications are created.
 */
@Service
public class PreventiveExecutionCandidateService {

    private final PreventiveMaintenancePlanRepository planRepository;
    private final PreventiveExecutionCandidateRepository candidateRepository;
    private final TriggerEvaluationService triggerEvaluationService;
    private final PreventiveExecutionReportService reportService;

    public PreventiveExecutionCandidateService(
            PreventiveMaintenancePlanRepository planRepository,
            PreventiveExecutionCandidateRepository candidateRepository,
            TriggerEvaluationService triggerEvaluationService,
            PreventiveExecutionReportService reportService) {
        this.planRepository = planRepository;
        this.candidateRepository = candidateRepository;
        this.triggerEvaluationService = triggerEvaluationService;
        this.reportService = reportService;
    }

    @Transactional(readOnly = true)
    public Page<PreventiveExecutionCandidateResponse> listCandidates(
            ExecutionCandidateStatus status,
            Long assetId,
            Long planId,
            Pageable pageable) {
        return candidateRepository.findFiltered(status, assetId, planId, pageable)
                .map(PreventiveExecutionCandidateResponse::from);
    }

    @Transactional(readOnly = true)
    public PreventiveExecutionCandidateResponse getCandidate(Long id) {
        return PreventiveExecutionCandidateResponse.from(findCandidate(id));
    }

    @Transactional
    public List<ExecutionCandidateGenerationResultResponse> generateCandidates(Long userId) {
        return generateCandidatesForActivePlans(userId, null);
    }

    @Transactional
    public List<ExecutionCandidateGenerationResultResponse> generateCandidatesForActivePlans(
            Long userId,
            Long departmentId) {
        List<PreventiveMaintenancePlan> activePlans = findActivePlans(departmentId);
        List<ExecutionCandidateGenerationResultResponse> results = new ArrayList<>();
        for (PreventiveMaintenancePlan plan : activePlans) {
            try {
                results.add(generateForPlan(plan, userId));
            } catch (Exception ex) {
                results.add(ExecutionCandidateGenerationResultResponse.failed(
                        plan.getId(),
                        plan.getPlanCode(),
                        ex.getMessage()));
            }
        }
        return results;
    }

    @Transactional
    public ExecutionCandidateGenerationResultResponse generateCandidateForPlan(Long planId, Long userId) {
        PreventiveMaintenancePlan plan = planRepository.findDetailedById(planId)
                .orElseThrow(() -> new NotFoundException("Preventive maintenance plan not found"));
        return generateForPlan(plan, userId);
    }

    private ExecutionCandidateGenerationResultResponse generateForPlan(
            PreventiveMaintenancePlan plan,
            Long userId) {
        if (plan.getStatus() != PreventiveMaintenancePlanStatus.ACTIVE) {
            return ExecutionCandidateGenerationResultResponse.notEligible(plan.getId(), plan.getPlanCode());
        }

        TriggerEvaluationResultResponse evaluation = triggerEvaluationService.evaluatePlan(plan.getId());
        if (!evaluation.isEligible()) {
            return ExecutionCandidateGenerationResultResponse.notEligible(plan.getId(), plan.getPlanCode());
        }

        Optional<PreventiveExecutionCandidate> existingPending = candidateRepository
                .findByPreventiveMaintenancePlanIdAndCandidateStatus(
                        plan.getId(),
                        ExecutionCandidateStatus.PENDING);
        if (existingPending.isPresent()) {
            return ExecutionCandidateGenerationResultResponse.skipped(
                    plan.getId(),
                    plan.getPlanCode(),
                    PreventiveExecutionCandidateResponse.from(existingPending.get()));
        }

        PreventiveExecutionCandidate candidate = createCandidate(plan, evaluation);
        PreventiveExecutionCandidate saved = candidateRepository.save(candidate);
        reportService.createReportForCandidate(saved, userId);
        return ExecutionCandidateGenerationResultResponse.created(
                plan.getId(),
                plan.getPlanCode(),
                PreventiveExecutionCandidateResponse.from(saved));
    }

    private List<PreventiveMaintenancePlan> findActivePlans(Long departmentId) {
        if (departmentId == null) {
            return planRepository.findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE);
        }
        return planRepository.findAllByStatusAndAsset_Department_Id(
                PreventiveMaintenancePlanStatus.ACTIVE,
                departmentId);
    }

    private PreventiveExecutionCandidate createCandidate(
            PreventiveMaintenancePlan plan,
            TriggerEvaluationResultResponse evaluation) {
        PlanBusinessTrigger trigger = plan.getBusinessTrigger();
        TriggerSummary summary = trigger != null
                ? TriggerDefinitionFactory.from(trigger).buildSummary()
                : new TriggerSummary("", "", evaluation.getTriggerType());

        return new PreventiveExecutionCandidate(
                plan,
                plan.getAsset(),
                evaluation.getTriggerType(),
                evaluation.getEvaluationReason(),
                evaluation.getEvaluatedAt(),
                evaluation.getNextEligibleAt(),
                plan.getPlanCode(),
                plan.getVersion(),
                plan.getName(),
                plan.getTargetAction(),
                summary.getTitle(),
                summary.getDescription());
    }

    private PreventiveExecutionCandidate findCandidate(Long id) {
        return candidateRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Preventive execution candidate not found"));
    }
}
