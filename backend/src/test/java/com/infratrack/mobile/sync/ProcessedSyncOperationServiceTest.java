package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedSyncOperationServiceTest {

    private static final Long USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T10:00:00Z");

    @Mock
    private ProcessedSyncOperationRepository repository;

    private SimpleMeterRegistry meterRegistry;
    private ProcessedSyncOperationService service;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        meterRegistry = new SimpleMeterRegistry();
        service = new ProcessedSyncOperationService(
                repository,
                clock,
                new SyncMetricsRecorder(meterRegistry));
    }

    @Test
    void processIdempotently_firstExecution_recordsOperation() {
        when(repository.findById("op-1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SyncOperationHandlerResult result = service.processIdempotently(
                USER_ID,
                operationRequest(),
                () -> SyncOperationHandlerResult.withoutConflict(acceptedResponse()));

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        ArgumentCaptor<ProcessedSyncOperation> captor = ArgumentCaptor.forClass(ProcessedSyncOperation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getOperationId()).isEqualTo("op-1");
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getResponseStatus()).isEqualTo("ACCEPTED");
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isZero();
    }

    @Test
    void processIdempotently_duplicateExecution_skipsExecutorAndReturnsStoredResponse() {
        ProcessedSyncOperation stored = new ProcessedSyncOperation(
                "op-1",
                USER_ID,
                "INSPECTION",
                123L,
                "SAVE_INSPECTION_PROGRESS",
                SyncProtocolVersion.CURRENT,
                FIXED_INSTANT,
                SyncOperationStatus.CONFLICT.name(),
                "Inspection is no longer editable.",
                null,
                SyncConflictType.WORKFLOW_COMPLETED.name(),
                "Inspection is no longer editable.");
        when(repository.findById("op-1")).thenReturn(Optional.of(stored));

        SyncOperationHandlerResult result = service.processIdempotently(
                USER_ID,
                operationRequest(),
                () -> {
                    throw new AssertionError("Executor must not run for duplicate operationId");
                });

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict()).isNotNull();
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        verify(repository, never()).save(any());
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
    }

    @Test
    void processIdempotently_runtimeFailure_doesNotRecordOperation() {
        when(repository.findById("op-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.processIdempotently(
                        USER_ID,
                        operationRequest(),
                        () -> {
                            throw new RuntimeException("boom");
                        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        verify(repository, never()).save(any());
    }

    @Test
    void processIdempotently_businessRejection_recordsRejectedOutcome() {
        when(repository.findById("op-1")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        SyncOperationResponse rejected = acceptedResponse();
        rejected.setStatus(SyncOperationStatus.REJECTED);
        rejected.setMessage("Question ID is required for each answer");

        service.processIdempotently(
                USER_ID,
                operationRequest(),
                () -> SyncOperationHandlerResult.withoutConflict(rejected));

        ArgumentCaptor<ProcessedSyncOperation> captor = ArgumentCaptor.forClass(ProcessedSyncOperation.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getResponseStatus()).isEqualTo("REJECTED");
        assertThat(captor.getValue().getResponseMessage()).isEqualTo("Question ID is required for each answer");
    }

    @Test
    void purgeExpired_delegatesToRepository() {
        Instant cutoff = FIXED_INSTANT.minusSeconds(3600);
        when(repository.deleteByProcessedAtBefore(cutoff)).thenReturn(3);

        assertThat(service.purgeExpired(cutoff)).isEqualTo(3);
    }

    private static PendingOperationRequest operationRequest() {
        PendingOperationRequest request = new PendingOperationRequest();
        request.setOperationId("op-1");
        request.setEntityType("INSPECTION");
        request.setEntityId(123L);
        request.setOperationType("SAVE_INSPECTION_PROGRESS");
        request.setPayload("{\"answers\":[]}");
        return request;
    }

    private static SyncOperationResponse acceptedResponse() {
        SyncOperationResponse response = new SyncOperationResponse();
        response.setOperationId("op-1");
        response.setEntityType("INSPECTION");
        response.setEntityId(123L);
        response.setOperationType("SAVE_INSPECTION_PROGRESS");
        response.setStatus(SyncOperationStatus.ACCEPTED);
        response.setServerUpdatedAt(FIXED_INSTANT);
        return response;
    }
}
