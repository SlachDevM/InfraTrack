package com.infratrack.mobile.sync;

import com.infratrack.config.GlobalExceptionHandler;
import com.infratrack.config.SecurityConfig;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.observability.ObservabilityTestConfiguration;
import com.infratrack.security.JwtAuthenticationFilter;
import com.infratrack.security.JwtTokenProvider;
import com.infratrack.security.UserAccountStatusService;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MobileSyncController.class)
@Import({
        GlobalExceptionHandler.class,
        SecurityConfig.class,
        JwtAuthenticationFilter.class,
        JwtTokenProvider.class,
        MobileSyncService.class,
        DefaultSyncTokenService.class,
        DefaultSyncOperationProcessor.class,
        ProcessedSyncOperationService.class,
        MobileSyncIdempotencyConfiguration.class,
        InspectionProgressSyncOperationHandler.class,
        InspectionSyncDeltaService.class,
        SyncMetricsRecorder.class,
        ObservabilityTestConfiguration.class,
        MobileSyncControllerOperationTest.FixedClockConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MobileSyncControllerOperationTest {

    private static final Long FIELD_USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T08:30:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MobileAuthorizationService authorizationService;

    @MockitoBean
    private UserAccountStatusService userAccountStatusService;

    @MockitoBean
    private InspectionSyncDeltaService inspectionSyncDeltaService;

    @MockitoBean
    private InspectionService inspectionService;

    @MockitoBean
    private ProcessedSyncOperationRepository processedSyncOperationRepository;

    @BeforeEach
    void setUp() {
        when(processedSyncOperationRepository.findById(any())).thenReturn(java.util.Optional.empty());
        when(processedSyncOperationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(userAccountStatusService.isEnabled(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        User fieldUser = new User();
        fieldUser.setId(FIELD_USER_ID);
        fieldUser.setEmail("field@test.com");
        fieldUser.setRole(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncInspectionDeltaResponse deltaInspection = new SyncInspectionDeltaResponse();
        deltaInspection.setId(123L);
        SyncDeltaResponse delta = SyncDeltaResponse.empty();
        delta.setInspections(java.util.List.of(deltaInspection));
        when(inspectionSyncDeltaService.build(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new InspectionSyncDeltaService.SyncDeltaBuildResult(delta, java.util.List.of()));
    }

    @Test
    void sync_validPendingOperation_returnsAcceptedOperationResult() throws Exception {
        when(inspectionService.saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID)))
                .thenReturn(new InspectionResponse());

        mockMvc.perform(post("/api/mobile/sync")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(syncRequestWithPendingOperationJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.operations").isArray())
                .andExpect(jsonPath("$.operations.length()").value(1))
                .andExpect(jsonPath("$.operations[0].operationId").value("8d89ef7e-1cb9-4fa4-91aa-3e06e405d70e"))
                .andExpect(jsonPath("$.operations[0].entityType").value("INSPECTION"))
                .andExpect(jsonPath("$.operations[0].entityId").value(123))
                .andExpect(jsonPath("$.operations[0].operationType").value("SAVE_INSPECTION_PROGRESS"))
                .andExpect(jsonPath("$.operations[0].status").value("ACCEPTED"))
                .andExpect(jsonPath("$.operations[0].serverUpdatedAt").value(FIXED_INSTANT.toString()))
                .andExpect(jsonPath("$.delta.inspections.length()").value(1))
                .andExpect(jsonPath("$.delta.inspections[0].id").value(123))
                .andExpect(jsonPath("$.conflicts").isEmpty())
                .andExpect(jsonPath("$.delta.assets").isEmpty())
                .andExpect(jsonPath("$.nextSyncToken").isNotEmpty());
    }

    @Test
    void sync_missingClientId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/mobile/sync")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientVersion": "1",
                                  "pendingOperations": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sync_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/api/mobile/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(syncRequestWithPendingOperationJson()))
                .andExpect(status().isUnauthorized());
    }

    private String syncRequestWithPendingOperationJson() {
        return """
                {
                  "clientId": "android-install-uuid",
                  "clientVersion": "1",
                  "appVersion": "1.1.0",
                  "pendingOperations": [
                    {
                      "operationId": "8d89ef7e-1cb9-4fa4-91aa-3e06e405d70e",
                      "entityType": "INSPECTION",
                      "entityId": 123,
                      "operationType": "SAVE_INSPECTION_PROGRESS",
                      "payload": "{\\"observedCondition\\":\\"GOOD\\",\\"observations\\":\\"Checked on site.\\",\\"issueIdentified\\":false,\\"answers\\":[{\\"questionId\\":10,\\"booleanValue\\":true}]}",
                      "createdAt": 1751700000000
                    }
                  ]
                }
                """;
    }

    private String bearerToken(Long userId, String email) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, email);
    }

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        }
    }
}
