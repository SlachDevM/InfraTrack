package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;

/**
 * Outcome of processing one pending operation, optionally with a detected conflict (M5.5-BE1).
 */
final class SyncOperationHandlerResult {

    private final SyncOperationResponse operation;
    private final SyncConflictResponse conflict;

    SyncOperationHandlerResult(SyncOperationResponse operation, SyncConflictResponse conflict) {
        this.operation = operation;
        this.conflict = conflict;
    }

    static SyncOperationHandlerResult withoutConflict(SyncOperationResponse operation) {
        return new SyncOperationHandlerResult(operation, null);
    }

    static SyncOperationHandlerResult withConflict(
            SyncOperationResponse operation,
            SyncConflictResponse conflict) {
        return new SyncOperationHandlerResult(operation, conflict);
    }

    SyncOperationResponse operation() {
        return operation;
    }

    SyncConflictResponse conflict() {
        return conflict;
    }
}
