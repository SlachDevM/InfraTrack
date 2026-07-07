package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessException;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.workorder.WorkOrderService;
import com.infratrack.workorder.dto.SaveWorkOrderProgressRequest;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Optional;

/**
 * Applies queued {@code SAVE_WORK_ORDER_PROGRESS} operations through {@link WorkOrderService}.
 */
@Service
class WorkOrderProgressSyncOperationHandler implements SyncOperationHandler {

    static final String OPERATION_TYPE = "SAVE_WORK_ORDER_PROGRESS";
    static final String ENTITY_TYPE = "WORK_ORDER";
    static final String INVALID_PAYLOAD_MESSAGE = "Invalid sync operation payload.";
    static final String INVALID_ENTITY_ID_MESSAGE = "Invalid sync operation entity id.";
    static final String GENERIC_FAILURE_MESSAGE = "Sync operation could not be processed.";

    private final WorkOrderService workOrderService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    WorkOrderProgressSyncOperationHandler(
            WorkOrderService workOrderService,
            ObjectMapper objectMapper,
            Clock clock) {
        this.workOrderService = workOrderService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(PendingOperationRequest operation) {
        return OPERATION_TYPE.equals(operation.getOperationType())
                && ENTITY_TYPE.equals(operation.getEntityType());
    }

    @Override
    public SyncOperationHandlerResult process(PendingOperationRequest operation, Long userId) {
        Long workOrderId = operation.getEntityId();
        if (workOrderId == null) {
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, null, INVALID_ENTITY_ID_MESSAGE));
        }

        SaveWorkOrderProgressRequest progressRequest = parsePayload(operation.getPayload());
        if (progressRequest == null) {
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, workOrderId, INVALID_PAYLOAD_MESSAGE));
        }

        try {
            workOrderService.saveWorkOrderProgress(workOrderId, progressRequest, userId);
            return SyncOperationHandlerResult.withoutConflict(accepted(operation, workOrderId));
        } catch (BusinessException ex) {
            Optional<SyncConflictClassifier.Classification> conflict =
                    SyncConflictClassifier.classify(ex, ENTITY_TYPE);
            if (conflict.isPresent()) {
                SyncConflictClassifier.Classification classification = conflict.get();
                SyncOperationResponse operationResponse =
                        conflict(operation, workOrderId, classification.message());
                SyncConflictResponse conflictResponse = SyncConflictClassifier.toConflictResponse(
                        operation, ENTITY_TYPE, workOrderId, classification, null, workOrderService, objectMapper);
                return SyncOperationHandlerResult.withConflict(operationResponse, conflictResponse);
            }
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, workOrderId, ex.getMessage()));
        } catch (RuntimeException ex) {
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, workOrderId, GENERIC_FAILURE_MESSAGE));
        }
    }

    private SaveWorkOrderProgressRequest parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return new SaveWorkOrderProgressRequest();
        }
        try {
            return objectMapper.readValue(payload, SaveWorkOrderProgressRequest.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private SyncOperationResponse accepted(PendingOperationRequest operation, Long workOrderId) {
        SyncOperationResponse response = baseResponse(operation, workOrderId);
        response.setStatus(SyncOperationStatus.ACCEPTED);
        response.setServerUpdatedAt(clock.instant());
        return response;
    }

    private SyncOperationResponse rejected(
            PendingOperationRequest operation,
            Long workOrderId,
            String message) {
        SyncOperationResponse response = baseResponse(operation, workOrderId);
        response.setStatus(SyncOperationStatus.REJECTED);
        response.setMessage(message);
        return response;
    }

    private SyncOperationResponse conflict(
            PendingOperationRequest operation,
            Long workOrderId,
            String message) {
        SyncOperationResponse response = baseResponse(operation, workOrderId);
        response.setStatus(SyncOperationStatus.CONFLICT);
        response.setMessage(message);
        return response;
    }

    private SyncOperationResponse baseResponse(PendingOperationRequest operation, Long workOrderId) {
        SyncOperationResponse response = new SyncOperationResponse();
        response.setOperationId(operation.getOperationId());
        response.setEntityType(ENTITY_TYPE);
        response.setEntityId(workOrderId);
        response.setOperationType(OPERATION_TYPE);
        return response;
    }
}
