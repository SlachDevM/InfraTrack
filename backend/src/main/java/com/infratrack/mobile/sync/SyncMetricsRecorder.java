package com.infratrack.mobile.sync;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Micrometer instrumentation for mobile sync (M5.4.1-BE).
 */
@Component
class SyncMetricsRecorder {

    private final Counter requests;
    private final Counter operationsAccepted;
    private final Counter operationsRejected;
    private final Counter operationsIgnored;
    private final Counter operationsConflict;
    private final Counter deltaInspections;
    private final Timer duration;

    SyncMetricsRecorder(MeterRegistry meterRegistry) {
        this.requests = Counter.builder("mobile.sync.requests")
                .description("Mobile sync handshake requests")
                .register(meterRegistry);
        this.operationsAccepted = Counter.builder("mobile.sync.operations.accepted")
                .description("Accepted pending sync operations")
                .register(meterRegistry);
        this.operationsRejected = Counter.builder("mobile.sync.operations.rejected")
                .description("Rejected pending sync operations")
                .register(meterRegistry);
        this.operationsIgnored = Counter.builder("mobile.sync.operations.ignored")
                .description("Ignored pending sync operations")
                .register(meterRegistry);
        this.operationsConflict = Counter.builder("mobile.sync.operations.conflict")
                .description("Conflict pending sync operations")
                .register(meterRegistry);
        this.deltaInspections = Counter.builder("mobile.sync.delta.inspections")
                .description("Inspection records returned in sync delta")
                .register(meterRegistry);
        this.duration = Timer.builder("mobile.sync.duration")
                .description("Mobile sync handshake duration")
                .register(meterRegistry);
    }

    void record(SyncDiagnostics diagnostics) {
        requests.increment();
        if (diagnostics.accepted() > 0) {
            operationsAccepted.increment(diagnostics.accepted());
        }
        if (diagnostics.rejected() > 0) {
            operationsRejected.increment(diagnostics.rejected());
        }
        if (diagnostics.ignored() > 0) {
            operationsIgnored.increment(diagnostics.ignored());
        }
        if (diagnostics.conflicts() > 0) {
            operationsConflict.increment(diagnostics.conflicts());
        }
        if (diagnostics.deltaInspections() > 0) {
            deltaInspections.increment(diagnostics.deltaInspections());
        }
        duration.record(diagnostics.elapsedMillis(), TimeUnit.MILLISECONDS);
    }
}
