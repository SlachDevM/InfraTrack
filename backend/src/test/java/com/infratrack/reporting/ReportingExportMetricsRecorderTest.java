package com.infratrack.reporting;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReportingExportMetricsRecorderTest {

    private SimpleMeterRegistry meterRegistry;
    private ReportingExportMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        recorder = new ReportingExportMetricsRecorder(meterRegistry);
    }

    @Test
    void recordExport_csv_incrementsCsvCounterAndDuration() {
        CsvExportResponse response = recorder.recordExport(
                ExportType.ASSETS,
                ReportingExportFormat.CSV,
                () -> new CsvExportResponse(new byte[] {1}, "assets-export.csv"));

        assertThat(response.filename()).isEqualTo("assets-export.csv");
        assertThat(meterRegistry.get("reporting.export.csv").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("reporting.export.duration")
                .tag("entity", "assets")
                .tag("format", "csv")
                .timer()
                .count()).isEqualTo(1);
    }

    @Test
    void recordExport_failure_incrementsFailureCounter() {
        assertThatThrownBy(() -> recorder.recordExport(
                ExportType.ISSUES,
                ReportingExportFormat.PDF,
                () -> {
                    throw new IllegalStateException("export failed");
                }))
                .isInstanceOf(IllegalStateException.class);

        assertThat(meterRegistry.get("reporting.export.failure").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("reporting.export.pdf").counter().count()).isZero();
    }
}
