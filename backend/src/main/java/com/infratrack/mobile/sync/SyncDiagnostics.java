package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;

import java.util.List;

/**
 * Per-request sync counters and timing for metrics and logging (M5.4.1-BE).
 */
final class SyncDiagnostics {

    private final long startNanos;
    private int processed;
    private int accepted;
    private int rejected;
    private int ignored;
    private int conflicts;
    private int deltaInspections;

    private SyncDiagnostics(long startNanos) {
        this.startNanos = startNanos;
    }

    static SyncDiagnostics start() {
        return new SyncDiagnostics(System.nanoTime());
    }

    void recordOperations(List<SyncOperationResponse> operations) {
        if (operations == null || operations.isEmpty()) {
            return;
        }
        processed = operations.size();
        for (SyncOperationResponse operation : operations) {
            if (operation.getStatus() == null) {
                continue;
            }
            switch (operation.getStatus()) {
                case ACCEPTED -> accepted++;
                case REJECTED -> rejected++;
                case IGNORED -> ignored++;
                case CONFLICT -> conflicts++;
                default -> {
                }
            }
        }
    }

    void recordDeltaInspections(int count) {
        deltaInspections = Math.max(0, count);
    }

    long elapsedMillis() {
        return (System.nanoTime() - startNanos) / 1_000_000L;
    }

    int processed() {
        return processed;
    }

    int accepted() {
        return accepted;
    }

    int rejected() {
        return rejected;
    }

    int ignored() {
        return ignored;
    }

    int conflicts() {
        return conflicts;
    }

    int deltaInspections() {
        return deltaInspections;
    }
}
