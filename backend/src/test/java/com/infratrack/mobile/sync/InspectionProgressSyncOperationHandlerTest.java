package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.inspection.dto.SaveInspectionProgressRequest;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
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
class InspectionProgressSyncOperationHandlerTest {

    private static final Long USER_ID = 20L;
    private static final Long INSPECTION_ID = 123L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T08:30:00Z");

    @Mock
    private InspectionService inspectionService;

    private InspectionProgressSyncOperationHandler handler;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        handler = new InspectionProgressSyncOperationHandler(inspectionService, objectMapper, clock);
    }

    @Test
    void supports_saveInspectionProgressOnInspection_returnsTrue() {
        assertThat(handler.supports(progressOperation())).isTrue();
    }

    @Test
    void supports_unsupportedOperationType_returnsFalse() {
        PendingOperationRequest operation = progressOperation();
        operation.setOperationType("COMPLETE_INSPECTION");
        assertThat(handler.supports(operation)).isFalse();
    }

    @Test
    void process_validOperation_returnsAccepted() {
        when(inspectionService.saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new InspectionResponse());

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);
        SyncOperationResponse response = result.operation();

        assertThat(result.conflict()).isNull();
        assertThat(response.getOperationId()).isEqualTo("op-1");
        assertThat(response.getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(response.getServerUpdatedAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void process_validOperation_parsesPayloadAndCallsInspectionService() {
        when(inspectionService.saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID)))
                .thenReturn(new InspectionResponse());

        PendingOperationRequest operation = progressOperation();
        operation.setPayload("""
                {
                  "observedCondition": "GOOD",
                  "observations": "Checked on site.",
                  "issueIdentified": false,
                  "answers": [
                    {
                      "questionId": 10,
                      "booleanValue": true
                    }
                  ]
                }
                """);

        handler.process(operation, USER_ID);

        ArgumentCaptor<SaveInspectionProgressRequest> captor = ArgumentCaptor.forClass(SaveInspectionProgressRequest.class);
        verify(inspectionService).saveInspectionProgress(eq(INSPECTION_ID), captor.capture(), eq(USER_ID));
        SaveInspectionProgressRequest request = captor.getValue();
        assertThat(request.getObservations()).isEqualTo("Checked on site.");
        assertThat(request.getAnswers()).hasSize(1);
    }

    @Test
    void process_missingEntityId_returnsRejected() {
        PendingOperationRequest operation = progressOperation();
        operation.setEntityId(null);

        SyncOperationHandlerResult result = handler.process(operation, USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo(InspectionProgressSyncOperationHandler.INVALID_ENTITY_ID_MESSAGE);
    }

    @Test
    void process_malformedPayload_returnsRejected() {
        PendingOperationRequest operation = progressOperation();
        operation.setPayload("{not-json");

        SyncOperationHandlerResult result = handler.process(operation, USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo(InspectionProgressSyncOperationHandler.INVALID_PAYLOAD_MESSAGE);
    }

    @Test
    void process_businessValidationException_returnsRejectedWithMessage() {
        doThrow(new BusinessValidationException("Question ID is required for each answer"))
                .when(inspectionService)
                .saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo("Question ID is required for each answer");
    }

    @Test
    void process_completedInspection_returnsConflictAndConflictEntry() {
        InspectionResponse inspection = mock(InspectionResponse.class);
        when(inspection.getId()).thenReturn(INSPECTION_ID);
        when(inspection.getStatus()).thenReturn(InspectionStatus.COMPLETED);
        when(inspection.getUpdatedAt()).thenReturn(1_700_000_000_000L);
        when(inspectionService.getById(INSPECTION_ID)).thenReturn(inspection);
        doThrow(new ConflictException("Inspection progress cannot be modified after completion"))
                .when(inspectionService)
                .saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.operation().getMessage()).isEqualTo("Inspection is no longer editable.");
        assertThat(result.conflict()).isNotNull();
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.WORKFLOW_COMPLETED);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(result.conflict().getOperationId()).isEqualTo("op-1");
        assertThat(result.conflict().getEntityId()).isEqualTo(INSPECTION_ID);
        assertThat(result.conflict().getServerState()).isNotNull();
        assertThat(result.conflict().getServerState().getStatus()).isEqualTo(InspectionStatus.COMPLETED);
        assertThat(result.conflict().getClientState().getOperationType())
                .isEqualTo("SAVE_INSPECTION_PROGRESS");
        assertThat(result.conflict().getClientState().getPayload()).isNotNull();
    }

    @Test
    void process_notFound_returnsConflictEntityDeleted() {
        doThrow(new NotFoundException("Inspection not found"))
                .when(inspectionService)
                .saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.ENTITY_DELETED);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(result.conflict().getServerState()).isNull();
    }

    @Test
    void process_forbidden_returnsConflictPermissionDenied() {
        InspectionResponse inspection = mock(InspectionResponse.class);
        when(inspection.getId()).thenReturn(INSPECTION_ID);
        when(inspection.getStatus()).thenReturn(InspectionStatus.ASSIGNED);
        when(inspectionService.getById(INSPECTION_ID)).thenReturn(inspection);
        doThrow(new ForbiddenOperationException("Only the assigned user can save inspection answers"))
                .when(inspectionService)
                .saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.PERMISSION_DENIED);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.MANUAL_REVIEW);
        assertThat(result.conflict().getMessage()).isEqualTo("Only the assigned user can save inspection answers");
    }

    @Test
    void process_versionMismatch_returnsClientRetryHint() {
        InspectionResponse inspection = mock(InspectionResponse.class);
        when(inspection.getId()).thenReturn(INSPECTION_ID);
        when(inspection.getStatus()).thenReturn(InspectionStatus.ASSIGNED);
        when(inspectionService.getById(INSPECTION_ID)).thenReturn(inspection);
        doThrow(new ConflictException("Duplicate answer submitted for the same checklist question"))
                .when(inspectionService)
                .saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(result.conflict().getConflictType()).isEqualTo(SyncConflictType.VERSION_MISMATCH);
        assertThat(result.conflict().getResolutionHint()).isEqualTo(SyncResolutionHint.CLIENT_RETRY);
    }

    @Test
    void process_unexpectedException_returnsRejectedWithGenericMessage() {
        doThrow(new IllegalStateException("boom"))
                .when(inspectionService)
                .saveInspectionProgress(eq(INSPECTION_ID), org.mockito.ArgumentMatchers.any(), eq(USER_ID));

        SyncOperationHandlerResult result = handler.process(progressOperation(), USER_ID);

        assertThat(result.conflict()).isNull();
        assertThat(result.operation().getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(result.operation().getMessage()).isEqualTo(InspectionProgressSyncOperationHandler.GENERIC_FAILURE_MESSAGE);
    }

    private PendingOperationRequest progressOperation() {
        PendingOperationRequest operation = new PendingOperationRequest();
        operation.setOperationId("op-1");
        operation.setEntityType("INSPECTION");
        operation.setEntityId(INSPECTION_ID);
        operation.setOperationType("SAVE_INSPECTION_PROGRESS");
        operation.setPayload("{\"answers\":[]}");
        return operation;
    }
}
