package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.ConflictException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import com.infratrack.workorder.WorkOrderService;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.dto.WorkOrderResponse;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
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

    @Mock
    private WorkOrderService workOrderService;

    private DefaultSyncOperationProcessor processor;
    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        Map<String, ProcessedSyncOperation> store = new HashMap<>();
        when(repository.findById(any())).thenAnswer(invocation -> Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });
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
        InspectionProgressSyncOperationHandler inspectionHandler = new InspectionProgressSyncOperationHandler(
                inspectionService,
                new ObjectMapper(),
                clock);
        WorkOrderProgressSyncOperationHandler workOrderHandler = new WorkOrderProgressSyncOperationHandler(
                workOrderService,
                new ObjectMapper(),
                clock);
        processor = new DefaultSyncOperationProcessor(
                List.of(inspectionHandler, workOrderHandler), idempotencyService);
    }

    @Test
    void duplicateWorkOrderSyncOperation_executesHandlerOnceAndReturnsSameResponse() {
        when(workOrderService.saveWorkOrderProgress(eq(456L), any(), eq(USER_ID)))
                .thenReturn(new WorkOrderResponse());

        PendingOperationRequest operation = workOrderProgressOperation("op-wo-dup-1");
        SyncOperationBatchResult first = processor.process(USER_ID, List.of(operation));
        SyncOperationBatchResult second = processor.process(USER_ID, List.of(operation));

        assertThat(first.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(second.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);

        verify(workOrderService, times(1)).saveWorkOrderProgress(eq(456L), any(), eq(USER_ID));
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
    }

    @Test
    void duplicateWorkOrderConflictOperation_executesHandlerOnceAndReturnsSameConflict() {
        WorkOrderResponse workOrder = mock(WorkOrderResponse.class);
        when(workOrder.getId()).thenReturn(456L);
        when(workOrder.getStatus()).thenReturn(WorkOrderStatus.COMPLETED);
        when(workOrderService.getById(456L)).thenReturn(workOrder);
        doThrow(new ConflictException("Work order is no longer editable."))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(456L), any(), eq(USER_ID));

        PendingOperationRequest operation = workOrderProgressOperation("op-wo-conflict-dup-1");
        SyncOperationBatchResult first = processor.process(USER_ID, List.of(operation));
        SyncOperationBatchResult second = processor.process(USER_ID, List.of(operation));

        assertThat(first.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(second.operations().get(0).getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(first.conflicts()).hasSize(1);
        assertThat(second.conflicts()).hasSize(1);
        assertThat(first.conflicts().get(0).getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(second.conflicts().get(0).getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(second.conflicts().get(0).getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(second.conflicts().get(0).getMessage()).isEqualTo(first.conflicts().get(0).getMessage());

        verify(workOrderService, times(1)).saveWorkOrderProgress(eq(456L), any(), eq(USER_ID));
        assertThat(meterRegistry.get("mobile.sync.operations.duplicate").counter().count()).isEqualTo(1.0);
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

    private static PendingOperationRequest workOrderProgressOperation(String operationId) {
        PendingOperationRequest operation = new PendingOperationRequest();
        operation.setOperationId(operationId);
        operation.setEntityType("WORK_ORDER");
        operation.setEntityId(456L);
        operation.setOperationType("SAVE_WORK_ORDER_PROGRESS");
        operation.setPayload("{\"completionNotes\":\"Draft notes.\"}");
        return operation;
    }
}
