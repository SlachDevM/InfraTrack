package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;

import java.util.List;

/**
 * Extension point for server-side business conflict detection (BDR-005 / M5.5+).
 * M5.2-BE1 does not invoke conflict resolution — implementations may remain no-op.
 */
public interface SyncConflictResolver {

    List<SyncConflictResponse> resolve(Long userId, List<PendingOperationRequest> pendingOperations);
}
