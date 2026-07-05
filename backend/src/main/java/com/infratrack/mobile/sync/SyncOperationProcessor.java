package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;

import java.util.List;

/**
 * Extension point for processing uploaded pending operations (BDR-005 / M5.2+).
 * M5.2-BE1 does not invoke processing — implementations may remain no-op.
 */
public interface SyncOperationProcessor {

    List<SyncOperationResponse> process(Long userId, List<PendingOperationRequest> pendingOperations);
}
