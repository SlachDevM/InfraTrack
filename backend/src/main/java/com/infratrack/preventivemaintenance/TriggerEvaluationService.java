package com.infratrack.preventivemaintenance;

import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.TriggerEvaluationResultResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Evaluates preventive maintenance plan eligibility in memory only (V2 Phase B Sprint B1.3).
 */
@Service
public class TriggerEvaluationService {

    private final PreventiveMaintenancePlanRepository planRepository;

    public TriggerEvaluationService(PreventiveMaintenancePlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Transactional(readOnly = true)
    public TriggerEvaluationResultResponse evaluatePlan(Long planId) {
        return evaluatePlan(planId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public TriggerEvaluationResultResponse evaluatePlan(Long planId, LocalDateTime currentDateTime) {
        PreventiveMaintenancePlan plan = planRepository.findDetailedById(planId)
                .orElseThrow(() -> new NotFoundException("Preventive maintenance plan not found"));
        return evaluateLoadedPlan(plan, currentDateTime);
    }

    @Transactional(readOnly = true)
    public List<TriggerEvaluationResultResponse> evaluateAllPlans() {
        return evaluateAllPlans(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public List<TriggerEvaluationResultResponse> evaluateAllPlans(LocalDateTime currentDateTime) {
        return planRepository.findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE).stream()
                .map(plan -> evaluateLoadedPlan(plan, currentDateTime))
                .toList();
    }

    private TriggerEvaluationResultResponse evaluateLoadedPlan(
            PreventiveMaintenancePlan plan,
            LocalDateTime currentDateTime) {
        long startedAt = System.nanoTime();
        long evaluatedAt = System.currentTimeMillis();

        PlanBusinessTrigger trigger = plan.getBusinessTrigger();
        PlanTriggerType triggerType = trigger != null ? trigger.getTriggerType() : null;
        TriggerSummary summary = trigger != null
                ? TriggerDefinitionFactory.from(trigger).buildSummary()
                : null;

        TriggerEvaluationOutcome outcome = resolveOutcome(plan, currentDateTime);
        long durationMs = (System.nanoTime() - startedAt) / 1_000_000;

        return TriggerEvaluationResultResponse.from(
                plan.getId(),
                plan.getPlanCode(),
                triggerType,
                outcome,
                summary,
                evaluatedAt,
                durationMs);
    }

    private TriggerEvaluationOutcome resolveOutcome(
            PreventiveMaintenancePlan plan,
            LocalDateTime currentDateTime) {
        return switch (plan.getStatus()) {
            case DRAFT -> new TriggerEvaluationOutcome(false, "Plan is still in draft.");
            case PAUSED -> new TriggerEvaluationOutcome(false, "Plan is paused.");
            case ARCHIVED -> new TriggerEvaluationOutcome(false, "Plan is archived.");
            case ACTIVE -> evaluateActivePlan(plan, currentDateTime);
        };
    }

    private TriggerEvaluationOutcome evaluateActivePlan(
            PreventiveMaintenancePlan plan,
            LocalDateTime currentDateTime) {
        PlanBusinessTrigger trigger = plan.getBusinessTrigger();
        if (trigger == null || !trigger.isActive()) {
            return new TriggerEvaluationOutcome(false, "Plan trigger is not active.");
        }
        TriggerDefinition definition = TriggerDefinitionFactory.from(trigger);
        TriggerEvaluationContext context = new TriggerEvaluationContext(plan, currentDateTime);
        return definition.evaluate(context);
    }
}
