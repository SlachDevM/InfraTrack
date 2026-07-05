package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncConflictResolutionStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SyncConflictResolutionMetricsRecorderTest {

    @Test
    void record_incrementsMatchingCounter() {
        SimpleMeterRegistry registry = new SimpleMeterRegistry();
        SyncConflictResolutionMetricsRecorder recorder = new SyncConflictResolutionMetricsRecorder(registry);

        recorder.record(SyncConflictResolutionStatus.RESOLVED);
        recorder.record(SyncConflictResolutionStatus.RETRY_REQUIRED);
        recorder.record(SyncConflictResolutionStatus.MANUAL_REVIEW_REQUIRED);
        recorder.record(SyncConflictResolutionStatus.REJECTED);

        assertThat(registry.get("mobile.sync.conflicts.resolved").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("mobile.sync.conflicts.retry_required").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("mobile.sync.conflicts.manual_review_required").counter().count()).isEqualTo(1.0);
        assertThat(registry.get("mobile.sync.conflicts.rejected").counter().count()).isEqualTo(1.0);
    }
}
