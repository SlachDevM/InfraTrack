package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public class SyncOperationResponse {

    @Schema(description = "Client operation id echoed from the upload request")
    private String operationId;

    @Schema(description = "Entity type", example = "INSPECTION")
    private String entityType;

    @Schema(description = "Entity identifier when applicable")
    private Long entityId;

    @Schema(description = "Operation type", example = "SAVE_INSPECTION_PROGRESS")
    private String operationType;

    @Schema(description = "Processing outcome", example = "ACCEPTED")
    private SyncOperationStatus status;

    @Schema(description = "Human-readable outcome detail")
    private String message;

    @Schema(description = "Server-side update timestamp when applicable")
    private Instant serverUpdatedAt;

    @Schema(description = "Optional response payload for the client")
    private String payload;

    public SyncOperationResponse() {
    }

    public SyncOperationResponse(
            String operationId,
            String entityType,
            Long entityId,
            String operationType,
            SyncOperationStatus status,
            String message,
            Instant serverUpdatedAt,
            String payload) {
        this.operationId = operationId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.operationType = operationType;
        this.status = status;
        this.message = message;
        this.serverUpdatedAt = serverUpdatedAt;
        this.payload = payload;
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

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public SyncOperationStatus getStatus() {
        return status;
    }

    public void setStatus(SyncOperationStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getServerUpdatedAt() {
        return serverUpdatedAt;
    }

    public void setServerUpdatedAt(Instant serverUpdatedAt) {
        this.serverUpdatedAt = serverUpdatedAt;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
