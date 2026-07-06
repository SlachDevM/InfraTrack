package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncOperationIdempotencyIntegrationTest {

    private static final Long USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T10:00:00Z");

    @Mock
    private ProcessedSyncOperationRepository repository;

    @Mock
    private InspectionService inspectionService;

    private DefaultSyncOperationProcessor processor;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        Map<String, ProcessedSyncOperation> store = new HashMap<>();
        when(repository.findById(any())).thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(repository.save(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });

        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        meterRegistry = new SimpleMeterRegistry();
        ProcessedSyncOperationService idempotencyService = new ProcessedSyncOperationService(
                repository,
                clock,
                new SyncMetricsRecorder(meterRegistry));
        InspectionProgressSyncOperationHandler handler = new InspectionProgressSyncOperationHandler(
                inspectionService,
                new ObjectMapper(),
                clock);
        processor = new DefaultSyncOperationProcessor(List.of(handler), idempotencyService);
    }

    @Test
    void duplicateSyncOperation_executesHandlerOnceAndReturnsSameResponse() {
        when(inspectionService.saveInspectionProgress(eq(123L), any(), eq(USER_ID)))
                .thenReturn(new InspectionResponse());

        PendingOperationRequest operation = progressOperation("op-dup-1");
        SyncOperationBatchResult first = processor.process(USER_ID, List.of(operation));
        SyncOperationBatchResult second = processor.process(USER_ID, List.of(operation));

        assertThat(first.operations()).hasSize(1);
        assertThat(second.operations()).hasSize(1);
        assertThat(first.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(second.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(second.operations().get(0).getServerUpdatedAt())
                .isEqualTo(first.operations().get(0).getServerUpdatedAt());

        verify(inspectionService, times(1)).saveInspectionProgress(eq(123L), any(), eq(USER_ID));
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
    }

    private static PendingOperationRequest progressOperation(String operationId) {
        PendingOperationRequest operation = new PendingOperationRequest();
        operation.setOperationId(operationId);
        operation.setEntityType("INSPECTION");
        operation.setEntityId(123L);
        operation.setOperationType("SAVE_INSPECTION_PROGRESS");
        operation.setPayload("{\"answers\":[]}");
        return operation;
    }
}
