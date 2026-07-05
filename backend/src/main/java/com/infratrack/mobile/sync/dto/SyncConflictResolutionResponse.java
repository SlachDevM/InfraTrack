package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

public class SyncConflictResolutionResponse {

    private String operationId;
    private String entityType;
    private Long entityId;
    private String operationType;
    private SyncConflictResolutionAction resolution;
    private SyncConflictResolutionStatus status;
    private String message;

    @Schema(description = "Server time when resolution outcome was recorded")
    private Instant serverTime;

    public SyncConflictResolutionResponse() {
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

    public SyncConflictResolutionAction getResolution() {
        return resolution;
    }

    public void setResolution(SyncConflictResolutionAction resolution) {
        this.resolution = resolution;
    }

    public SyncConflictResolutionStatus getStatus() {
        return status;
    }

    public void setStatus(SyncConflictResolutionStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Instant getServerTime() {
        return serverTime;
    }

    public void setServerTime(Instant serverTime) {
        this.serverTime = serverTime;
    }
}
