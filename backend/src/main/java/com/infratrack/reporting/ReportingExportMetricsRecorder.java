package com.infratrack.reporting;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * Micrometer instrumentation for reporting exports (V2.5-STAB-3).
 */
@Component
public class ReportingExportMetricsRecorder {

    private final Counter csvExports;
    private final Counter xlsxExports;
    private final Counter pdfExports;
    private final Counter exportFailures;
    private final MeterRegistry meterRegistry;

    public ReportingExportMetricsRecorder(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.csvExports = Counter.builder("reporting.export.csv")
                .description("Successful CSV reporting exports")
                .register(meterRegistry);
        this.xlsxExports = Counter.builder("reporting.export.xlsx")
                .description("Successful XLSX reporting exports")
                .register(meterRegistry);
        this.pdfExports = Counter.builder("reporting.export.pdf")
                .description("Successful PDF reporting exports")
                .register(meterRegistry);
        this.exportFailures = Counter.builder("reporting.export.failure")
                .description("Failed reporting exports")
                .register(meterRegistry);
    }

    public <T> T recordExport(ExportType entity, ReportingExportFormat format, Supplier<T> action) {
        long start = System.nanoTime();
        try {
            T result = action.get();
            incrementSuccess(format);
            return result;
        } catch (RuntimeException ex) {
            exportFailures.increment();
            throw ex;
        } finally {
            durationTimer(entity, format).record(System.nanoTime() - start, TimeUnit.NANOSECONDS);
        }
    }

    private void incrementSuccess(ReportingExportFormat format) {
        switch (format) {
            case CSV -> csvExports.increment();
            case XLSX -> xlsxExports.increment();
            case PDF -> pdfExports.increment();
        }
    }

    private Timer durationTimer(ExportType entity, ReportingExportFormat format) {
        return Timer.builder("reporting.export.duration")
                .description("Reporting export duration")
                .tag("entity", entity.name().toLowerCase())
                .tag("format", format.name().toLowerCase())
                .register(meterRegistry);
    }
}
