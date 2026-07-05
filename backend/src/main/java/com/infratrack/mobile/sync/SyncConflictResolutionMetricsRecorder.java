package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncConflictResolutionStatus;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

/**
 * Micrometer instrumentation for explicit conflict resolution (M5.5-BE2).
 */
@Component
class SyncConflictResolutionMetricsRecorder {

    private final Counter resolved;
    private final Counter retryRequired;
    private final Counter manualReviewRequired;
    private final Counter rejected;

    SyncConflictResolutionMetricsRecorder(MeterRegistry meterRegistry) {
        this.resolved = Counter.builder("mobile.sync.conflicts.resolved")
                .description("Explicit conflict resolutions with RESOLVED outcome")
                .register(meterRegistry);
        this.retryRequired = Counter.builder("mobile.sync.conflicts.retry_required")
                .description("Explicit conflict resolutions with RETRY_REQUIRED outcome")
                .register(meterRegistry);
        this.manualReviewRequired = Counter.builder("mobile.sync.conflicts.manual_review_required")
                .description("Explicit conflict resolutions with MANUAL_REVIEW_REQUIRED outcome")
                .register(meterRegistry);
        this.rejected = Counter.builder("mobile.sync.conflicts.rejected")
                .description("Explicit conflict resolutions with REJECTED outcome")
                .register(meterRegistry);
    }

    void record(SyncConflictResolutionStatus status) {
        switch (status) {
            case RESOLVED -> resolved.increment();
            case RETRY_REQUIRED -> retryRequired.increment();
            case MANUAL_REVIEW_REQUIRED -> manualReviewRequired.increment();
            case REJECTED -> rejected.increment();
            default -> {
            }
        }
    }
}
