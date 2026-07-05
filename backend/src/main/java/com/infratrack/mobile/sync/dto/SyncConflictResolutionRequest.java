package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class SyncConflictResolutionRequest {

    @NotBlank
    @Schema(description = "Client operation id that conflicted")
    private String operationId;

    @NotBlank
    @Schema(description = "Entity type", example = "INSPECTION")
    private String entityType;

    @NotNull
    @Schema(description = "Entity identifier")
    private Long entityId;

    @NotBlank
    @Schema(description = "Operation type", example = "SAVE_INSPECTION_PROGRESS")
    private String operationType;

    @NotNull
    @Schema(description = "Conflict classification from sync response")
    private SyncConflictType conflictType;

    @NotNull
    @Schema(description = "Explicit resolution decision")
    private SyncConflictResolutionAction resolution;

    @Schema(description = "Optional client state snapshot from the conflict payload")
    private SyncConflictClientState clientState;

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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public SyncConflictType getConflictType() {
        return conflictType;
    }

    public void setConflictType(SyncConflictType conflictType) {
        this.conflictType = conflictType;
    }

    public SyncConflictResolutionAction getResolution() {
        return resolution;
    }

    public void setResolution(SyncConflictResolutionAction resolution) {
        this.resolution = resolution;
    }

    public SyncConflictClientState getClientState() {
        return clientState;
    }

    public void setClientState(SyncConflictClientState clientState) {
        this.clientState = clientState;
    }
}
