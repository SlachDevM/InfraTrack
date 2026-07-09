package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.SaveInspectionProgressRequest;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Applies queued {@code SAVE_INSPECTION_PROGRESS} operations through {@link InspectionService}.
 */
@Service
class InspectionProgressSyncOperationHandler implements SyncOperationHandler {

    static final String OPERATION_TYPE = "SAVE_INSPECTION_PROGRESS";
    static final String ENTITY_TYPE = "INSPECTION";
    static final String INVALID_PAYLOAD_MESSAGE = "Invalid sync operation payload.";
    static final String INVALID_ENTITY_ID_MESSAGE = "Invalid sync operation entity id.";
    static final String GENERIC_FAILURE_MESSAGE = "Sync operation could not be processed.";

    private final InspectionService inspectionService;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    InspectionProgressSyncOperationHandler(
            InspectionService inspectionService,
            ObjectMapper objectMapper,
            Clock clock) {
        this.inspectionService = inspectionService;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    @Override
    public boolean supports(PendingOperationRequest operation) {
        return SyncOperationMatching.matches(operation, ENTITY_TYPE, OPERATION_TYPE);
    }

    @Override
    public SyncOperationHandlerResult process(PendingOperationRequest operation, Long userId) {
        Long inspectionId = operation.getEntityId();
        if (inspectionId == null) {
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, null, INVALID_ENTITY_ID_MESSAGE));
        }

        SaveInspectionProgressRequest progressRequest = parsePayload(operation.getPayload());
        if (progressRequest == null) {
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, inspectionId, INVALID_PAYLOAD_MESSAGE));
        }

        try {
            inspectionService.saveInspectionProgress(inspectionId, progressRequest, userId);
            return SyncOperationHandlerResult.withoutConflict(accepted(operation, inspectionId));
        } catch (BusinessException ex) {
            Optional<SyncConflictClassifier.Classification> conflict = SyncConflictClassifier.classify(ex, ENTITY_TYPE);
            if (conflict.isPresent()) {
                SyncConflictClassifier.Classification classification = conflict.get();
                SyncOperationResponse operationResponse =
                        conflict(operation, inspectionId, classification.message());
                SyncConflictResponse conflictResponse = SyncConflictClassifier.toConflictResponse(
                        operation, ENTITY_TYPE, inspectionId, classification, inspectionService, null, objectMapper);
                return SyncOperationHandlerResult.withConflict(operationResponse, conflictResponse);
            }
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, inspectionId, ex.getMessage()));
        } catch (RuntimeException ex) {
            return SyncOperationHandlerResult.withoutConflict(
                    rejected(operation, inspectionId, GENERIC_FAILURE_MESSAGE));
        }
    }

    private SaveInspectionProgressRequest parsePayload(String payload) {
        if (payload == null || payload.isBlank()) {
            return new SaveInspectionProgressRequest();
        }
        try {
            return objectMapper.readValue(payload, SaveInspectionProgressRequest.class);
        } catch (Exception ex) {
            return null;
        }
    }

    private SyncOperationResponse accepted(PendingOperationRequest operation, Long inspectionId) {
        SyncOperationResponse response = baseResponse(operation, inspectionId);
        response.setStatus(SyncOperationStatus.ACCEPTED);
        response.setServerUpdatedAt(clock.instant().truncatedTo(ChronoUnit.MICROS));
        return response;
    }

    private SyncOperationResponse rejected(
            PendingOperationRequest operation,
            Long inspectionId,
            String message) {
        SyncOperationResponse response = baseResponse(operation, inspectionId);
        response.setStatus(SyncOperationStatus.REJECTED);
        response.setMessage(message);
        return response;
    }

    private SyncOperationResponse conflict(
            PendingOperationRequest operation,
            Long inspectionId,
            String message) {
        SyncOperationResponse response = baseResponse(operation, inspectionId);
        response.setStatus(SyncOperationStatus.CONFLICT);
        response.setMessage(message);
        return response;
    }

    private SyncOperationResponse baseResponse(PendingOperationRequest operation, Long inspectionId) {
        SyncOperationResponse response = new SyncOperationResponse();
        response.setOperationId(operation.getOperationId());
        response.setEntityType(ENTITY_TYPE);
        response.setEntityId(inspectionId);
        response.setOperationType(OPERATION_TYPE);
        return response;
    }
}
