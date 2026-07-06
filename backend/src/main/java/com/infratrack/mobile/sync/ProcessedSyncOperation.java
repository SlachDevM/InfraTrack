package com.infratrack.mobile.sync;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

    private static final String PROCESSING_STATUS = "PROCESSING";

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

    @Enumerated(EnumType.STRING)
    @Column(name = "record_state", nullable = false, length = 16)
    private ProcessedSyncOperationRecordState recordState;

    protected ProcessedSyncOperation() {
    }

    static ProcessedSyncOperation processing(
            String operationId,
            Long userId,
            String entityType,
            Long entityId,
            String operationType,
            int protocolVersion,
            Instant processedAt) {
        ProcessedSyncOperation entity = new ProcessedSyncOperation();
        entity.operationId = operationId;
        entity.userId = userId;
        entity.entityType = entityType;
        entity.entityId = entityId;
        entity.operationType = operationType;
        entity.protocolVersion = protocolVersion;
        entity.processedAt = processedAt;
        entity.responseStatus = PROCESSING_STATUS;
        entity.recordState = ProcessedSyncOperationRecordState.PROCESSING;
        return entity;
    }

    static ProcessedSyncOperation recorded(
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
        ProcessedSyncOperation entity = new ProcessedSyncOperation();
        entity.operationId = operationId;
        entity.userId = userId;
        entity.entityType = entityType;
        entity.entityId = entityId;
        entity.operationType = operationType;
        entity.protocolVersion = protocolVersion;
        entity.processedAt = processedAt;
        entity.responseStatus = responseStatus;
        entity.responseMessage = responseMessage;
        entity.serverUpdatedAt = serverUpdatedAt;
        entity.conflictType = conflictType;
        entity.conflictMessage = conflictMessage;
        entity.recordState = ProcessedSyncOperationRecordState.RECORDED;
        return entity;
    }

    void finalizeRecord(
            Instant processedAt,
            String responseStatus,
            String responseMessage,
            Instant serverUpdatedAt,
            String conflictType,
            String conflictMessage) {
        this.processedAt = processedAt;
        this.responseStatus = responseStatus;
        this.responseMessage = responseMessage;
        this.serverUpdatedAt = serverUpdatedAt;
        this.conflictType = conflictType;
        this.conflictMessage = conflictMessage;
        this.recordState = ProcessedSyncOperationRecordState.RECORDED;
    }

    boolean isRecorded() {
        return recordState == ProcessedSyncOperationRecordState.RECORDED;
    }

    boolean isProcessing() {
        return recordState == ProcessedSyncOperationRecordState.PROCESSING;
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

    ProcessedSyncOperationRecordState getRecordState() {
        return recordState;
    }
}
