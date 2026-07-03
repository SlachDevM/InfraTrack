package com.infratrack.organization.policy.reporting;

import com.infratrack.reporting.ReportingExportFormat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DefaultReportingPolicyTest {

    private DefaultReportingPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new DefaultReportingPolicy();
    }

    @Test
    void defaultExportFormat_shouldBeCsv() {
        assertThat(policy.defaultExportFormat()).isEqualTo(ReportingExportFormat.CSV);
    }

    @Test
    void enabledExportFormats_shouldIncludeCsvXlsxAndPdf() {
        assertThat(policy.enabledExportFormats()).containsExactly(
                ReportingExportFormat.CSV,
                ReportingExportFormat.XLSX,
                ReportingExportFormat.PDF);
    }

    @Test
    void defaultReportingDateRange_shouldBeLast30Days() {
        assertThat(policy.defaultReportingDateRange()).isEqualTo(ReportingDateRange.LAST_30_DAYS);
    }
}
