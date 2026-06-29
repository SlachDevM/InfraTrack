package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.TriggerEvaluationResultResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TriggerEvaluationServiceTest {

    @Mock
    private PreventiveMaintenancePlanRepository planRepository;

    @InjectMocks
    private TriggerEvaluationService triggerEvaluationService;

    @Test
    void evaluatePlan_shouldReturnEligibleForActiveMonthlyPlan() {
        PreventiveMaintenancePlan plan = plan(
                100L,
                "PUMP_MONTHLY",
                PreventiveMaintenancePlanStatus.ACTIVE,
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}",
                LocalDate.of(2024, 1, 1));
        when(planRepository.findDetailedById(100L)).thenReturn(Optional.of(plan));

        TriggerEvaluationResultResponse result = triggerEvaluationService.evaluatePlan(
                100L,
                LocalDateTime.of(2024, 2, 1, 10, 0));

        assertThat(result.isEligible()).isTrue();
        assertThat(result.getPlanCode()).isEqualTo("PUMP_MONTHLY");
        assertThat(result.getEvaluationReason()).isEqualTo("One full month has elapsed.");
        assertThat(result.getTriggerSummary().getTitle()).isEqualTo("Every month");
        assertThat(result.getEvaluatedAt()).isPositive();
        assertThat(result.getEvaluationDurationMs()).isGreaterThanOrEqualTo(0);
        assertThat(result.getNextEligibleAt()).isNull();
        verify(planRepository, never()).save(any());
    }

    @Test
    void evaluatePlan_shouldReturnDraftReason() {
        PreventiveMaintenancePlan plan = plan(
                100L,
                "PUMP_MONTHLY",
                PreventiveMaintenancePlanStatus.DRAFT,
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}",
                LocalDate.of(2024, 1, 1));
        when(planRepository.findDetailedById(100L)).thenReturn(Optional.of(plan));

        TriggerEvaluationResultResponse result = triggerEvaluationService.evaluatePlan(100L);

        assertThat(result.isEligible()).isFalse();
        assertThat(result.getEvaluationReason()).isEqualTo("Plan is still in draft.");
    }

    @Test
    void evaluatePlan_shouldReturnPausedAndArchivedReasons() {
        PreventiveMaintenancePlan paused = plan(
                101L,
                "PAUSED_PLAN",
                PreventiveMaintenancePlanStatus.PAUSED,
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}",
                LocalDate.of(2024, 1, 1));
        PreventiveMaintenancePlan archived = plan(
                102L,
                "ARCHIVED_PLAN",
                PreventiveMaintenancePlanStatus.ARCHIVED,
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}",
                LocalDate.of(2024, 1, 1));
        when(planRepository.findDetailedById(101L)).thenReturn(Optional.of(paused));
        when(planRepository.findDetailedById(102L)).thenReturn(Optional.of(archived));

        assertThat(triggerEvaluationService.evaluatePlan(101L).getEvaluationReason())
                .isEqualTo("Plan is paused.");
        assertThat(triggerEvaluationService.evaluatePlan(102L).getEvaluationReason())
                .isEqualTo("Plan is archived.");
    }

    @Test
    void evaluatePlan_shouldDeferMeterAndEventTriggers() {
        PreventiveMaintenancePlan meterPlan = plan(
                200L,
                "COMPRESSOR_500H",
                PreventiveMaintenancePlanStatus.ACTIVE,
                PlanTriggerType.METER,
                "{\"meter\":\"OPERATING_HOURS\",\"every\":250}",
                LocalDate.of(2024, 1, 1));
        PreventiveMaintenancePlan eventPlan = plan(
                201L,
                "POST_REVIEW_CHECK",
                PreventiveMaintenancePlanStatus.ACTIVE,
                PlanTriggerType.EVENT,
                "{\"event\":\"COMPLETION_REVIEW\"}",
                LocalDate.of(2024, 1, 1));
        when(planRepository.findDetailedById(200L)).thenReturn(Optional.of(meterPlan));
        when(planRepository.findDetailedById(201L)).thenReturn(Optional.of(eventPlan));

        assertThat(triggerEvaluationService.evaluatePlan(200L).getEvaluationReason())
                .isEqualTo("Meter values are not available yet.");
        assertThat(triggerEvaluationService.evaluatePlan(200L).getNextEligibleAt()).isNull();
        assertThat(triggerEvaluationService.evaluatePlan(201L).getEvaluationReason())
                .isEqualTo("Business event evaluation is not implemented yet.");
        assertThat(triggerEvaluationService.evaluatePlan(201L).getNextEligibleAt()).isNull();
    }

    @Test
    void evaluateAllPlans_shouldEvaluateOnlyActivePlans() {
        PreventiveMaintenancePlan active = plan(
                100L,
                "PUMP_MONTHLY",
                PreventiveMaintenancePlanStatus.ACTIVE,
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}",
                LocalDate.of(2024, 1, 1));
        when(planRepository.findAllByStatus(PreventiveMaintenancePlanStatus.ACTIVE))
                .thenReturn(List.of(active));

        List<TriggerEvaluationResultResponse> results = triggerEvaluationService.evaluateAllPlans(
                LocalDateTime.of(2024, 2, 1, 0, 0));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getPlanId()).isEqualTo(100L);
        verify(planRepository, never()).save(any());
    }

    @Test
    void evaluatePlan_shouldRejectMissingPlan() {
        when(planRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> triggerEvaluationService.evaluatePlan(999L))
                .isInstanceOf(NotFoundException.class);
    }

    private PreventiveMaintenancePlan plan(
            Long id,
            String planCode,
            PreventiveMaintenancePlanStatus status,
            PlanTriggerType triggerType,
            String configurationJson,
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
        PlanBusinessTrigger trigger = new PlanBusinessTrigger(triggerType, configurationJson, true);
        trigger.setId(200L);
        plan.setBusinessTrigger(trigger);
        return plan;
    }
}
