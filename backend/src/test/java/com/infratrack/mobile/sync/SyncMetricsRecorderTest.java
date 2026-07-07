package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyncMetricsRecorderTest {

    private SimpleMeterRegistry meterRegistry;
    private SyncMetricsRecorder recorder;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        recorder = new SyncMetricsRecorder(meterRegistry);
    }

    @Test
    void record_incrementsCountersTimerAndSummaries() {
        SyncDiagnostics diagnostics = SyncDiagnostics.start();
        diagnostics.recordBatchSize(4);
        diagnostics.recordProtocolVersion(1);
        diagnostics.recordInvalidToken(true);
        diagnostics.recordRequiresFullSync(true);
        diagnostics.recordOperations(List.of(
                operation(SyncOperationStatus.ACCEPTED),
                operation(SyncOperationStatus.CONFLICT),
                operation(SyncOperationStatus.REJECTED),
                operation(SyncOperationStatus.IGNORED)));
        diagnostics.recordDeltaInspections(3);
        diagnostics.recordDeltaWorkOrders(2);
        diagnostics.recordDeltaAssets(5);
        diagnostics.recordDashboardIncluded(true);
        diagnostics.recordReferenceDataIncluded(true);
        diagnostics.recordResponseSizeBytes(12_345);
        diagnostics.recordSectionDuration(SyncDeltaSections.INSPECTIONS, 42);
        diagnostics.recordSectionDuration(SyncDeltaSections.ASSETS, 18);

        recorder.record(diagnostics);

        assertThat(meterRegistry.get("mobile.sync.requests").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.accepted").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.rejected").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.ignored").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.conflict").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.delta.inspections").counter().count()).isEqualTo(3.0);
        assertThat(meterRegistry.get("mobile.sync.delta.work_orders").counter().count()).isEqualTo(2.0);
        assertThat(meterRegistry.get("mobile.sync.delta.assets").counter().count()).isEqualTo(5.0);
        assertThat(meterRegistry.get("mobile.sync.delta.dashboard").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.delta.reference_data").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.delta.size").summary().totalAmount()).isEqualTo(5.0);
        assertThat(meterRegistry.get("mobile.sync.batch.size").summary().totalAmount()).isEqualTo(4.0);
        assertThat(meterRegistry.get("mobile.sync.response.size.bytes").summary().totalAmount()).isEqualTo(12_345.0);
        assertThat(meterRegistry.get("mobile.sync.full_sync_required").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.invalid_token").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.protocol_version").tag("version", "1").counter().count())
                .isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.duration").timer().count()).isEqualTo(1);
        assertThat(meterRegistry.get("mobile.sync.delta.section.duration")
                .tag("section", SyncDeltaSections.INSPECTIONS)
                .timer()
                .count())
                .isEqualTo(1);
        assertThat(meterRegistry.get("mobile.sync.delta.section.duration")
                .tag("section", SyncDeltaSections.ASSETS)
                .timer()
                .count())
                .isEqualTo(1);
    }

    @Test
    void recordDuplicateOperation_incrementsDuplicateCounter() {
        recorder.recordDuplicateOperation();

        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
    }

    private static SyncOperationResponse operation(SyncOperationStatus status) {
        SyncOperationResponse response = new SyncOperationResponse();
        response.setStatus(status);
        return response;
    }
}
