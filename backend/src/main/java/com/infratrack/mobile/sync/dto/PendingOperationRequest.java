package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public class PendingOperationRequest {

    @NotBlank
    @Schema(description = "Client-generated idempotency key (UUID)", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String operationId;

    @NotBlank
    @Schema(description = "Target entity type", example = "INSPECTION")
    private String entityType;

    @Schema(description = "Target entity identifier; optional for future create operations")
    private Long entityId;

    @NotBlank
    @Schema(description = "Operation type", example = "SAVE_INSPECTION_PROGRESS")
    private String operationType;

    @Schema(description = "Opaque JSON payload matching the future write DTO shape")
    private String payload;

    @Schema(description = "Client creation time (epoch millis); optional")
    private Long createdAt;

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

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }
}
