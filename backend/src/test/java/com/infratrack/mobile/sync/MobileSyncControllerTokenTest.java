package com.infratrack.mobile.sync;

import com.infratrack.config.GlobalExceptionHandler;
import com.infratrack.config.SecurityConfig;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.security.JwtAuthenticationFilter;
import com.infratrack.security.JwtTokenProvider;
import com.infratrack.security.UserAccountStatusService;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

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
        NoOpSyncOperationProcessor.class,
        NoOpSyncConflictResolver.class,
        MobileSyncControllerTokenTest.FixedClockConfig.class
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MobileSyncControllerTokenTest {

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

    @BeforeEach
    void setUp() {
        when(userAccountStatusService.isEnabled(org.mockito.ArgumentMatchers.any())).thenReturn(true);
        User fieldUser = new User();
        fieldUser.setId(FIELD_USER_ID);
        fieldUser.setEmail("field@test.com");
        fieldUser.setRole(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);
    }

    @Test
    void sync_returnsGeneratedTokenAndEmptyDelta() throws Exception {
        mockMvc.perform(post("/api/mobile/sync")
                        .header("Authorization", bearerToken(FIELD_USER_ID, "field@test.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validSyncRequestJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.protocolVersion").value(SyncProtocolVersion.CURRENT))
                .andExpect(jsonPath("$.serverTime").value(FIXED_INSTANT.toString()))
                .andExpect(jsonPath("$.nextSyncToken").isNotEmpty())
                .andExpect(jsonPath("$.delta.assets").isArray())
                .andExpect(jsonPath("$.delta.assets").isEmpty())
                .andExpect(jsonPath("$.delta.inspections").isEmpty())
                .andExpect(jsonPath("$.delta.workOrders").isEmpty())
                .andExpect(jsonPath("$.delta.documents").isEmpty())
                .andExpect(jsonPath("$.delta.users").isEmpty())
                .andExpect(jsonPath("$.delta.referenceData").isEmpty())
                .andExpect(jsonPath("$.operations").isEmpty())
                .andExpect(jsonPath("$.conflicts").isEmpty())
                .andExpect(jsonPath("$.warnings").isEmpty())
                .andExpect(jsonPath("$.requiresFullSync").value(false));
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

    @TestConfiguration
    static class FixedClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        }
    }
}
