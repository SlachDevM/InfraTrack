package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultSyncOperationProcessorTest {

    private static final Long USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T08:30:00Z");

    @Mock
    private InspectionService inspectionService;

    private DefaultSyncOperationProcessor processor;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        InspectionProgressSyncOperationHandler handler = new InspectionProgressSyncOperationHandler(
                inspectionService,
                new ObjectMapper(),
                clock);
        processor = new DefaultSyncOperationProcessor(
                List.of(handler),
                SyncTestIdempotencySupport.passthroughService(clock));
    }

    @Test
    void process_emptyPendingOperations_returnsEmptyBatch() {
        assertThat(processor.process(USER_ID, List.of()).operations()).isEmpty();
        assertThat(processor.process(USER_ID, List.of()).conflicts()).isEmpty();
        assertThat(processor.process(USER_ID, null).operations()).isEmpty();
    }

    @Test
    void process_validSaveInspectionProgress_returnsAccepted() {
        when(inspectionService.saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new InspectionResponse());

        SyncOperationBatchResult batch = processor.process(USER_ID, List.of(progressOperation("op-1", 123L)));

        assertThat(batch.operations()).hasSize(1);
        assertThat(batch.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(batch.conflicts()).isEmpty();
        verify(inspectionService).saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(USER_ID));
    }

    @Test
    void process_multipleOperations_returnsAcceptedConflictAndRejectedTogether() {
        when(inspectionService.saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new InspectionResponse());
        doThrow(new ConflictException("Inspection progress cannot be modified after completion"))
                .when(inspectionService)
                .saveInspectionProgress(eq(456L), org.mockito.ArgumentMatchers.any(), eq(USER_ID));
        InspectionResponse completedInspection = mock(InspectionResponse.class);
        when(completedInspection.getId()).thenReturn(456L);
        when(completedInspection.getStatus()).thenReturn(InspectionStatus.COMPLETED);
        when(inspectionService.getById(456L)).thenReturn(completedInspection);
        doThrow(new BusinessValidationException("Question ID is required for each answer"))
                .when(inspectionService)
                .saveInspectionProgress(eq(999L), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        PendingOperationRequest accepted = progressOperation("op-accepted", 123L);
        PendingOperationRequest conflict = progressOperation("op-conflict", 456L);
        PendingOperationRequest rejected = progressOperation("op-rejected", 999L);

        SyncOperationBatchResult batch = processor.process(USER_ID, List.of(accepted, conflict, rejected));

        assertThat(batch.operations()).hasSize(3);
        assertThat(batch.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(batch.operations().get(1).getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(batch.operations().get(2).getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(batch.conflicts()).hasSize(1);
        assertThat(batch.conflicts().get(0).getOperationId()).isEqualTo("op-conflict");
        assertThat(batch.conflicts().get(0).getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(batch.conflicts().get(0).getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(batch.conflicts().get(0).getServerState()).isNotNull();
        assertThat(batch.conflicts().get(0).getClientState()).isNotNull();
    }

    @Test
    void process_multipleOperations_processesIndependently() {
        when(inspectionService.saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new InspectionResponse());
        doThrow(new NotFoundException("Inspection not found"))
                .when(inspectionService)
                .saveInspectionProgress(eq(999L), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        PendingOperationRequest accepted = progressOperation("op-accepted", 123L);
        PendingOperationRequest conflict = progressOperation("op-conflict", 999L);
        PendingOperationRequest ignored = progressOperation("op-ignored", 456L);
        ignored.setOperationType("COMPLETE_INSPECTION");

        SyncOperationBatchResult batch = processor.process(USER_ID, List.of(accepted, conflict, ignored));

        assertThat(batch.operations()).hasSize(3);
        assertThat(batch.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(batch.operations().get(1).getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(batch.operations().get(2).getStatus()).isEqualTo(SyncOperationStatus.IGNORED);
        assertThat(batch.conflicts()).hasSize(1);
    }

    @Test
    void process_unsupportedOperation_returnsIgnored() {
        PendingOperationRequest operation = progressOperation("op-1", 123L);
        operation.setOperationType("COMPLETE_INSPECTION");

        SyncOperationBatchResult batch = processor.process(USER_ID, List.of(operation));

        assertThat(batch.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.IGNORED);
        assertThat(batch.conflicts()).isEmpty();
    }

    @Test
    void process_oversizedPayload_returnsRejected() {
        PendingOperationRequest operation = progressOperation("op-large", 123L);
        operation.setPayload(oversizedPayload());

        SyncOperationBatchResult batch = processor.process(USER_ID, List.of(operation));

        assertThat(batch.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(batch.operations().get(0).getMessage()).isEqualTo(SyncLimits.PAYLOAD_SIZE_MESSAGE);
        assertThat(batch.conflicts()).isEmpty();
    }

    @Test
    void process_duplicateOperationId_returnsStoredResponseWithoutCallingHandler() {
        ProcessedSyncOperationRepository repository = org.mockito.Mockito.mock(ProcessedSyncOperationRepository.class);
        java.util.Map<String, ProcessedSyncOperation> store = new java.util.HashMap<>();
        when(repository.findById(org.mockito.ArgumentMatchers.any()))
                .thenAnswer(invocation -> java.util.Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(repository.save(org.mockito.ArgumentMatchers.any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });

        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        io.micrometer.core.instrument.simple.SimpleMeterRegistry meterRegistry =
                new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        ProcessedSyncOperationService idempotencyService = new ProcessedSyncOperationService(
                repository,
                clock,
                new SyncMetricsRecorder(meterRegistry));
        InspectionProgressSyncOperationHandler handler = new InspectionProgressSyncOperationHandler(
                inspectionService,
                new ObjectMapper(),
                clock);
        DefaultSyncOperationProcessor idempotentProcessor =
                new DefaultSyncOperationProcessor(List.of(handler), idempotencyService);

        when(inspectionService.saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new InspectionResponse());

        PendingOperationRequest operation = progressOperation("op-dup", 123L);
        idempotentProcessor.process(USER_ID, List.of(operation));
        SyncOperationBatchResult second = idempotentProcessor.process(USER_ID, List.of(operation));

        verify(inspectionService, times(1))
                .saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(USER_ID));
        assertThat(second.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
    }

    private static String oversizedPayload() {
        byte[] bytes = new byte[SyncLimits.MAX_OPERATION_PAYLOAD_BYTES + 1];
        return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
    }

    private PendingOperationRequest progressOperation(String operationId, Long inspectionId) {
        PendingOperationRequest operation = new PendingOperationRequest();
        operation.setOperationId(operationId);
        operation.setEntityType("INSPECTION");
        operation.setEntityId(inspectionId);
        operation.setOperationType("SAVE_INSPECTION_PROGRESS");
        operation.setPayload("{\"answers\":[]}");
        return operation;
    }
}
