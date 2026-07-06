package com.infratrack.mobile.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Protocol-level idempotency record for a processed mobile sync operation (DT-OFFLINE-1).
 * Stores outcome metadata only — no payload or business entity data.
 */
@Entity
@Table(name = "mobile_sync_operation")
class ProcessedSyncOperation {

    @Id
    @Column(name = "operation_id", nullable = false, length = 128)
    private String operationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "entity_type", nullable = false, length = 64)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "operation_type", nullable = false, length = 128)
    private String operationType;

    @Column(name = "protocol_version", nullable = false)
    private int protocolVersion;

    @Column(name = "processed_at", nullable = false)
    private Instant processedAt;

    @Column(name = "response_status", nullable = false, length = 32)
    private String responseStatus;

    @Column(name = "response_message", length = 1024)
    private String responseMessage;

    @Column(name = "server_updated_at")
    private Instant serverUpdatedAt;

    @Column(name = "conflict_type", length = 64)
    private String conflictType;

    @Column(name = "conflict_message", length = 1024)
    private String conflictMessage;

    protected ProcessedSyncOperation() {
    }

    ProcessedSyncOperation(
            String operationId,
            Long userId,
            String entityType,
            Long entityId,
            String operationType,
            int protocolVersion,
            Instant processedAt,
            String responseStatus,
            String responseMessage,
            Instant serverUpdatedAt,
            String conflictType,
            String conflictMessage) {
        this.operationId = operationId;
        this.userId = userId;
        this.entityType = entityType;
        this.entityId = entityId;
        this.operationType = operationType;
        this.protocolVersion = protocolVersion;
        this.processedAt = processedAt;
        this.responseStatus = responseStatus;
        this.responseMessage = responseMessage;
        this.serverUpdatedAt = serverUpdatedAt;
        this.conflictType = conflictType;
        this.conflictMessage = conflictMessage;
    }

    String getOperationId() {
        return operationId;
    }

    Long getUserId() {
        return userId;
    }

    String getEntityType() {
        return entityType;
    }

    Long getEntityId() {
        return entityId;
    }

    String getOperationType() {
        return operationType;
    }

    int getProtocolVersion() {
        return protocolVersion;
    }

    Instant getProcessedAt() {
        return processedAt;
    }

    String getResponseStatus() {
        return responseStatus;
    }

    String getResponseMessage() {
        return responseMessage;
    }

    Instant getServerUpdatedAt() {
        return serverUpdatedAt;
    }

    String getConflictType() {
        return conflictType;
    }

    String getConflictMessage() {
        return conflictMessage;
    }
}
