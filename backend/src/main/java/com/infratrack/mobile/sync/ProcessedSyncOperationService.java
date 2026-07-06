package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Protocol-level idempotency for mobile sync operations (DT-OFFLINE-1).
 */
@Service
class ProcessedSyncOperationService {

    private static final Logger log = LoggerFactory.getLogger(ProcessedSyncOperationService.class);

    private static final String USER_MISMATCH_MESSAGE =
            "Sync operation id was already processed by another user.";

    private final ProcessedSyncOperationRepository repository;
    private final Clock clock;
    private final SyncMetricsRecorder metricsRecorder;

    ProcessedSyncOperationService(
            ProcessedSyncOperationRepository repository,
            Clock clock,
            SyncMetricsRecorder metricsRecorder) {
        this.repository = repository;
        this.clock = clock;
        this.metricsRecorder = metricsRecorder;
    }

    boolean exists(String operationId) {
        return repository.findById(operationId).isPresent();
    }

    Optional<SyncOperationHandlerResult> getRecordedResponse(String operationId, Long userId) {
        return repository.findById(operationId).map(record -> toHandlerResult(record, userId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    SyncOperationHandlerResult processIdempotently(
            Long userId,
            PendingOperationRequest operation,
            Supplier<SyncOperationHandlerResult> executor) {
        Optional<ProcessedSyncOperation> existing = repository.findById(operation.getOperationId());
        if (existing.isPresent()) {
            SyncOperationHandlerResult recorded = toHandlerResult(existing.get(), userId);
            metricsRecorder.recordDuplicateOperation();
            log.info(
                    "Duplicate sync operation operationId={} user={} ignored=true",
                    operation.getOperationId(),
                    userId);
            return SyncOperationHandlerResult.duplicate(recorded.operation(), recorded.conflict());
        }

        SyncOperationHandlerResult result = executor.get();
        record(userId, operation, result);
        return result;
    }

    @Transactional
    int purgeExpired(Instant cutoff) {
        return repository.deleteByProcessedAtBefore(cutoff);
    }

    private void record(Long userId, PendingOperationRequest operation, SyncOperationHandlerResult result) {
        SyncOperationResponse response = result.operation();
        SyncConflictResponse conflict = result.conflict();
        ProcessedSyncOperation entity = new ProcessedSyncOperation(
                operation.getOperationId(),
                userId,
                operation.getEntityType(),
                operation.getEntityId(),
                operation.getOperationType(),
                SyncProtocolVersion.CURRENT,
                clock.instant(),
                response.getStatus().name(),
                response.getMessage(),
                response.getServerUpdatedAt(),
                conflict != null && conflict.getConflictType() != null
                        ? conflict.getConflictType().name()
                        : null,
                conflict != null ? conflict.getMessage() : null);
        repository.save(entity);
    }

    private SyncOperationHandlerResult toHandlerResult(ProcessedSyncOperation record, Long userId) {
        if (!record.getUserId().equals(userId)) {
            SyncOperationResponse response = new SyncOperationResponse();
            response.setOperationId(record.getOperationId());
            response.setEntityType(record.getEntityType());
            response.setEntityId(record.getEntityId());
            response.setOperationType(record.getOperationType());
            response.setStatus(SyncOperationStatus.REJECTED);
            response.setMessage(USER_MISMATCH_MESSAGE);
            return SyncOperationHandlerResult.withoutConflict(response);
        }

        SyncOperationResponse response = new SyncOperationResponse();
        response.setOperationId(record.getOperationId());
        response.setEntityType(record.getEntityType());
        response.setEntityId(record.getEntityId());
        response.setOperationType(record.getOperationType());
        response.setStatus(SyncOperationStatus.valueOf(record.getResponseStatus()));
        response.setMessage(record.getResponseMessage());
        response.setServerUpdatedAt(record.getServerUpdatedAt());

        if (record.getConflictType() == null) {
            return SyncOperationHandlerResult.withoutConflict(response);
        }

        SyncConflictResponse conflict = new SyncConflictResponse();
        conflict.setOperationId(record.getOperationId());
        conflict.setEntityType(record.getEntityType());
        conflict.setEntityId(record.getEntityId());
        conflict.setConflictType(SyncConflictType.valueOf(record.getConflictType()));
        conflict.setMessage(record.getConflictMessage());
        conflict.setResolutionHint(SyncConflictClassifier.resolutionHint(conflict.getConflictType()));
        return SyncOperationHandlerResult.withConflict(response, conflict);
    }
}
