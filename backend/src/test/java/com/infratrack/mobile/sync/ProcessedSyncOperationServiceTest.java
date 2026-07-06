package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessedSyncOperationServiceTest {

    private static final Long USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T10:00:00Z");

    @Mock
    private ProcessedSyncOperationRepository repository;

    private Map<String, ProcessedSyncOperation> store;
    private SimpleMeterRegistry meterRegistry;
    private ProcessedSyncOperationService service;

    @BeforeEach
    void setUp() {
        store = new HashMap<>();
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        meterRegistry = new SimpleMeterRegistry();
        lenient().when(repository.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(store.get(invocation.getArgument(0))));
        lenient().when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });
        lenient().when(repository.save(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });
        lenient().doAnswer(invocation -> {
            store.remove(invocation.getArgument(0));
            return null;
        }).when(repository).deleteById(any());
        service = new ProcessedSyncOperationService(
                repository,
                clock,
                new SyncMetricsRecorder(meterRegistry));
    }

    @Test
    void processIdempotently_firstExecution_reservesThenRecordsOperation() {
        SyncOperationHandlerResult result = service.processIdempotently(
                USER_ID,
                operationRequest(),
                () -> SyncOperationHandlerResult.withoutConflict(acceptedResponse()));

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        verify(repository).saveAndFlush(any());
        verify(repository).save(any());
        assertThat(store.get("op-1").isRecorded()).isTrue();
        assertThat(store.get("op-1").getResponseStatus()).isEqualTo("ACCEPTED");
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isZero();
    }

    @Test
    void processIdempotently_duplicateExecution_skipsExecutorAndReturnsStoredResponse() {
        ProcessedSyncOperation stored = ProcessedSyncOperation.recorded(
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
        store.put("op-1", stored);

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
        verify(repository, never()).saveAndFlush(any());
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
    }

    @Test
    void processIdempotently_runtimeFailure_releasesReservationForRetry() {
        assertThatThrownBy(() -> service.processIdempotently(
                        USER_ID,
                        operationRequest(),
                        () -> {
                            throw new RuntimeException("boom");
                        }))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("boom");

        verify(repository).deleteById("op-1");
        verify(repository, never()).save(any());
        assertThat(store).isEmpty();
    }

    @Test
    void processIdempotently_concurrentDuplicateExecutesHandlerOnce() throws Exception {
        ProcessedSyncOperationRepository concurrentRepository =
                SyncTestIdempotencySupport.concurrentRepository(store);
        ProcessedSyncOperationService concurrentService = new ProcessedSyncOperationService(
                concurrentRepository,
                Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC),
                new SyncMetricsRecorder(meterRegistry));
        AtomicInteger executions = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<SyncOperationHandlerResult> first = executor.submit(() -> {
                try {
                    start.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                return concurrentService.processIdempotently(
                        USER_ID,
                        operationRequest(),
                        () -> {
                            executions.incrementAndGet();
                            try {
                                Thread.sleep(100);
                            } catch (InterruptedException exception) {
                                Thread.currentThread().interrupt();
                                throw new IllegalStateException(exception);
                            }
                            return SyncOperationHandlerResult.withoutConflict(acceptedResponse());
                        });
            });
            Future<SyncOperationHandlerResult> second = executor.submit(() -> {
                try {
                    start.await();
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                return concurrentService.processIdempotently(
                        USER_ID,
                        operationRequest(),
                        () -> {
                            executions.incrementAndGet();
                            return SyncOperationHandlerResult.withoutConflict(acceptedResponse());
                        });
            });
            start.countDown();

            assertThat(first.get().operation().getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
            assertThat(second.get().operation().getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
            assertThat(executions.get()).isEqualTo(1);
            assertThat(store.get("op-1").isRecorded()).isTrue();
            assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void processIdempotently_businessRejection_recordsRejectedOutcome() {
        SyncOperationResponse rejected = acceptedResponse();
        rejected.setStatus(SyncOperationStatus.REJECTED);
        rejected.setMessage("Question ID is required for each answer");

        service.processIdempotently(
                USER_ID,
                operationRequest(),
                () -> SyncOperationHandlerResult.withoutConflict(rejected));

        ArgumentCaptor<ProcessedSyncOperation> captor = ArgumentCaptor.forClass(ProcessedSyncOperation.class);
        verify(repository, atLeastOnce()).save(captor.capture());
        ProcessedSyncOperation recorded = captor.getAllValues().stream()
                .filter(ProcessedSyncOperation::isRecorded)
                .findFirst()
                .orElseThrow();
        assertThat(recorded.getResponseStatus()).isEqualTo("REJECTED");
        assertThat(recorded.getResponseMessage()).isEqualTo("Question ID is required for each answer");
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
