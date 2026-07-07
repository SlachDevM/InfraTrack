package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictClientState;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import com.infratrack.mobile.sync.dto.SyncConflictServerState;
import com.infratrack.mobile.sync.dto.SyncConflictType;
import com.infratrack.mobile.sync.dto.SyncResolutionHint;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncWorkOrderDeltaResponse;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SyncDtoSerializationTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void syncRequest_deserializesExpectedShape() throws Exception {
        String json = """
                {
                  "clientId": "android-install-uuid",
                  "clientVersion": "1",
                  "appVersion": "1.1.0",
                  "syncToken": "cursor-abc",
                  "deviceTime": 1751700000000,
                  "pendingOperations": [
                    {
                      "operationId": "op-1",
                      "entityType": "INSPECTION",
                      "entityId": 42,
                      "operationType": "SAVE_INSPECTION_PROGRESS",
                      "payload": "{\\"answers\\":[]}",
                      "createdAt": 1751700001000
                    }
                  ]
                }
                """;

        SyncRequest request = objectMapper.readValue(json, SyncRequest.class);

        assertThat(request.getClientId()).isEqualTo("android-install-uuid");
        assertThat(request.getClientVersion()).isEqualTo("1");
        assertThat(request.getAppVersion()).isEqualTo("1.1.0");
        assertThat(request.getSyncToken()).isEqualTo("cursor-abc");
        assertThat(request.getDeviceTime()).isEqualTo(1751700000000L);
        assertThat(request.getPendingOperations()).hasSize(1);
        assertThat(request.getPendingOperations().get(0).getOperationId()).isEqualTo("op-1");
    }

    @Test
    void syncResponse_roundTripsExpectedShape() throws Exception {
        SyncResponse response = new SyncResponse();
        response.setProtocolVersion(SyncProtocolVersion.CURRENT);
        response.setServerTime(Instant.parse("2026-07-05T08:30:00Z"));
        response.setNextSyncToken(SyncToken.issue(Instant.parse("2026-07-05T08:30:00Z")).toOpaqueValue());
        response.setDelta(SyncDeltaResponse.empty());
        response.setRequiresFullSync(false);

        String json = objectMapper.writeValueAsString(response);
        SyncResponse roundTripped = objectMapper.readValue(json, SyncResponse.class);

        assertThat(roundTripped.getProtocolVersion()).isEqualTo(SyncProtocolVersion.CURRENT);
        assertThat(roundTripped.getServerTime()).isEqualTo(Instant.parse("2026-07-05T08:30:00Z"));
        assertThat(roundTripped.getNextSyncToken()).isNotBlank();
        assertThat(roundTripped.getDelta().getAssets()).isEmpty();
        assertThat(roundTripped.getDelta().getInspections()).isEmpty();
        assertThat(roundTripped.getDelta().getWorkOrders()).isEmpty();
        assertThat(roundTripped.getDelta().getDocuments()).isEmpty();
        assertThat(roundTripped.getDelta().getUsers()).isEmpty();
        assertThat(roundTripped.getDelta().getReferenceData()).isEmpty();
        assertThat(roundTripped.getOperations()).isEmpty();
        assertThat(roundTripped.getConflicts()).isEmpty();
        assertThat(roundTripped.getWarnings()).isEmpty();
        assertThat(roundTripped.isRequiresFullSync()).isFalse();
    }

    @Test
    void syncResponse_protocolVersionDefaultsToCurrent() {
        SyncResponse response = new SyncResponse();
        assertThat(response.getProtocolVersion()).isEqualTo(SyncProtocolVersion.CURRENT);
    }

    @Test
    void syncResponse_deltaDefaultsToEmptySections() {
        SyncResponse response = new SyncResponse();
        assertThat(response.getDelta().getAssets()).isEmpty();
        assertThat(response.getDelta().getInspections()).isEmpty();
        assertThat(response.getDelta().getWorkOrders()).isEmpty();
        assertThat(response.getDelta().getDocuments()).isEmpty();
        assertThat(response.getDelta().getUsers()).isEmpty();
        assertThat(response.getDelta().getReferenceData()).isEmpty();
    }

    @Test
    void enumFields_serializeAsStringNames() throws Exception {
        SyncOperationResponse operation = new SyncOperationResponse();
        operation.setOperationId("op-1");
        operation.setStatus(SyncOperationStatus.ACCEPTED);

        SyncConflictResponse conflict = new SyncConflictResponse();
        conflict.setOperationId("op-1");
        conflict.setConflictType(SyncConflictType.ENTITY_MODIFIED);

        SyncWarningResponse warning = new SyncWarningResponse(SyncWarningCode.SYNC_TOKEN_EXPIRED, "Token expired");

        assertThat(objectMapper.writeValueAsString(operation)).contains("\"status\":\"ACCEPTED\"");
        assertThat(objectMapper.writeValueAsString(conflict)).contains("\"conflictType\":\"ENTITY_MODIFIED\"");
        assertThat(objectMapper.writeValueAsString(warning)).contains("\"code\":\"SYNC_TOKEN_EXPIRED\"");
    }

    @Test
    void syncConflictResponse_serializesEnrichedFields() throws Exception {
        SyncConflictClientState clientState = new SyncConflictClientState();
        clientState.setOperationType("SAVE_INSPECTION_PROGRESS");
        clientState.setCreatedAt(1_751_700_001_000L);
        clientState.setPayload(objectMapper.readTree("{\"answers\":[]}"));

        SyncConflictServerState serverState = new SyncConflictServerState();
        serverState.setEntityId(42L);
        serverState.setEntityType("INSPECTION");
        serverState.setStatus(com.infratrack.inspection.InspectionStatus.COMPLETED.name());

        SyncConflictResponse conflict = new SyncConflictResponse();
        conflict.setOperationId("op-1");
        conflict.setEntityId(42L);
        conflict.setConflictType(SyncConflictType.WORKFLOW_COMPLETED);
        conflict.setResolutionHint(SyncResolutionHint.SERVER_WINS);
        conflict.setMessage("Inspection is no longer editable.");
        conflict.setClientState(clientState);
        conflict.setServerState(serverState);

        String json = objectMapper.writeValueAsString(conflict);

        assertThat(json).contains("\"resolutionHint\":\"SERVER_WINS\"");
        assertThat(json).contains("\"serverState\"");
        assertThat(json).contains("\"clientState\"");
        assertThat(json).contains("\"operationType\":\"SAVE_INSPECTION_PROGRESS\"");
        assertThat(json).contains("\"status\":\"COMPLETED\"");

        SyncConflictResponse roundTripped = objectMapper.readValue(json, SyncConflictResponse.class);
        assertThat(roundTripped.getResolutionHint()).isEqualTo(SyncResolutionHint.SERVER_WINS);
        assertThat(roundTripped.getServerState().getEntityId()).isEqualTo(42L);
        assertThat(roundTripped.getClientState().getPayload().get("answers").isArray()).isTrue();
    }

    @Test
    void syncWorkOrderDeltaResponse_serializesExpectedFields() throws Exception {
        SyncWorkOrderDeltaResponse workOrder = new SyncWorkOrderDeltaResponse();
        workOrder.setWorkOrderId(500L);
        workOrder.setStatus(WorkOrderStatus.ASSIGNED);
        workOrder.setPriority(WorkOrderPriority.HIGH);
        workOrder.setWorkType(WorkType.INTERNAL_MAINTENANCE);
        workOrder.setDescription("Fix swing");
        workOrder.setAssetId(50L);
        workOrder.setAssetName("Central Playground");
        workOrder.setAssetCategoryName("Playground");
        workOrder.setAssignedTo(20L);
        workOrder.setAssignedToName("Field User");
        workOrder.setCreatedAt(1_751_700_000_000L);
        workOrder.setUpdatedAt(1_751_700_100_000L);
        workOrder.setDraftCompletionNotes("Offline draft");
        workOrder.setCompletionEligible(true);
        workOrder.setOperationalDecisionId(200L);

        SyncDeltaResponse delta = SyncDeltaResponse.empty();
        delta.setWorkOrders(List.of(workOrder));

        String json = objectMapper.writeValueAsString(delta);

        assertThat(json).contains("\"workOrderId\":500");
        assertThat(json).contains("\"draftCompletionNotes\":\"Offline draft\"");
        assertThat(json).contains("\"completionEligible\":true");
        assertThat(json).contains("\"operationalDecisionId\":200");

        SyncDeltaResponse roundTripped = objectMapper.readValue(json, SyncDeltaResponse.class);
        assertThat(roundTripped.getWorkOrders()).hasSize(1);
        assertThat(roundTripped.getWorkOrders().get(0).getWorkOrderId()).isEqualTo(500L);
        assertThat(roundTripped.getWorkOrders().get(0).getDraftCompletionNotes()).isEqualTo("Offline draft");
    }

    @Test
    void syncRequest_pendingOperationsDefaultsToEmptyList() {
        SyncRequest request = new SyncRequest();
        assertThat(request.getPendingOperations()).isEmpty();
    }
}
