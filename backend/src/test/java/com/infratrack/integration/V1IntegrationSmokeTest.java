package com.infratrack.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.assetcategory.AssetCategoryRepository;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.department.DepartmentRepository;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.security.JwtTokenProvider;
import com.infratrack.user.User;
import com.infratrack.user.UserRepository;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class V1IntegrationSmokeTest {

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
    private PasswordEncoder passwordEncoder;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private AssetCategoryRepository assetCategoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusinessTriggerRepository businessTriggerRepository;

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private IssueRepository issueRepository;

    @Autowired
    private AssetRepository assetRepository;

    private Long departmentId;
    private Long categoryId;
    private Long coordinatorUserId;
    private String coordinatorEmail;
    private Long fieldEmployeeUserId;
    private String fieldEmployeeEmail;

    @BeforeEach
    void setUp() throws Exception {
        Files.createDirectories(Path.of(System.getProperty("java.io.tmpdir"), "infratrack-integration-docs"));

        Department department = departmentRepository.save(new Department("Smoke Dept " + System.nanoTime()));
        AssetCategory category = assetCategoryRepository.save(new AssetCategory("Smoke Cat " + System.nanoTime()));

        User coordinator = buildUser(
                "coordinator+" + System.nanoTime() + "@smoke.test",
                "Smoke Coordinator",
                UserRole.OPERATIONAL_COORDINATOR);
        coordinator.setDepartment(department);
        coordinator = userRepository.save(coordinator);
        User fieldEmployee = userRepository.save(buildUser(
                "field+" + System.nanoTime() + "@smoke.test",
                "Smoke Field Employee",
                UserRole.FIELD_EMPLOYEE));

        departmentId = department.getId();
        categoryId = category.getId();
        coordinatorUserId = coordinator.getId();
        coordinatorEmail = coordinator.getEmail();
        fieldEmployeeUserId = fieldEmployee.getId();
        fieldEmployeeEmail = fieldEmployee.getEmail();
    }

    @Test
    void protectedEndpoint_withoutToken_isRejected() throws Exception {
        mockMvc.perform(get("/api/assets"))
                .andExpect(status().isForbidden());
    }

    @Test
    void protectedEndpoint_withValidToken_isAllowed() throws Exception {
        mockMvc.perform(get("/api/assets")
                        .header("Authorization", bearerToken(coordinatorUserId, coordinatorEmail)))
                .andExpect(status().isOk());
    }

    @Test
    void registerAsset_createsAssetHistoryEntry() throws Exception {
        String body = objectMapper.writeValueAsString(new RegisterAssetPayload(
                "Bridge A",
                departmentId,
                categoryId,
                "Main Street",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 6, 1)));

        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", bearerToken(coordinatorUserId, coordinatorEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Bridge A"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long assetId = objectMapper.readTree(response).get("id").asLong();

        mockMvc.perform(get("/api/assets/" + assetId + "/history")
                        .header("Authorization", bearerToken(coordinatorUserId, coordinatorEmail)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].eventType").value("ASSET_REGISTERED"))
                .andExpect(jsonPath("$.content[0].responsibleUserId").value(coordinatorUserId));
    }

    @Test
    void registerAsset_withInvalidEnum_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "Bridge B",
                  "departmentId": %d,
                  "assetCategoryId": %d,
                  "location": "Side Street",
                  "status": "NOT_A_REAL_STATUS",
                  "registrationDate": "2024-06-01"
                }
                """.formatted(departmentId, categoryId);

        mockMvc.perform(post("/api/assets")
                        .header("Authorization", bearerToken(coordinatorUserId, coordinatorEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Invalid request body"));
    }

    @Test
    void registerAsset_withBlankName_returnsBadRequestBeforeServiceLogic() throws Exception {
        String body = objectMapper.writeValueAsString(new RegisterAssetPayload(
                "",
                departmentId,
                categoryId,
                "Main Street",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 6, 1)));

        mockMvc.perform(post("/api/assets")
                        .header("Authorization", bearerToken(coordinatorUserId, coordinatorEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("name: must not be blank"));
    }

    @Test
    void login_withInvalidEmail_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"secret"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("email: must be a well-formed email address"));
    }

    @Test
    void createIssue_withMissingInspectionId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/issues")
                        .header("Authorization", bearerToken(fieldEmployeeUserId, fieldEmployeeEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "description": "Surface damage observed",
                                  "severity": "MEDIUM",
                                  "recordedAt": "2024-06-01T10:00:00"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("inspectionId: must not be null"));
    }

    @Test
    void uploadOperationalDocument_multipartRequest_succeeds() throws Exception {
        Long assetId = createAssetThroughService();

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "evidence.jpg",
                "image/jpeg",
                new byte[] {
                        (byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0,
                        0x00, 0x10, 0x4A, 0x46, 0x49, 0x46
                });

        mockMvc.perform(multipart("/api/assets/" + assetId + "/documents")
                        .file(file)
                        .param("documentType", "PHOTO")
                        .header("Authorization", bearerToken(coordinatorUserId, coordinatorEmail)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documentType").value("PHOTO"))
                .andExpect(jsonPath("$.assetId").value(assetId));
    }

    @Test
    void duplicateIssueForInspection_viaHttp_returnsConflict() throws Exception {
        Long inspectionId = createCompletedInspectionWithIssueIdentified();

        String issueBody = objectMapper.writeValueAsString(new RecordIssuePayload(
                inspectionId,
                "Surface damage observed",
                IssueSeverity.MEDIUM,
                LocalDateTime.now().minusMinutes(5)));

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", bearerToken(fieldEmployeeUserId, fieldEmployeeEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueBody))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/issues")
                        .header("Authorization", bearerToken(fieldEmployeeUserId, fieldEmployeeEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(issueBody))
                .andExpect(status().isConflict())
                .andExpect(content().string("An issue has already been recorded for this inspection"));
    }

    @Test
    void duplicateIssueForInspection_databaseUniqueConstraintIsEnforced() {
        Inspection inspection = createCompletedInspectionEntity();

        issueRepository.save(new Issue(
                inspection,
                inspection.getAsset(),
                "First issue",
                IssueSeverity.LOW,
                fieldEmployeeUserId,
                LocalDateTime.now().minusMinutes(10)));

        Issue duplicate = new Issue(
                inspection,
                inspection.getAsset(),
                "Second issue",
                IssueSeverity.HIGH,
                fieldEmployeeUserId,
                LocalDateTime.now().minusMinutes(5));

        assertThatThrownBy(() -> issueRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    private Long createAssetThroughService() throws Exception {
        String body = objectMapper.writeValueAsString(new RegisterAssetPayload(
                "Upload Asset",
                departmentId,
                categoryId,
                "Upload Street",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 6, 2)));

        String response = mockMvc.perform(post("/api/assets")
                        .header("Authorization", bearerToken(coordinatorUserId, coordinatorEmail))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createCompletedInspectionWithIssueIdentified() {
        return createCompletedInspectionEntity().getId();
    }

    private Inspection createCompletedInspectionEntity() {
        Asset asset = assetRepository.save(new Asset(
                "Inspection Asset",
                departmentRepository.findById(departmentId).orElseThrow(),
                assetCategoryRepository.findById(categoryId).orElseThrow(),
                "Inspection Street",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 6, 1),
                coordinatorUserId));

        BusinessTrigger trigger = businessTriggerRepository.save(new BusinessTrigger(
                asset,
                BusinessTriggerType.SCHEDULED_INSPECTION,
                "Routine check",
                false,
                coordinatorUserId));

        Inspection inspection = new Inspection(
                asset,
                trigger,
                fieldEmployeeUserId,
                coordinatorUserId,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7));
        inspection.complete(
                PhysicalCondition.FAIR,
                "Issue likely",
                true,
                LocalDateTime.now().minusHours(1),
                fieldEmployeeUserId);
        return inspectionRepository.save(inspection);
    }

    private User buildUser(String email, String name, UserRole role) {
        User user = new User(email, passwordEncoder.encode("Secret1!"), name, role);
        user.setEnabled(true);
        return user;
    }

    private String bearerToken(Long userId, String email) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, email);
    }

    private record RegisterAssetPayload(
            String name,
            Long departmentId,
            Long assetCategoryId,
            String location,
            AssetStatus status,
            LocalDate registrationDate) {
    }

    private record RecordIssuePayload(
            Long inspectionId,
            String description,
            IssueSeverity severity,
            LocalDateTime recordedAt) {
    }
}
