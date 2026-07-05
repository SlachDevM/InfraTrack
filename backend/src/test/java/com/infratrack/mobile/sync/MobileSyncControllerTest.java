package com.infratrack.mobile.sync;

import com.infratrack.config.GlobalExceptionHandler;
import com.infratrack.config.SecurityConfig;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.mobile.sync.dto.SyncResponse;
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

@WebMvcTest(controllers = MobileSyncController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MobileSyncControllerTest {

    private static final Long FIELD_USER_ID = 20L;
    private static final Long COORDINATOR_USER_ID = 30L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MobileSyncService mobileSyncService;

    @MockitoBean
    private UserAccountStatusService userAccountStatusService;

    @BeforeEach
    void setUpAccountStatus() {
        when(userAccountStatusService.isEnabled(any())).thenReturn(true);
    }

    @Test
    void sync_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(post("/api/mobile/sync")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSyncRequestJson()))
                .andExpect(status().isForbidden());
    }

    @Test
    void sync_validRequest_returns200WithEmptyEnvelope() throws Exception {
        Instant serverTime = Instant.parse("2026-07-05T08:30:00Z");
        SyncResponse response = new SyncResponse();
        response.setServerTime(serverTime);
        response.setNextSyncToken(null);
        response.setRequiresFullSync(false);

        when(mobileSyncService.sync(eq(FIELD_USER_ID), any())).thenReturn(response);

        mockMvc.perform(post("/api/mobile/sync")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSyncRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.serverTime").value("2026-07-05T08:30:00Z"))
                .andExpect(jsonPath("$.nextSyncToken").isEmpty())
                .andExpect(jsonPath("$.operations").isArray())
                .andExpect(jsonPath("$.operations").isEmpty())
                .andExpect(jsonPath("$.conflicts").isArray())
                .andExpect(jsonPath("$.conflicts").isEmpty())
                .andExpect(jsonPath("$.warnings").isArray())
                .andExpect(jsonPath("$.warnings").isEmpty())
                .andExpect(jsonPath("$.requiresFullSync").value(false));
    }

    @Test
    void sync_missingClientId_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/mobile/sync")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "clientVersion": "1"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sync_unauthorizedRole_returnsForbidden() throws Exception {
        when(mobileSyncService.sync(eq(COORDINATOR_USER_ID), any()))
                .thenThrow(new ForbiddenOperationException("Mobile API access is not available for this role."));

        mockMvc.perform(post("/api/mobile/sync")
                        .header("Authorization", bearerToken(COORDINATOR_USER_ID, "coord@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSyncRequestJson()))
                .andExpect(status().isForbidden())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .content().string("Mobile API access is not available for this role."));
    }

    private String validSyncRequestJson() {
        return """
                {
                  "clientId": "android-install-uuid",
                  "clientVersion": "1",
                  "appVersion": "1.1.0",
                  "pendingOperations": []
                }
                """;
    }

    private String bearerToken(Long userId, String email) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, email);
    }
}
