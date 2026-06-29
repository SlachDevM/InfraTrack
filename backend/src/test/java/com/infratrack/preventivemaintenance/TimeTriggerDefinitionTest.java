package com.infratrack.preventivemaintenance;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class TimeTriggerDefinitionTest {

    @Test
    void evaluate_shouldBeEligibleAfterOneFullMonth() {
        TriggerDefinition definition = TriggerDefinitionFactory.from(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");
        PreventiveMaintenancePlan plan = activePlan(
                LocalDate.of(2024, 1, 1),
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");

        TriggerEvaluationOutcome outcome = definition.evaluate(context(plan, LocalDateTime.of(2024, 2, 1, 9, 0)));

        assertThat(outcome.isEligible()).isTrue();
        assertThat(outcome.getEvaluationReason()).isEqualTo("One full month has elapsed.");
    }

    @Test
    void evaluate_shouldNotBeEligibleBeforeIntervalReached() {
        TriggerDefinition definition = TriggerDefinitionFactory.from(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");
        PreventiveMaintenancePlan plan = activePlan(
                LocalDate.of(2024, 6, 27),
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");

        TriggerEvaluationOutcome outcome = definition.evaluate(context(plan, LocalDateTime.of(2024, 6, 27, 12, 0)));

        assertThat(outcome.isEligible()).isFalse();
        assertThat(outcome.getEvaluationReason()).isEqualTo("Next execution interval has not been reached.");
    }

    @Test
    void evaluate_shouldSupportDayWeekAndYear() {
        PreventiveMaintenancePlan dayPlan = activePlan(
                LocalDate.of(2024, 1, 1),
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"DAY\"}");
        TriggerDefinition dayDefinition = TriggerDefinitionFactory.from(PlanTriggerType.TIME, "{\"every\":1,\"unit\":\"DAY\"}");
        assertThat(dayDefinition.evaluate(context(dayPlan, LocalDateTime.of(2024, 1, 2, 0, 0))).isEligible()).isTrue();

        PreventiveMaintenancePlan weekPlan = activePlan(
                LocalDate.of(2024, 1, 1),
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"WEEK\"}");
        TriggerDefinition weekDefinition = TriggerDefinitionFactory.from(PlanTriggerType.TIME, "{\"every\":1,\"unit\":\"WEEK\"}");
        assertThat(weekDefinition.evaluate(context(weekPlan, LocalDateTime.of(2024, 1, 8, 0, 0))).isEligible()).isTrue();

        PreventiveMaintenancePlan yearPlan = activePlan(
                LocalDate.of(2023, 1, 1),
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"YEAR\"}");
        TriggerDefinition yearDefinition = TriggerDefinitionFactory.from(PlanTriggerType.TIME, "{\"every\":1,\"unit\":\"YEAR\"}");
        assertThat(yearDefinition.evaluate(context(yearPlan, LocalDateTime.of(2024, 1, 1, 0, 0))).isEligible()).isTrue();
    }

    @Test
    void evaluate_shouldHandleLeapYearMonthBoundary() {
        PreventiveMaintenancePlan plan = activePlan(
                LocalDate.of(2024, 2, 29),
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");
        TriggerDefinition definition = TriggerDefinitionFactory.from(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");

        assertThat(definition.evaluate(context(plan, LocalDateTime.of(2024, 3, 29, 0, 0))).isEligible()).isTrue();
        assertThat(definition.evaluate(context(plan, LocalDateTime.of(2024, 3, 28, 0, 0))).isEligible()).isFalse();
    }

    @Test
    void buildSummary_shouldIncludeTitleAndDescription() {
        TriggerSummary summary = TriggerDefinitionFactory.from(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}").buildSummary();

        assertThat(summary.getTitle()).isEqualTo("Every month");
        assertThat(summary.getDescription()).isEqualTo("Eligible once every full month from plan creation.");
        assertThat(summary.getTriggerType()).isEqualTo(PlanTriggerType.TIME);
    }

    private TriggerEvaluationContext context(PreventiveMaintenancePlan plan, LocalDateTime currentDateTime) {
        return new TriggerEvaluationContext(plan, currentDateTime);
    }

    private PreventiveMaintenancePlan activePlan(LocalDate createdDate, PlanTriggerType type, String config) {
        com.infratrack.asset.Asset asset = new com.infratrack.asset.Asset(
                "Pump A",
                mock(com.infratrack.department.Department.class),
                mock(com.infratrack.assetcategory.AssetCategory.class),
                "Location",
                com.infratrack.asset.AssetStatus.ACTIVE,
                LocalDate.of(2024, 1, 1),
                1L
        );
        PreventiveMaintenancePlan plan = new PreventiveMaintenancePlan(
                asset,
                "PUMP_MONTHLY",
                "Monthly Pump Inspection",
                null,
                1,
                PreventiveMaintenancePlanStatus.ACTIVE,
                PreventiveMaintenancePlanPriority.MEDIUM,
                PlanTargetAction.CREATE_INSPECTION,
                null
        );
        plan.setId(100L);
        long createdAt = createdDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        plan.setCreatedAtMillis(createdAt);
        PlanBusinessTrigger trigger = new PlanBusinessTrigger(type, config, true);
        plan.setBusinessTrigger(trigger);
        return plan;
    }
}
