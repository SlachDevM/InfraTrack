package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessException;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import com.infratrack.workorder.WorkOrderService;

import java.util.Optional;

/**
 * Maps service-layer exceptions to sync conflict classifications (M5.5-BE1).
 * {@link BusinessValidationException} is not a conflict — callers treat it as {@code REJECTED}.
 */
final class SyncConflictClassifier {

    private static final String INSPECTION_ENTITY_TYPE = "INSPECTION";
    private static final String WORK_ORDER_ENTITY_TYPE = "WORK_ORDER";

    private SyncConflictClassifier() {
    }

    record Classification(SyncConflictType conflictType, String message) {
    }

    static Optional<Classification> classify(BusinessException exception) {
        return classify(exception, INSPECTION_ENTITY_TYPE);
    }

    static Optional<Classification> classify(BusinessException exception, String entityType) {
        if (exception instanceof BusinessValidationException) {
            return Optional.empty();
        }
        if (exception instanceof NotFoundException) {
            return Optional.of(new Classification(
                    SyncConflictType.ENTITY_DELETED,
                    entityDeletedMessage(entityType)));
        }
        if (exception instanceof ForbiddenOperationException) {
            return Optional.of(new Classification(
                    SyncConflictType.PERMISSION_DENIED,
                    exception.getMessage()));
        }
        if (exception instanceof ConflictException conflictException) {
            String message = conflictException.getMessage();
            if (message != null && message.contains("Duplicate answer")) {
                return Optional.of(new Classification(SyncConflictType.VERSION_MISMATCH, message));
            }
            return Optional.of(new Classification(
                    SyncConflictType.WORKFLOW_COMPLETED,
                    workflowCompletedMessage(entityType)));
        }
        return Optional.of(new Classification(
                SyncConflictType.UNKNOWN,
                exception.getMessage()));
    }

    static SyncResolutionHint resolutionHint(SyncConflictType conflictType) {
        return switch (conflictType) {
            case WORKFLOW_COMPLETED, ENTITY_DELETED -> SyncResolutionHint.SERVER_WINS;
            case PERMISSION_DENIED -> SyncResolutionHint.MANUAL_REVIEW;
            case VERSION_MISMATCH -> SyncResolutionHint.CLIENT_RETRY;
            default -> SyncResolutionHint.UNKNOWN;
        };
    }

    static SyncConflictResponse toConflictResponse(
            PendingOperationRequest operation,
            String entityType,
            Long entityId,
            Classification classification,
            InspectionService inspectionService,
            ObjectMapper objectMapper) {
        return toConflictResponse(
                operation, entityType, entityId, classification, inspectionService, null, objectMapper);
    }

    static SyncConflictResponse toConflictResponse(
            PendingOperationRequest operation,
            String entityType,
            Long entityId,
            Classification classification,
            InspectionService inspectionService,
            WorkOrderService workOrderService,
            ObjectMapper objectMapper) {
        SyncConflictResponse conflict = new SyncConflictResponse();
        conflict.setOperationId(operation.getOperationId());
        conflict.setEntityType(entityType);
        conflict.setEntityId(entityId);
        conflict.setConflictType(classification.conflictType());
        conflict.setResolutionHint(resolutionHint(classification.conflictType()));
        conflict.setMessage(classification.message());
        conflict.setClientState(SyncConflictEnrichment.buildClientState(operation, objectMapper));
        conflict.setServerState(SyncConflictEnrichment.buildServerState(
                inspectionService, workOrderService, entityType, entityId, classification.conflictType()));
        return conflict;
    }

    private static String entityDeletedMessage(String entityType) {
        if (WORK_ORDER_ENTITY_TYPE.equals(entityType)) {
            return "Work order no longer exists.";
        }
        return "Inspection no longer exists.";
    }

    private static String workflowCompletedMessage(String entityType) {
        if (WORK_ORDER_ENTITY_TYPE.equals(entityType)) {
            return "Work order is no longer editable.";
        }
        return "Inspection is no longer editable.";
    }
}
