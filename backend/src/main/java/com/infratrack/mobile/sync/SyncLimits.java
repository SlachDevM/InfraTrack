package com.infratrack.mobile.sync;

import java.nio.charset.StandardCharsets;

/**
 * Mobile sync request limits (M5.4.1-BE).
 */
public final class SyncLimits {

    public static final int MAX_PENDING_OPERATIONS = 100;

    public static final int MAX_OPERATION_PAYLOAD_BYTES = 256 * 1024;

    public static final String BATCH_LIMIT_MESSAGE =
            "Synchronization requests cannot contain more than 100 pending operations.";

    public static final String PAYLOAD_SIZE_MESSAGE =
            "Sync operation payload exceeds maximum supported size.";

    private SyncLimits() {
    }

    static boolean isPayloadWithinLimit(String payload) {
        if (payload == null || payload.isBlank()) {
            return true;
        }
        return payload.getBytes(StandardCharsets.UTF_8).length <= MAX_OPERATION_PAYLOAD_BYTES;
    }
}
