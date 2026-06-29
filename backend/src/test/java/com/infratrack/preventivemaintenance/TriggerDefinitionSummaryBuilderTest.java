package com.infratrack.preventivemaintenance;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class TriggerDefinitionSummaryBuilderTest {

    @Test
    void buildSummary_shouldUseSingularMonthTitle() {
        TriggerSummary summary = TriggerDefinitionSummaryBuilder.buildSummary(
                PlanTriggerType.TIME,
                "{\"every\":1,\"unit\":\"MONTH\"}");

        assertThat(summary.getTitle()).isEqualTo("Every month");
        assertThat(summary.getDescription()).contains("full month");
    }

    @Test
    void buildSummary_shouldDescribeMeterTrigger() {
        TriggerSummary summary = TriggerDefinitionSummaryBuilder.buildSummary(
                PlanTriggerType.METER,
                "{\"meter\":\"OPERATING_HOURS\",\"every\":250}");

        assertThat(summary.getTitle()).isEqualTo("Every 250 operating hours");
        assertThat(summary.getTriggerType()).isEqualTo(PlanTriggerType.METER);
    }

    @Test
    void buildSummary_shouldDescribeEventTrigger() {
        TriggerSummary summary = TriggerDefinitionSummaryBuilder.buildSummary(
                PlanTriggerType.EVENT,
                "{\"event\":\"COMPLETION_REVIEW\"}");

        assertThat(summary.getTitle()).isEqualTo("After Completion Review");
    }

    @Test
    void buildSummary_shouldUsePluralUnits() {
        TriggerSummary summary = TriggerDefinitionSummaryBuilder.buildSummary(
                PlanTriggerType.TIME,
                "{\"every\":2,\"unit\":\"WEEK\"}");

        assertThat(summary.getTitle()).isEqualTo("Every 2 weeks");
    }
}
