package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SyncConflictResponse {

    @Schema(description = "Client operation id that conflicted")
    private String operationId;

    @Schema(description = "Entity type", example = "INSPECTION")
    private String entityType;

    @Schema(description = "Entity identifier when applicable")
    private Long entityId;

    @Schema(description = "Conflict classification", example = "ENTITY_MODIFIED")
    private SyncConflictType conflictType;

    @Schema(description = "Opaque server state snapshot")
    private String serverState;

    @Schema(description = "Opaque client state snapshot")
    private String clientState;

    @Schema(description = "Human-readable conflict explanation")
    private String message;

    public SyncConflictResponse() {
    }

    public SyncConflictResponse(
            String operationId,
            String entityType,
            Long entityId,
            SyncConflictType conflictType,
            String serverState,
            String clientState,
            String message) {
        this.operationId = operationId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.conflictType = conflictType;
        this.serverState = serverState;
        this.clientState = clientState;
        this.message = message;
    }

    public String getOperationId() {
        return operationId;
    }

    public void setOperationId(String operationId) {
        this.operationId = operationId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public SyncConflictType getConflictType() {
        return conflictType;
    }

    public void setConflictType(SyncConflictType conflictType) {
        this.conflictType = conflictType;
    }

    public String getServerState() {
        return serverState;
    }

    public void setServerState(String serverState) {
        this.serverState = serverState;
    }

    public String getClientState() {
        return clientState;
    }

    public void setClientState(String clientState) {
        this.clientState = clientState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
