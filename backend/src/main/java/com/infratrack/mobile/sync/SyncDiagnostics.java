package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Per-request sync counters and timing for metrics and logging (M5.4.1-BE, V2.5-STAB-3, M6.5-STAB-1).
 */
final class SyncDiagnostics {

    private final long startNanos;
    private int processed;
    private int accepted;
    private int rejected;
    private int ignored;
    private int conflicts;
    private int deltaInspections;
    private int deltaWorkOrders;
    private int deltaAssets;
    private int batchSize;
    private int duplicateOperations;
    private int protocolVersion;
    private boolean requiresFullSync;
    private boolean invalidToken;
    private boolean dashboardIncluded;
    private boolean referenceDataIncluded;
    private long responseSizeBytes = -1;
    private final Map<String, Long> sectionDurationMillis = new HashMap<>();

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

    void recordBatchSize(int count) {
        batchSize = Math.max(0, count);
    }

    void recordDuplicateOperations(int count) {
        duplicateOperations = Math.max(0, count);
    }

    void recordDeltaInspections(int count) {
        deltaInspections = Math.max(0, count);
    }

    void recordDeltaWorkOrders(int count) {
        deltaWorkOrders = Math.max(0, count);
    }

    void recordDeltaAssets(int count) {
        deltaAssets = Math.max(0, count);
    }

    void recordDashboardIncluded(boolean value) {
        dashboardIncluded = value;
    }

    void recordReferenceDataIncluded(boolean value) {
        referenceDataIncluded = value;
    }

    void recordSectionDuration(String section, long millis) {
        if (section != null && millis >= 0) {
            sectionDurationMillis.put(section, millis);
        }
    }

    void recordResponseSizeBytes(long bytes) {
        if (bytes >= 0) {
            responseSizeBytes = bytes;
        }
    }

    void recordProtocolVersion(int version) {
        protocolVersion = version;
    }

    void recordRequiresFullSync(boolean value) {
        requiresFullSync = value;
    }

    void recordInvalidToken(boolean value) {
        invalidToken = value;
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

    int deltaWorkOrders() {
        return deltaWorkOrders;
    }

    int deltaAssets() {
        return deltaAssets;
    }

    int batchSize() {
        return batchSize;
    }

    int duplicateOperations() {
        return duplicateOperations;
    }

    int protocolVersion() {
        return protocolVersion;
    }

    boolean requiresFullSync() {
        return requiresFullSync;
    }

    boolean invalidToken() {
        return invalidToken;
    }

    boolean dashboardIncluded() {
        return dashboardIncluded;
    }

    boolean referenceDataIncluded() {
        return referenceDataIncluded;
    }

    long responseSizeBytes() {
        return responseSizeBytes;
    }

    Map<String, Long> sectionDurationMillis() {
        return Map.copyOf(sectionDurationMillis);
    }
}
