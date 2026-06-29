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

    public PreventiveExecutionCandidateService(
            PreventiveMaintenancePlanRepository planRepository,
            PreventiveExecutionCandidateRepository candidateRepository,
            TriggerEvaluationService triggerEvaluationService) {
        this.planRepository = planRepository;
        this.candidateRepository = candidateRepository;
        this.triggerEvaluationService = triggerEvaluationService;
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
    public List<ExecutionCandidateGenerationResultResponse> generateCandidates() {
        List<PreventiveMaintenancePlan> activePlans =
                planRepository.findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE);
        List<ExecutionCandidateGenerationResultResponse> results = new ArrayList<>();
        for (PreventiveMaintenancePlan plan : activePlans) {
            results.add(generateForPlan(plan));
        }
        return results;
    }

    @Transactional
    public ExecutionCandidateGenerationResultResponse generateCandidateForPlan(Long planId) {
        PreventiveMaintenancePlan plan = planRepository.findDetailedById(planId)
                .orElseThrow(() -> new NotFoundException("Preventive maintenance plan not found"));
        return generateForPlan(plan);
    }

    private ExecutionCandidateGenerationResultResponse generateForPlan(PreventiveMaintenancePlan plan) {
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
        return ExecutionCandidateGenerationResultResponse.created(
                plan.getId(),
                plan.getPlanCode(),
                PreventiveExecutionCandidateResponse.from(saved));
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
