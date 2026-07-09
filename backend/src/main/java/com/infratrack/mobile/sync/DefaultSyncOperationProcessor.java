package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Processes uploaded pending operations independently (M5.3-BE+).
 */
@Service
class DefaultSyncOperationProcessor implements SyncOperationProcessor {

    private static final String UNSUPPORTED_OPERATION_MESSAGE = "Unsupported sync operation type.";

    private final List<SyncOperationHandler> handlers;
    private final ProcessedSyncOperationService processedSyncOperationService;

    DefaultSyncOperationProcessor(
            @Qualifier("syncOperationHandlers") List<SyncOperationHandler> handlers,
            ProcessedSyncOperationService processedSyncOperationService) {
        this.handlers = handlers != null ? handlers : List.of();
        this.processedSyncOperationService = processedSyncOperationService;
    }

    @Override
    public SyncOperationBatchResult process(Long userId, List<PendingOperationRequest> pendingOperations) {
        if (pendingOperations == null || pendingOperations.isEmpty()) {
            return SyncOperationBatchResult.empty();
        }
        List<SyncOperationResponse> operations = new ArrayList<>(pendingOperations.size());
        List<SyncConflictResponse> conflicts = new ArrayList<>();
        int duplicateOperations = 0;
        for (PendingOperationRequest operation : pendingOperations) {
            SyncOperationHandlerResult result = processedSyncOperationService.processIdempotently(
                    userId,
                    operation,
                    () -> executeOperation(userId, operation));
            operations.add(result.operation());
            if (result.conflict() != null) {
                conflicts.add(result.conflict());
            }
            if (result.duplicate()) {
                duplicateOperations++;
            }
        }
        return new SyncOperationBatchResult(operations, conflicts, duplicateOperations);
    }

    private SyncOperationHandlerResult executeOperation(Long userId, PendingOperationRequest operation) {
        if (!SyncLimits.isPayloadWithinLimit(operation.getPayload())) {
            return SyncOperationHandlerResult.withoutConflict(rejectedPayloadTooLarge(operation));
        }
        for (SyncOperationHandler handler : handlers) {
            if (handler.supports(operation)) {
                return handler.process(operation, userId);
            }
        }
        return SyncOperationHandlerResult.withoutConflict(ignored(operation));
    }

    private SyncOperationResponse ignored(PendingOperationRequest operation) {
        SyncOperationResponse response = new SyncOperationResponse();
        response.setOperationId(operation.getOperationId());
        response.setEntityType(operation.getEntityType());
        response.setEntityId(operation.getEntityId());
        response.setOperationType(operation.getOperationType());
        response.setStatus(SyncOperationStatus.IGNORED);
        response.setMessage(UNSUPPORTED_OPERATION_MESSAGE);
        return response;
    }

    private SyncOperationResponse rejectedPayloadTooLarge(PendingOperationRequest operation) {
        SyncOperationResponse response = new SyncOperationResponse();
        response.setOperationId(operation.getOperationId());
        response.setEntityType(operation.getEntityType());
        response.setEntityId(operation.getEntityId());
        response.setOperationType(operation.getOperationType());
        response.setStatus(SyncOperationStatus.REJECTED);
        response.setMessage(SyncLimits.PAYLOAD_SIZE_MESSAGE);
        return response;
    }
}
