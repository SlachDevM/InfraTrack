package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;

/**
 * Handles one supported {@link PendingOperationRequest} type during mobile sync upload processing.
 */
public interface SyncOperationHandler {

    boolean supports(PendingOperationRequest operation);

    SyncOperationHandlerResult process(PendingOperationRequest operation, Long userId);
}
