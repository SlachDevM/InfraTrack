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
    void record_incrementsCountersAndTimer() {
        SyncDiagnostics diagnostics = SyncDiagnostics.start();
        diagnostics.recordOperations(List.of(
                operation(SyncOperationStatus.ACCEPTED),
                operation(SyncOperationStatus.CONFLICT),
                operation(SyncOperationStatus.REJECTED),
                operation(SyncOperationStatus.IGNORED)));
        diagnostics.recordDeltaInspections(3);

        recorder.record(diagnostics);

        assertThat(meterRegistry.get("mobile.sync.requests").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.accepted").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.rejected").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.ignored").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.conflict").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.delta.inspections").counter().count()).isEqualTo(3.0);
        assertThat(meterRegistry.get("mobile.sync.duration").timer().count()).isEqualTo(1);
    }

    private static com.infratrack.mobile.sync.dto.SyncOperationResponse operation(SyncOperationStatus status) {
        com.infratrack.mobile.sync.dto.SyncOperationResponse response =
                new com.infratrack.mobile.sync.dto.SyncOperationResponse();
        response.setStatus(status);
        return response;
    }
}
