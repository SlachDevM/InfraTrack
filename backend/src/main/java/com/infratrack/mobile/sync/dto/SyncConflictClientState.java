package com.infratrack.mobile.sync.dto;

import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Snapshot of the client operation that conflicted (M5.5-BE1.1).
 */
public class SyncConflictClientState {

    @Schema(description = "Queued operation type", example = "SAVE_INSPECTION_PROGRESS")
    private String operationType;

    @Schema(description = "Client creation time (epoch millis) when provided")
    private Long createdAt;

    @Schema(description = "Original operation payload JSON without DTO transformation")
    private JsonNode payload;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public JsonNode getPayload() {
        return payload;
    }

    public void setPayload(JsonNode payload) {
        this.payload = payload;
    }
}
