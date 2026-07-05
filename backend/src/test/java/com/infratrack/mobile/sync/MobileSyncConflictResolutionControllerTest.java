package com.infratrack.mobile.sync;

import com.infratrack.config.GlobalExceptionHandler;
import com.infratrack.config.SecurityConfig;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionAction;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionResponse;
import com.infratrack.mobile.sync.dto.SyncConflictResolutionStatus;
import com.infratrack.security.JwtAuthenticationFilter;
import com.infratrack.security.JwtTokenProvider;
import com.infratrack.security.UserAccountStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MobileSyncConflictResolutionController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MobileSyncConflictResolutionControllerTest {

    private static final Long FIELD_USER_ID = 20L;
    private static final Long COORDINATOR_USER_ID = 30L;
    private static final Instant SERVER_TIME = Instant.parse("2026-07-05T09:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private SyncConflictResolutionService conflictResolutionService;

    @MockitoBean
    private UserAccountStatusService userAccountStatusService;

    @BeforeEach
    void setUp() {
        when(userAccountStatusService.isEnabled(any())).thenReturn(true);
    }

    @Test
    void resolve_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/mobile/sync/conflicts/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("SERVER_WINS")))
                .andExpect(status().isForbidden());
    }

    @Test
    void resolve_serverWins_returnsResolved() throws Exception {
        when(conflictResolutionService.resolve(eq(FIELD_USER_ID), any()))
                .thenReturn(response(SyncConflictResolutionStatus.RESOLVED, SyncConflictResolutionAction.SERVER_WINS));

        mockMvc.perform(post("/api/mobile/sync/conflicts/resolve")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("SERVER_WINS")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESOLVED"))
                .andExpect(jsonPath("$.resolution").value("SERVER_WINS"))
                .andExpect(jsonPath("$.operationId").value("op-1"));
    }

    @Test
    void resolve_clientRetry_returnsRetryRequired() throws Exception {
        when(conflictResolutionService.resolve(eq(FIELD_USER_ID), any()))
                .thenReturn(response(SyncConflictResolutionStatus.RETRY_REQUIRED, SyncConflictResolutionAction.CLIENT_RETRY));

        mockMvc.perform(post("/api/mobile/sync/conflicts/resolve")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("CLIENT_RETRY")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RETRY_REQUIRED"))
                .andExpect(jsonPath("$.resolution").value("CLIENT_RETRY"));
    }

    @Test
    void resolve_manualReview_returnsManualReviewRequired() throws Exception {
        when(conflictResolutionService.resolve(eq(FIELD_USER_ID), any()))
                .thenReturn(response(
                        SyncConflictResolutionStatus.MANUAL_REVIEW_REQUIRED,
                        SyncConflictResolutionAction.MANUAL_REVIEW));

        mockMvc.perform(post("/api/mobile/sync/conflicts/resolve")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("MANUAL_REVIEW")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MANUAL_REVIEW_REQUIRED"))
                .andExpect(jsonPath("$.resolution").value("MANUAL_REVIEW"));
    }

    @Test
    void resolve_invalidRequest_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/mobile/sync/conflicts/resolve")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "entityType": "INSPECTION"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resolve_unauthorizedRole_returnsForbidden() throws Exception {
        when(conflictResolutionService.resolve(eq(COORDINATOR_USER_ID), any()))
                .thenThrow(new ForbiddenOperationException("Mobile API access is not available for this role."));

        mockMvc.perform(post("/api/mobile/sync/conflicts/resolve")
                        .header("Authorization", bearerToken(COORDINATOR_USER_ID, "coord@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson("SERVER_WINS")))
                .andExpect(status().isForbidden());
    }

    private static SyncConflictResolutionResponse response(
            SyncConflictResolutionStatus status,
            SyncConflictResolutionAction resolution) {
        SyncConflictResolutionResponse response = new SyncConflictResolutionResponse();
        response.setOperationId("op-1");
        response.setEntityType("INSPECTION");
        response.setEntityId(123L);
        response.setOperationType("SAVE_INSPECTION_PROGRESS");
        response.setResolution(resolution);
        response.setStatus(status);
        response.setMessage("ok");
        response.setServerTime(SERVER_TIME);
        return response;
    }

    private static String validRequestJson(String resolution) {
        return """
                {
                  "operationId": "op-1",
                  "entityType": "INSPECTION",
                  "entityId": 123,
                  "operationType": "SAVE_INSPECTION_PROGRESS",
                  "conflictType": "WORKFLOW_COMPLETED",
                  "resolution": "%s"
                }
                """.formatted(resolution);
    }

    private String bearerToken(Long userId, String email) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, email);
    }
}
