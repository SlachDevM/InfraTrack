package com.infratrack.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.issue.IssueRepository;
import com.infratrack.mobile.sync.SyncProtocolVersion;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.security.JwtTokenProvider;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end integration tests for {@code POST /api/mobile/sync} (M6.5-STAB-2).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class MobileSyncIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private BusinessTriggerRepository businessTriggerRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private OperationalDecisionRepository operationalDecisionRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MobileSyncIntegrationFixture fixture;

    private User coordinator;
    private User fieldEmployee;
    private Asset asset;
    private Inspection assignedInspection;
    private WorkOrder assignedWorkOrder;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "infratrack-integration-docs"));

        fixture = new MobileSyncIntegrationFixture(
                departmentRepository,
                assetCategoryRepository,
                userRepository,
                assetRepository,
                businessTriggerRepository,
                inspectionRepository,
                issueRepository,
                operationalDecisionRepository,
                workOrderRepository,
                passwordEncoder);

        Department department = fixture.createDepartment("Sync Dept");
        var category = fixture.createCategory();
        coordinator = fixture.createUser("coordinator", "Sync Coordinator", UserRole.OPERATIONAL_COORDINATOR, department);
        fieldEmployee = fixture.createUser("field", "Sync Field Employee", UserRole.FIELD_EMPLOYEE, department);
        asset = fixture.createAsset("Sync Asset", department, category, coordinator.getId());
        BusinessTrigger trigger = fixture.createTrigger(asset, coordinator.getId());
        assignedInspection = fixture.createAssignedInspection(
                asset, trigger, fieldEmployee.getId(), coordinator.getId());
        assignedWorkOrder = fixture.createAssignedWorkOrder(
                asset,
                fieldEmployee.getId(),
                coordinator.getId(),
                WorkType.INTERNAL_MAINTENANCE,
                "Replace worn component");
    }

    @Test
    void firstSync_returnsCompleteDeltaEnvelope() throws Exception {
        JsonNode response = sync(fieldEmployee, syncRequest(null, "[]"));

        assertThat(response.path("protocolVersion").asInt()).isEqualTo(SyncProtocolVersion.CURRENT);
        assertThat(response.path("serverTime").asText()).isNotBlank();
        assertThat(response.path("nextSyncToken").asText()).isNotBlank();
        assertThat(response.path("requiresFullSync").asBoolean()).isFalse();
        assertThat(response.path("delta").path("inspections").isArray()).isTrue();
        assertThat(response.path("delta").path("workOrders").isArray()).isTrue();
        assertThat(response.path("delta").path("dashboard").isObject()).isTrue();
        assertThat(response.path("delta").path("assets").isArray()).isTrue();
        assertThat(response.path("delta").path("referenceData").isObject()).isTrue();
        assertThat(response.path("delta").path("referenceData").path("schemaVersion").asInt()).isEqualTo(1);
        assertThat(MobileSyncIntegrationFixture.deltaContainsInspectionId(response, assignedInspection.getId()))
                .isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(response, assignedWorkOrder.getId()))
                .isTrue();
    }

    @Test
    void mixedPendingOperations_reflectAcceptedChangesInSameResponse() throws Exception {
        String pendingOperations = """
                [
                  {
                    "operationId": "sync-mixed-inspection-1",
                    "entityType": "INSPECTION",
                    "entityId": %d,
                    "operationType": "SAVE_INSPECTION_PROGRESS",
                    "payload": "{\\"observations\\":\\"Checked during mixed sync.\\",\\"answers\\":[]}"
                  },
                  {
                    "operationId": "sync-mixed-work-order-1",
                    "entityType": "WORK_ORDER",
                    "entityId": %d,
                    "operationType": "SAVE_WORK_ORDER_PROGRESS",
                    "payload": "{\\"completionNotes\\":\\"Draft notes from mixed sync.\\"}"
                  }
                ]
                """.formatted(assignedInspection.getId(), assignedWorkOrder.getId());

        JsonNode response = sync(fieldEmployee, syncRequest(null, pendingOperations));

        assertThat(response.path("operations").size()).isEqualTo(2);
        assertThat(response.path("operations").get(0).path("status").asText()).isEqualTo("ACCEPTED");
        assertThat(response.path("operations").get(1).path("status").asText()).isEqualTo("ACCEPTED");
        assertThat(response.path("nextSyncToken").asText()).isNotBlank();

        JsonNode inspectionDelta = findInspectionDelta(response, assignedInspection.getId());
        assertThat(inspectionDelta.path("observations").asText()).isEqualTo("Checked during mixed sync.");

        JsonNode workOrderDelta = findWorkOrderDelta(response, assignedWorkOrder.getId());
        assertThat(workOrderDelta.path("draftCompletionNotes").asText()).isEqualTo("Draft notes from mixed sync.");

        assertThat(response.path("delta").path("dashboard").path("assignedWorkOrders").asLong())
                .isGreaterThanOrEqualTo(1L);
        assertThat(MobileSyncIntegrationFixture.deltaContainsAssetId(response, asset.getId())).isTrue();
    }

    @Test
    void invalidSyncToken_requiresFullSyncAndReturnsFullDelta() throws Exception {
        JsonNode firstSync = sync(fieldEmployee, syncRequest(null, "[]"));
        String validToken = firstSync.path("nextSyncToken").asText();

        JsonNode incrementalSync = sync(fieldEmployee, syncRequest(validToken, "[]"));
        assertThat(incrementalSync.path("delta").path("inspections").size()).isZero();

        JsonNode invalidTokenSync = sync(fieldEmployee, syncRequest("not-a-valid-sync-token", "[]"));

        assertThat(invalidTokenSync.path("requiresFullSync").asBoolean()).isTrue();
        assertThat(invalidTokenSync.path("warnings").size()).isEqualTo(1);
        assertThat(invalidTokenSync.path("warnings").get(0).path("code").asText()).isEqualTo("FULL_SYNC_REQUIRED");
        assertThat(MobileSyncIntegrationFixture.deltaContainsInspectionId(invalidTokenSync, assignedInspection.getId()))
                .isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(invalidTokenSync, assignedWorkOrder.getId()))
                .isTrue();
        assertThat(invalidTokenSync.path("delta").path("dashboard").isObject()).isTrue();
        assertThat(invalidTokenSync.path("delta").path("referenceData").path("schemaVersion").asInt()).isEqualTo(1);
    }

    @Test
    void duplicateOperationId_executesBusinessEffectOnce() throws Exception {
        String pendingOperations = """
                [
                  {
                    "operationId": "sync-idempotent-wo-1",
                    "entityType": "WORK_ORDER",
                    "entityId": %d,
                    "operationType": "SAVE_WORK_ORDER_PROGRESS",
                    "payload": "{\\"completionNotes\\":\\"Idempotent draft notes.\\"}"
                  }
                ]
                """.formatted(assignedWorkOrder.getId());

        JsonNode first = sync(fieldEmployee, syncRequest(null, pendingOperations));
        JsonNode second = sync(fieldEmployee, syncRequest(null, pendingOperations));

        assertThat(first.path("operations").get(0).path("status").asText()).isEqualTo("ACCEPTED");
        assertThat(second.path("operations").get(0).path("status").asText()).isEqualTo("ACCEPTED");
        assertThat(second.path("operations").get(0).path("serverUpdatedAt").asText())
                .isEqualTo(first.path("operations").get(0).path("serverUpdatedAt").asText());
        assertThat(fixture.reloadWorkOrder(assignedWorkOrder.getId()).getDraftCompletionNotes())
                .isEqualTo("Idempotent draft notes.");
    }

    @Test
    void conflictOperation_continuesBatchAndStoresIdempotentOutcome() throws Exception {
        WorkOrder conflictWorkOrder = fixture.createAssignedWorkOrder(
                asset,
                fieldEmployee.getId(),
                coordinator.getId(),
                WorkType.INTERNAL_MAINTENANCE,
                "Conflict target work order");
        fixture.completeWorkOrder(conflictWorkOrder.getId());

        String pendingOperations = """
                [
                  {
                    "operationId": "sync-batch-accepted-1",
                    "entityType": "INSPECTION",
                    "entityId": %d,
                    "operationType": "SAVE_INSPECTION_PROGRESS",
                    "payload": "{\\"observations\\":\\"Accepted while peer conflicts.\\",\\"answers\\":[]}"
                  },
                  {
                    "operationId": "sync-batch-conflict-1",
                    "entityType": "WORK_ORDER",
                    "entityId": %d,
                    "operationType": "SAVE_WORK_ORDER_PROGRESS",
                    "payload": "{\\"completionNotes\\":\\"Should conflict.\\"}"
                  }
                ]
                """.formatted(assignedInspection.getId(), conflictWorkOrder.getId());

        JsonNode response = sync(fieldEmployee, syncRequest(null, pendingOperations));

        assertThat(response.path("operations").size()).isEqualTo(2);
        assertThat(response.path("operations").get(0).path("status").asText()).isEqualTo("ACCEPTED");
        assertThat(response.path("operations").get(1).path("status").asText()).isEqualTo("CONFLICT");
        assertThat(response.path("conflicts").size()).isEqualTo(1);
        assertThat(response.path("conflicts").get(0).path("operationId").asText()).isEqualTo("sync-batch-conflict-1");
        assertThat(response.path("conflicts").get(0).path("entityId").asLong()).isEqualTo(conflictWorkOrder.getId());
        assertThat(findInspectionDelta(response, assignedInspection.getId()).path("observations").asText())
                .isEqualTo("Accepted while peer conflicts.");

        JsonNode replay = sync(fieldEmployee, syncRequest(null, pendingOperations));
        assertThat(replay.path("operations").get(1).path("status").asText()).isEqualTo("CONFLICT");
        assertThat(replay.path("conflicts").get(0).path("conflictType").asText())
                .isEqualTo(response.path("conflicts").get(0).path("conflictType").asText());
    }

    @Test
    void roleScoping_limitsDeltaToAuthorizedRecords() throws Exception {
        Department departmentA = fixture.createDepartment("Dept A");
        Department departmentB = fixture.createDepartment("Dept B");
        var category = fixture.createCategory();

        User managerA = fixture.createUser("manager-a", "Manager A", UserRole.MANAGER, departmentA);
        User fieldA = fixture.createUser("field-a", "Field A", UserRole.FIELD_EMPLOYEE, departmentA);
        User fieldB = fixture.createUser("field-b", "Field B", UserRole.FIELD_EMPLOYEE, departmentB);
        User contractor = fixture.createUser("contractor", "Contractor C", UserRole.CONTRACTOR, departmentA);

        Asset assetA = fixture.createAsset("Asset A", departmentA, category, coordinator.getId());
        Asset assetB = fixture.createAsset("Asset B", departmentB, category, coordinator.getId());
        BusinessTrigger triggerA = fixture.createTrigger(assetA, coordinator.getId());
        BusinessTrigger triggerB = fixture.createTrigger(assetB, coordinator.getId());

        Inspection inspectionA = fixture.createAssignedInspection(assetA, triggerA, fieldA.getId(), coordinator.getId());
        Inspection inspectionB = fixture.createAssignedInspection(assetB, triggerB, fieldB.getId(), coordinator.getId());
        WorkOrder workOrderA = fixture.createAssignedWorkOrder(
                assetA, fieldA.getId(), coordinator.getId(), WorkType.INTERNAL_MAINTENANCE, "Dept A maintenance");
        WorkOrder workOrderB = fixture.createAssignedWorkOrder(
                assetB, fieldB.getId(), coordinator.getId(), WorkType.INTERNAL_MAINTENANCE, "Dept B maintenance");
        WorkOrder contractorWorkOrder = fixture.createAssignedWorkOrder(
                assetA, contractor.getId(), coordinator.getId(), WorkType.CONTRACTOR_WORK, "Contractor maintenance");

        JsonNode fieldAResponse = sync(fieldA, syncRequest(null, "[]"));
        assertThat(MobileSyncIntegrationFixture.deltaContainsInspectionId(fieldAResponse, inspectionA.getId())).isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsInspectionId(fieldAResponse, inspectionB.getId())).isFalse();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(fieldAResponse, workOrderA.getId())).isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(fieldAResponse, workOrderB.getId())).isFalse();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(fieldAResponse, contractorWorkOrder.getId()))
                .isFalse();
        assertThat(MobileSyncIntegrationFixture.deltaContainsAssetId(fieldAResponse, assetA.getId())).isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsAssetId(fieldAResponse, assetB.getId())).isFalse();

        JsonNode managerAResponse = sync(managerA, syncRequest(null, "[]"));
        assertThat(MobileSyncIntegrationFixture.deltaContainsInspectionId(managerAResponse, inspectionA.getId())).isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsInspectionId(managerAResponse, inspectionB.getId())).isFalse();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(managerAResponse, workOrderA.getId())).isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(managerAResponse, workOrderB.getId())).isFalse();

        JsonNode contractorResponse = sync(contractor, syncRequest(null, "[]"));
        assertThat(MobileSyncIntegrationFixture.deltaContainsInspectionId(contractorResponse, inspectionA.getId()))
                .isFalse();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(contractorResponse, contractorWorkOrder.getId()))
                .isTrue();
        assertThat(MobileSyncIntegrationFixture.deltaContainsWorkOrderId(contractorResponse, workOrderA.getId()))
                .isFalse();
    }

    private JsonNode sync(User user, String requestBody) throws Exception {
        String responseBody = mockMvc.perform(post("/api/mobile/sync")
                        .header("Authorization", bearerToken(user))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return objectMapper.readTree(responseBody);
    }

    private String syncRequest(String syncToken, String pendingOperationsJson) {
        String tokenField = syncToken == null ? "" : ",\n  \"syncToken\": \"" + syncToken + "\"";
        return """
                {
                  "clientId": "android-sync-integration",
                  "clientVersion": "1",
                  "appVersion": "1.1.0"%s,
                  "pendingOperations": %s
                }
                """.formatted(tokenField, pendingOperationsJson);
    }

    private String bearerToken(User user) {
        return "Bearer " + jwtTokenProvider.generateToken(user.getId(), user.getEmail());
    }

    private static JsonNode findInspectionDelta(JsonNode response, long inspectionId) {
        for (JsonNode item : response.path("delta").path("inspections")) {
            if (item.path("id").asLong() == inspectionId) {
                return item;
            }
        }
        throw new AssertionError("Inspection delta not found for id " + inspectionId);
    }

    private static JsonNode findWorkOrderDelta(JsonNode response, long workOrderId) {
        for (JsonNode item : response.path("delta").path("workOrders")) {
            if (item.path("workOrderId").asLong() == workOrderId) {
                return item;
            }
        }
        throw new AssertionError("Work order delta not found for id " + workOrderId);
    }
}
