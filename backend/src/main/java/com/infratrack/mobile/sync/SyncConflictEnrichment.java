package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictClientState;
import com.infratrack.mobile.sync.dto.SyncConflictServerState;
import com.infratrack.mobile.sync.dto.SyncConflictType;

/**
 * Builds enriched conflict snapshots from pending operations and existing service data (M5.5-BE1.1).
 */
final class SyncConflictEnrichment {

    private SyncConflictEnrichment() {
    }

    static SyncConflictClientState buildClientState(PendingOperationRequest operation, ObjectMapper objectMapper) {
        SyncConflictClientState clientState = new SyncConflictClientState();
        clientState.setOperationType(operation.getOperationType());
        clientState.setCreatedAt(operation.getCreatedAt());
        clientState.setPayload(parsePayload(operation.getPayload(), objectMapper));
        return clientState;
    }

    static SyncConflictServerState buildServerState(
            InspectionService inspectionService,
            String entityType,
            Long entityId,
            SyncConflictType conflictType) {
        if (entityId == null || conflictType == SyncConflictType.ENTITY_DELETED) {
            return null;
        }
        try {
            InspectionResponse inspection = inspectionService.getById(entityId);
            if (inspection == null) {
                return null;
            }
            return fromInspection(entityType, inspection);
        } catch (NotFoundException ex) {
            return null;
        }
    }

    private static SyncConflictServerState fromInspection(String entityType, InspectionResponse inspection) {
        SyncConflictServerState serverState = new SyncConflictServerState();
        serverState.setEntityId(inspection.getId());
        serverState.setEntityType(entityType);
        serverState.setStatus(inspection.getStatus());
        serverState.setUpdatedAt(inspection.getUpdatedAt());
        serverState.setCompletedAt(inspection.getCompletedAt());
        serverState.setAssignedTo(inspection.getAssignedToUserId());
        serverState.setAssignedToName(inspection.getAssignedToUserName());
        return serverState;
    }

    private static JsonNode parsePayload(String payload, ObjectMapper objectMapper) {
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            return objectMapper.readTree(payload);
        } catch (Exception ex) {
            return null;
        }
    }
}
