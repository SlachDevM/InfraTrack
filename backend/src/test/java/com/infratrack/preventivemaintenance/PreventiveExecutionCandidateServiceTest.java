package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationOutcome;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationResultResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveExecutionCandidateResponse;
import com.infratrack.preventivemaintenance.dto.TriggerEvaluationResultResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PreventiveExecutionCandidateServiceTest {

    @Mock
    private PreventiveMaintenancePlanRepository planRepository;

    @Mock
    private PreventiveExecutionCandidateRepository candidateRepository;

    @Mock
    private TriggerEvaluationService triggerEvaluationService;

    @InjectMocks
    private PreventiveExecutionCandidateService candidateService;

    @Test
    void generateCandidateForPlan_shouldCreatePendingCandidateForEligibleActivePlan() {
        PreventiveMaintenancePlan plan = plan(
                100L,
                "PUMP_MONTHLY",
                PreventiveMaintenancePlanStatus.ACTIVE,
                LocalDate.of(2024, 1, 1));
        when(planRepository.findDetailedById(100L)).thenReturn(Optional.of(plan));
        when(triggerEvaluationService.evaluatePlan(100L)).thenReturn(eligibleEvaluation(100L, "PUMP_MONTHLY"));
        when(candidateRepository.findByPreventiveMaintenancePlanIdAndCandidateStatus(
                100L, ExecutionCandidateStatus.PENDING)).thenReturn(Optional.empty());
        when(candidateRepository.save(any(PreventiveExecutionCandidate.class))).thenAnswer(invocation -> {
            PreventiveExecutionCandidate candidate = invocation.getArgument(0);
            candidate.setId(500L);
            return candidate;
        });

        ExecutionCandidateGenerationResultResponse result =
                candidateService.generateCandidateForPlan(100L);

        assertThat(result.getOutcome()).isEqualTo(ExecutionCandidateGenerationOutcome.CREATED);
        assertThat(result.getCandidate().getCandidateStatus()).isEqualTo(ExecutionCandidateStatus.PENDING);
        assertThat(result.getCandidate().getPlanCodeSnapshot()).isEqualTo("PUMP_MONTHLY");
        assertThat(result.getCandidate().getPlanNameSnapshot()).isEqualTo("Monthly Pump Inspection");
        assertThat(result.getCandidate().getTargetActionSnapshot()).isEqualTo(PlanTargetAction.CREATE_INSPECTION);
        assertThat(result.getCandidate().getTriggerSummaryTitleSnapshot()).isEqualTo("Every month");
        assertThat(result.getCandidate().getEligibilityReason()).isEqualTo("One full month has elapsed.");

        ArgumentCaptor<PreventiveExecutionCandidate> captor =
                ArgumentCaptor.forClass(PreventiveExecutionCandidate.class);
        verify(candidateRepository).save(captor.capture());
        assertThat(captor.getValue().getCandidateStatus()).isEqualTo(ExecutionCandidateStatus.PENDING);
    }

    @Test
    void generateCandidateForPlan_shouldNotCreateCandidateWhenNotEligible() {
        PreventiveMaintenancePlan plan = plan(
                101L,
                "PUMP_WEEKLY",
                PreventiveMaintenancePlanStatus.ACTIVE,
                LocalDate.of(2024, 6, 27));
        when(planRepository.findDetailedById(101L)).thenReturn(Optional.of(plan));
        when(triggerEvaluationService.evaluatePlan(101L)).thenReturn(notEligibleEvaluation(101L, "PUMP_WEEKLY"));

        ExecutionCandidateGenerationResultResponse result =
                candidateService.generateCandidateForPlan(101L);

        assertThat(result.getOutcome()).isEqualTo(ExecutionCandidateGenerationOutcome.NOT_ELIGIBLE);
        assertThat(result.getCandidate()).isNull();
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void generateCandidateForPlan_shouldNotCreateCandidateForDraftPlan() {
        PreventiveMaintenancePlan plan = plan(
                102L,
                "DRAFT_PLAN",
                PreventiveMaintenancePlanStatus.DRAFT,
                LocalDate.of(2024, 1, 1));
        when(planRepository.findDetailedById(102L)).thenReturn(Optional.of(plan));

        ExecutionCandidateGenerationResultResponse result =
                candidateService.generateCandidateForPlan(102L);

        assertThat(result.getOutcome()).isEqualTo(ExecutionCandidateGenerationOutcome.NOT_ELIGIBLE);
        verify(triggerEvaluationService, never()).evaluatePlan(any());
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void generateCandidateForPlan_shouldSkipWhenPendingCandidateExists() {
        PreventiveMaintenancePlan plan = plan(
                100L,
                "PUMP_MONTHLY",
                PreventiveMaintenancePlanStatus.ACTIVE,
                LocalDate.of(2024, 1, 1));
        PreventiveExecutionCandidate existing = existingCandidate(plan);
        when(planRepository.findDetailedById(100L)).thenReturn(Optional.of(plan));
        when(triggerEvaluationService.evaluatePlan(100L)).thenReturn(eligibleEvaluation(100L, "PUMP_MONTHLY"));
        when(candidateRepository.findByPreventiveMaintenancePlanIdAndCandidateStatus(
                100L, ExecutionCandidateStatus.PENDING)).thenReturn(Optional.of(existing));

        ExecutionCandidateGenerationResultResponse result =
                candidateService.generateCandidateForPlan(100L);

        assertThat(result.getOutcome()).isEqualTo(ExecutionCandidateGenerationOutcome.SKIPPED_DUPLICATE);
        assertThat(result.getCandidate().getId()).isEqualTo(500L);
        verify(candidateRepository, never()).save(any());
    }

    @Test
    void generateCandidates_shouldEvaluateActivePlansOnly() {
        PreventiveMaintenancePlan eligible = plan(
                100L,
                "PUMP_MONTHLY",
                PreventiveMaintenancePlanStatus.ACTIVE,
                LocalDate.of(2024, 1, 1));
        when(planRepository.findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE))
                .thenReturn(List.of(eligible));
        when(triggerEvaluationService.evaluatePlan(100L)).thenReturn(eligibleEvaluation(100L, "PUMP_MONTHLY"));
        when(candidateRepository.findByPreventiveMaintenancePlanIdAndCandidateStatus(
                100L, ExecutionCandidateStatus.PENDING)).thenReturn(Optional.empty());
        when(candidateRepository.save(any(PreventiveExecutionCandidate.class))).thenAnswer(invocation -> {
            PreventiveExecutionCandidate candidate = invocation.getArgument(0);
            candidate.setId(500L);
            return candidate;
        });

        List<ExecutionCandidateGenerationResultResponse> results = candidateService.generateCandidates();

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getOutcome()).isEqualTo(ExecutionCandidateGenerationOutcome.CREATED);
    }

    @Test
    void getCandidate_shouldRejectMissingCandidate() {
        when(candidateRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.getCandidate(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void generateCandidateForPlan_shouldRejectMissingPlan() {
        when(planRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> candidateService.generateCandidateForPlan(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private TriggerEvaluationResultResponse eligibleEvaluation(Long planId, String planCode) {
        return TriggerEvaluationResultResponse.from(
                planId,
                planCode,
                PlanTriggerType.TIME,
                new TriggerEvaluationOutcome(true, "One full month has elapsed.", null),
                new TriggerSummary("Every month", "Eligible once every full month from plan creation.", PlanTriggerType.TIME),
                1710000000000L,
                1L);
    }

    private TriggerEvaluationResultResponse notEligibleEvaluation(Long planId, String planCode) {
        LocalDate created = LocalDate.of(2024, 6, 27);
        long nextEligibleAt = created.plusMonths(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return TriggerEvaluationResultResponse.from(
                planId,
                planCode,
                PlanTriggerType.TIME,
                new TriggerEvaluationOutcome(false, "Next execution interval has not been reached.", nextEligibleAt),
                new TriggerSummary("Every month", "Eligible once every full month from plan creation.", PlanTriggerType.TIME),
                1710000000000L,
                1L);
    }

    private PreventiveExecutionCandidate existingCandidate(PreventiveMaintenancePlan plan) {
        PreventiveExecutionCandidate candidate = new PreventiveExecutionCandidate(
                plan,
                plan.getAsset(),
                PlanTriggerType.TIME,
                "One full month has elapsed.",
                1710000000000L,
                null,
                plan.getPlanCode(),
                plan.getVersion(),
                plan.getName(),
                plan.getTargetAction(),
                "Every month",
                "Eligible once every full month from plan creation.");
        candidate.setId(500L);
        return candidate;
    }

    private PreventiveMaintenancePlan plan(
            Long id,
            String planCode,
            PreventiveMaintenancePlanStatus status,
            LocalDate createdDate) {
        Asset asset = new Asset(
                "Pump A",
                mock(Department.class),
                mock(AssetCategory.class),
                "Location",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 1, 1),
                1L
        );
        asset.setId(5L);
        PreventiveMaintenancePlan plan = new PreventiveMaintenancePlan(
                asset,
                planCode,
                "Monthly Pump Inspection",
                null,
                1,
                status,
                PreventiveMaintenancePlanPriority.MEDIUM,
                PlanTargetAction.CREATE_INSPECTION,
                null
        );
        plan.setId(id);
        plan.setCreatedAtMillis(createdDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli());
        PlanBusinessTrigger trigger = new PlanBusinessTrigger(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}",
                true);
        plan.setBusinessTrigger(trigger);
        return plan;
    }
}
