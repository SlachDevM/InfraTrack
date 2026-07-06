package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;

import java.util.List;

/**
 * Aggregated upload processing outcome for one sync request (M5.5-BE1).
 */
record SyncOperationBatchResult(
        List<SyncOperationResponse> operations,
        List<SyncConflictResponse> conflicts,
        int duplicateOperations) {

    static SyncOperationBatchResult empty() {
        return new SyncOperationBatchResult(List.of(), List.of(), 0);
    }
}
