package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Protocol-level idempotency for mobile sync operations (DT-OFFLINE-1 / RC-FIX-BE-1).
 */
@Service
class ProcessedSyncOperationService {

    private static final Logger log = LoggerFactory.getLogger(ProcessedSyncOperationService.class);

    private static final String USER_MISMATCH_MESSAGE =
            "Sync operation id was already processed by another user.";
    private static final long AWAIT_RECORDED_TIMEOUT_MILLIS = 5_000L;
    private static final long AWAIT_RECORDED_POLL_MILLIS = 25L;

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
        return repository.findById(operationId)
                .filter(ProcessedSyncOperation::isRecorded)
                .isPresent();
    }

    Optional<SyncOperationHandlerResult> getRecordedResponse(String operationId, Long userId) {
        return repository.findById(operationId)
                .filter(ProcessedSyncOperation::isRecorded)
                .map(record -> toHandlerResult(record, userId));
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    SyncOperationHandlerResult processIdempotently(
            Long userId,
            PendingOperationRequest operation,
            Supplier<SyncOperationHandlerResult> executor) {
        ReservationOutcome reservation = reserve(userId, operation);
        if (reservation.duplicate()) {
            metricsRecorder.recordDuplicateOperation();
            log.info(
                    "Duplicate sync operation operationId={} user={} ignored=true",
                    operation.getOperationId(),
                    userId);
            return reservation.result();
        }

        try {
            SyncOperationHandlerResult result = executor.get();
            finalizeRecord(operation.getOperationId(), operation, result);
            return result;
        } catch (RuntimeException exception) {
            releaseReservation(operation.getOperationId());
            throw exception;
        }
    }

    @Transactional
    int purgeExpired(Instant cutoff) {
        return repository.deleteByProcessedAtBefore(cutoff);
    }

    private ReservationOutcome reserve(Long userId, PendingOperationRequest operation) {
        Optional<ProcessedSyncOperation> existing = repository.findById(operation.getOperationId());
        if (existing.isPresent()) {
            ProcessedSyncOperation record = existing.get();
            if (record.isRecorded()) {
                return ReservationOutcome.duplicate(toHandlerResult(record, userId));
            }
            return ReservationOutcome.duplicate(awaitRecordedResult(operation.getOperationId(), userId));
        }

        ProcessedSyncOperation processing = ProcessedSyncOperation.processing(
                operation.getOperationId(),
                userId,
                operation.getEntityType(),
                operation.getEntityId(),
                operation.getOperationType(),
                SyncProtocolVersion.CURRENT,
                clock.instant());
        try {
            repository.saveAndFlush(processing);
            return ReservationOutcome.reserved();
        } catch (DataIntegrityViolationException exception) {
            ProcessedSyncOperation concurrent = repository.findById(operation.getOperationId())
                    .orElseThrow(() -> exception);
            if (concurrent.isRecorded()) {
                return ReservationOutcome.duplicate(toHandlerResult(concurrent, userId));
            }
            return ReservationOutcome.duplicate(awaitRecordedResult(operation.getOperationId(), userId));
        }
    }

    private SyncOperationHandlerResult awaitRecordedResult(String operationId, Long userId) {
        long deadline = System.currentTimeMillis() + AWAIT_RECORDED_TIMEOUT_MILLIS;
        while (System.currentTimeMillis() < deadline) {
            Optional<ProcessedSyncOperation> record = repository.findById(operationId);
            if (record.isPresent()) {
                if (record.get().isRecorded()) {
                    return toHandlerResult(record.get(), userId);
                }
            } else {
                break;
            }
            sleepBriefly();
        }
        throw new IllegalStateException(
                "Timed out waiting for concurrent sync operation to complete: " + operationId);
    }

    private void finalizeRecord(
            String operationId,
            PendingOperationRequest operation,
            SyncOperationHandlerResult result) {
        ProcessedSyncOperation record = repository.findById(operationId)
                .orElseThrow(() -> new IllegalStateException(
                        "Missing sync operation reservation: " + operationId));
        SyncOperationResponse response = result.operation();
        SyncConflictResponse conflict = result.conflict();
        record.finalizeRecord(
                clock.instant(),
                response.getStatus().name(),
                response.getMessage(),
                response.getServerUpdatedAt(),
                conflict != null && conflict.getConflictType() != null
                        ? conflict.getConflictType().name()
                        : null,
                conflict != null ? conflict.getMessage() : null);
        repository.save(record);
    }

    private void releaseReservation(String operationId) {
        repository.findById(operationId)
                .filter(ProcessedSyncOperation::isProcessing)
                .ifPresent(record -> repository.deleteById(operationId));
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

    private static void sleepBriefly() {
        try {
            Thread.sleep(AWAIT_RECORDED_POLL_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for sync operation result", exception);
        }
    }

    private record ReservationOutcome(boolean duplicate, SyncOperationHandlerResult result) {

        static ReservationOutcome reserved() {
            return new ReservationOutcome(false, null);
        }

        static ReservationOutcome duplicate(SyncOperationHandlerResult result) {
            return new ReservationOutcome(true, result);
        }
    }
}
