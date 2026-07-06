package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;

/**
 * Outcome of processing one pending operation, optionally with a detected conflict (M5.5-BE1).
 */
final class SyncOperationHandlerResult {

    private final SyncOperationResponse operation;
    private final SyncConflictResponse conflict;
    private final boolean duplicate;

    SyncOperationHandlerResult(SyncOperationResponse operation, SyncConflictResponse conflict, boolean duplicate) {
        this.operation = operation;
        this.conflict = conflict;
        this.duplicate = duplicate;
    }

    static SyncOperationHandlerResult withoutConflict(SyncOperationResponse operation) {
        return new SyncOperationHandlerResult(operation, null, false);
    }

    static SyncOperationHandlerResult withConflict(
            SyncOperationResponse operation,
            SyncConflictResponse conflict) {
        return new SyncOperationHandlerResult(operation, conflict, false);
    }

    static SyncOperationHandlerResult duplicate(SyncOperationResponse operation, SyncConflictResponse conflict) {
        return new SyncOperationHandlerResult(operation, conflict, true);
    }

    SyncOperationResponse operation() {
        return operation;
    }

    SyncConflictResponse conflict() {
        return conflict;
    }

    boolean duplicate() {
        return duplicate;
    }
}
