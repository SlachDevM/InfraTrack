package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import com.infratrack.workorder.WorkOrderService;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.dto.SaveWorkOrderProgressRequest;
import com.infratrack.workorder.dto.WorkOrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderProgressSyncOperationHandlerTest {

    private static final Long USER_ID = 20L;
    private static final Long WORK_ORDER_ID = 456L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T08:30:00Z");

    @Mock
    private WorkOrderService workOrderService;

    private WorkOrderProgressSyncOperationHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        handler = new WorkOrderProgressSyncOperationHandler(workOrderService, objectMapper, clock);
    }

    @Test
    void supports_saveWorkOrderProgressOnWorkOrder_returnsTrue() {
        assertThat(handler.supports(progressOperation())).isTrue();
    }

    @Test
    void supports_unsupportedOperationType_returnsFalse() {
        PendingOperationRequest operation = progressOperation();
        operation.setOperationType("COMPLETE_MAINTENANCE");
        assertThat(handler.supports(operation)).isFalse();
    }

    @Test
    void process_validOperation_returnsAccepted() {
        when(workOrderService.saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new WorkOrderResponse());

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);
        SyncOperationResponse response = result.operation();

        assertThat(result.conflict()).isNull();
        assertThat(response.getOperationId()).isEqualTo("op-wo-1");
        assertThat(response.getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(response.getServerUpdatedAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void process_validOperation_parsesPayloadAndCallsWorkOrderService() {
        when(workOrderService.saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new WorkOrderResponse());

        PendingOperationRequest operation = progressOperation();
        operation.setPayload("""
                {
                  "completionNotes": "Replaced valve gasket."
                }
                """);

        handler.process(operation, USER_ID);

        ArgumentCaptor<SaveWorkOrderProgressRequest> captor = ArgumentCaptor.forClass(SaveWorkOrderProgressRequest.class);
        verify(workOrderService).saveWorkOrderProgress(eq(WORK_ORDER_ID), captor.capture(), eq(USER_ID));
        assertThat(captor.getValue().getCompletionNotes()).isEqualTo("Replaced valve gasket.");
    }

    @Test
    void process_missingEntityId_returnsRejected() {
        PendingOperationRequest operation = progressOperation();
        operation.setEntityId(null);

        SyncOperationHandlerResult result = handler.process(operation, USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo(WorkOrderProgressSyncOperationHandler.INVALID_ENTITY_ID_MESSAGE);
    }

    @Test
    void process_malformedPayload_returnsRejected() {
        PendingOperationRequest operation = progressOperation();
        operation.setPayload("{not-json");

        SyncOperationHandlerResult result = handler.process(operation, USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo(WorkOrderProgressSyncOperationHandler.INVALID_PAYLOAD_MESSAGE);
    }

    @Test
    void process_businessValidationException_returnsRejectedWithMessage() {
        doThrow(new BusinessValidationException("Completion notes must not exceed 4000 characters"))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo("Completion notes must not exceed 4000 characters");
    }

    @Test
    void process_completedWorkOrder_returnsConflictAndConflictEntry() {
        WorkOrderResponse workOrder = mock(WorkOrderResponse.class);
        when(workOrder.getId()).thenReturn(WORK_ORDER_ID);
        when(workOrder.getStatus()).thenReturn(WorkOrderStatus.COMPLETED);
        when(workOrder.getUpdatedAt()).thenReturn(1_700_000_000_000L);
        when(workOrderService.getById(WORK_ORDER_ID)).thenReturn(workOrder);
        doThrow(new ConflictException("Work order is no longer editable."))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.operation().getMessage()).isEqualTo("Work order is no longer editable.");
        assertThat(result.conflict()).isNotNull();
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(result.conflict().getServerState()).isNotNull();
        assertThat(result.conflict().getServerState().getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    void process_notFound_returnsConflictEntityDeleted() {
        doThrow(new NotFoundException("Work order not found"))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.ENTITY_DELETED);
        assertThat(result.conflict().getMessage()).isEqualTo("Work order no longer exists.");
        assertThat(result.conflict().getServerState()).isNull();
    }

    @Test
    void process_cancelledWorkOrder_returnsConflictWorkflowCompleted() {
        WorkOrderResponse workOrder = mock(WorkOrderResponse.class);
        when(workOrder.getId()).thenReturn(WORK_ORDER_ID);
        when(workOrder.getStatus()).thenReturn(WorkOrderStatus.CANCELLED);
        when(workOrder.getUpdatedAt()).thenReturn(1_700_000_000_000L);
        when(workOrderService.getById(WORK_ORDER_ID)).thenReturn(workOrder);
        doThrow(new ConflictException("Work order is no longer editable."))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(result.conflict().getServerState().getStatus()).isEqualTo("CANCELLED");
    }

    @Test
    void process_maintenanceAlreadyExists_returnsConflictWorkflowCompleted() {
        WorkOrderResponse workOrder = mock(WorkOrderResponse.class);
        when(workOrder.getId()).thenReturn(WORK_ORDER_ID);
        when(workOrder.getStatus()).thenReturn(WorkOrderStatus.ASSIGNED);
        when(workOrder.getUpdatedAt()).thenReturn(1_700_000_000_000L);
        when(workOrderService.getById(WORK_ORDER_ID)).thenReturn(workOrder);
        doThrow(new ConflictException("Work order is no longer editable."))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(result.conflict().getClientState().getOperationType()).isEqualTo("SAVE_WORK_ORDER_PROGRESS");
    }

    @Test
    void process_forbidden_returnsConflictPermissionDenied() {
        WorkOrderResponse workOrder = mock(WorkOrderResponse.class);
        when(workOrder.getId()).thenReturn(WORK_ORDER_ID);
        when(workOrder.getStatus()).thenReturn(WorkOrderStatus.ASSIGNED);
        when(workOrderService.getById(WORK_ORDER_ID)).thenReturn(workOrder);
        doThrow(new ForbiddenOperationException(
                "Only the assigned worker may save maintenance progress for this work order"))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.PERMISSION_DENIED);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.MANUAL_REVIEW);
    }

    @Test
    void process_unexpectedException_returnsRejectedWithGenericMessage() {
        doThrow(new IllegalStateException("boom"))
                .when(workOrderService)
                .saveWorkOrderProgress(eq(WORK_ORDER_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo(WorkOrderProgressSyncOperationHandler.GENERIC_FAILURE_MESSAGE);
    }

    private PendingOperationRequest progressOperation() {
        PendingOperationRequest operation = new PendingOperationRequest();
        operation.setOperationId("op-wo-1");
        operation.setEntityType("WORK_ORDER");
        operation.setEntityId(WORK_ORDER_ID);
        operation.setOperationType("SAVE_WORK_ORDER_PROGRESS");
        operation.setPayload("{\"completionNotes\":\"Draft notes.\"}");
        return operation;
    }
}
