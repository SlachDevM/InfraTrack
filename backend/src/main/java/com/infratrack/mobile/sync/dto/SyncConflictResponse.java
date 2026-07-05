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

    @Schema(description = "Informational resolution guidance for clients; no server-side merge")
    private SyncResolutionHint resolutionHint;

    @Schema(description = "Compact server entity snapshot when available")
    private SyncConflictServerState serverState;

    @Schema(description = "Client operation snapshot")
    private SyncConflictClientState clientState;

    @Schema(description = "Human-readable conflict explanation")
    private String message;

    public SyncConflictResponse() {
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

    public SyncResolutionHint getResolutionHint() {
        return resolutionHint;
    }

    public void setResolutionHint(SyncResolutionHint resolutionHint) {
        this.resolutionHint = resolutionHint;
    }

    public SyncConflictServerState getServerState() {
        return serverState;
    }

    public void setServerState(SyncConflictServerState serverState) {
        this.serverState = serverState;
    }

    public SyncConflictClientState getClientState() {
        return clientState;
    }

    public void setClientState(SyncConflictClientState clientState) {
        this.clientState = clientState;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
