package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import com.infratrack.workorder.WorkOrderService;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.dto.WorkOrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SyncConflictEnrichmentTest {

    private static final Long INSPECTION_ID = 42L;

    @Mock
    private InspectionService inspectionService;

    @Mock
    private WorkOrderService workOrderService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void buildServerState_workflowCompleted_containsInspectionSnapshot() {
        InspectionResponse inspection = inspectionResponse(InspectionStatus.COMPLETED);
        when(inspectionService.getByIdForConflictSnapshot(INSPECTION_ID)).thenReturn(inspection);

        var serverState = SyncConflictEnrichment.buildServerState(
                inspectionService, "INSPECTION", INSPECTION_ID, SyncConflictType.WORKFLOW_COMPLETED);

        assertThat(serverState).isNotNull();
        assertThat(serverState.getEntityId()).isEqualTo(INSPECTION_ID);
        assertThat(serverState.getEntityType()).isEqualTo("INSPECTION");
        assertThat(serverState.getStatus()).isEqualTo("COMPLETED");
        assertThat(serverState.getUpdatedAt()).isEqualTo(1_700_000_000_000L);
        assertThat(serverState.getCompletedAt()).isEqualTo(LocalDateTime.parse("2026-07-01T10:00:00"));
        assertThat(serverState.getAssignedTo()).isEqualTo(20L);
        assertThat(serverState.getAssignedToName()).isEqualTo("Field User");
    }

    @Test
    void buildServerState_workOrderWorkflowCompleted_containsWorkOrderSnapshot() {
        WorkOrderResponse workOrder = workOrderResponse(WorkOrderStatus.COMPLETED);
        when(workOrderService.getByIdForConflictSnapshot(88L)).thenReturn(workOrder);

        var serverState = SyncConflictEnrichment.buildServerState(
                null, workOrderService, "WORK_ORDER", 88L, SyncConflictType.WORKFLOW_COMPLETED);

        assertThat(serverState).isNotNull();
        assertThat(serverState.getEntityId()).isEqualTo(88L);
        assertThat(serverState.getEntityType()).isEqualTo("WORK_ORDER");
        assertThat(serverState.getStatus()).isEqualTo("COMPLETED");
        assertThat(serverState.getAssignedTo()).isEqualTo(20L);
        assertThat(serverState.getAssignedToName()).isEqualTo("Field User");
        assertThat(serverState.getUpdatedAt()).isEqualTo(1_700_000_000_000L);
    }

    @Test
    void buildClientState_workOrderProgress_mirrorsSubmittedPayload() throws Exception {
        PendingOperationRequest operation = new PendingOperationRequest();
        operation.setOperationType("SAVE_WORK_ORDER_PROGRESS");
        operation.setCreatedAt(1_751_700_001_000L);
        operation.setPayload("{\"completionNotes\":\"Draft gasket notes.\"}");

        var clientState = SyncConflictEnrichment.buildClientState(operation, objectMapper);

        assertThat(clientState.getOperationType()).isEqualTo("SAVE_WORK_ORDER_PROGRESS");
        assertThat(clientState.getCreatedAt()).isEqualTo(1_751_700_001_000L);
        assertThat(clientState.getPayload().get("completionNotes").asText()).isEqualTo("Draft gasket notes.");
    }

    @Test
    void buildServerState_entityDeleted_returnsNullWithoutQuery() {
        var serverState = SyncConflictEnrichment.buildServerState(
                inspectionService, "INSPECTION", INSPECTION_ID, SyncConflictType.ENTITY_DELETED);

        assertThat(serverState).isNull();
    }

    @Test
    void buildClientState_mirrorsSubmittedPayload() throws Exception {
        PendingOperationRequest operation = new PendingOperationRequest();
        operation.setOperationType("SAVE_INSPECTION_PROGRESS");
        operation.setCreatedAt(1_751_700_001_000L);
        operation.setPayload("{\"answers\":[{\"questionId\":10,\"booleanValue\":true}]}");

        var clientState = SyncConflictEnrichment.buildClientState(operation, objectMapper);

        assertThat(clientState.getOperationType()).isEqualTo("SAVE_INSPECTION_PROGRESS");
        assertThat(clientState.getCreatedAt()).isEqualTo(1_751_700_001_000L);
        assertThat(clientState.getPayload().get("answers").isArray()).isTrue();
        assertThat(clientState.getPayload().get("answers").get(0).get("questionId").asInt()).isEqualTo(10);
    }

    @Test
    void classifier_resolutionHint_mapsConservatively() {
        assertThat(SyncConflictClassifier.resolutionHint(SyncConflictType.WORKFLOW_COMPLETED))
                .isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(SyncConflictClassifier.resolutionHint(SyncConflictType.ENTITY_DELETED))
                .isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(SyncConflictClassifier.resolutionHint(SyncConflictType.PERMISSION_DENIED))
                .isEqualTo(SyncResolutionHint.MANUAL_REVIEW);
        assertThat(SyncConflictClassifier.resolutionHint(SyncConflictType.VERSION_MISMATCH))
                .isEqualTo(SyncResolutionHint.CLIENT_RETRY);
        assertThat(SyncConflictClassifier.resolutionHint(SyncConflictType.UNKNOWN))
                .isEqualTo(SyncResolutionHint.UNKNOWN);
    }

    @Test
    void buildServerState_missingInspection_returnsNull() {
        when(inspectionService.getByIdForConflictSnapshot(INSPECTION_ID))
                .thenThrow(new NotFoundException("Inspection not found"));

        var serverState = SyncConflictEnrichment.buildServerState(
                inspectionService, "INSPECTION", INSPECTION_ID, SyncConflictType.UNKNOWN);

        assertThat(serverState).isNull();
    }

    private static InspectionResponse inspectionResponse(InspectionStatus status) {
        InspectionResponse response = mock(InspectionResponse.class);
        when(response.getId()).thenReturn(INSPECTION_ID);
        when(response.getStatus()).thenReturn(status);
        when(response.getUpdatedAt()).thenReturn(1_700_000_000_000L);
        when(response.getCompletedAt()).thenReturn(LocalDateTime.parse("2026-07-01T10:00:00"));
        when(response.getAssignedToUserId()).thenReturn(20L);
        when(response.getAssignedToUserName()).thenReturn("Field User");
        return response;
    }

    private static WorkOrderResponse workOrderResponse(WorkOrderStatus status) {
        WorkOrderResponse response = mock(WorkOrderResponse.class);
        when(response.getId()).thenReturn(88L);
        when(response.getStatus()).thenReturn(status);
        when(response.getUpdatedAt()).thenReturn(1_700_000_000_000L);
        when(response.getAssignedToUserId()).thenReturn(20L);
        when(response.getAssignedToUserName()).thenReturn("Field User");
        return response;
    }
}
