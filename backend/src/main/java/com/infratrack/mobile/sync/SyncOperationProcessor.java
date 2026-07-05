package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;

import java.util.List;

/**
 * Extension point for processing uploaded pending operations (BDR-005 / M5.2+).
 */
public interface SyncOperationProcessor {

    SyncOperationBatchResult process(Long userId, List<PendingOperationRequest> pendingOperations);
}
