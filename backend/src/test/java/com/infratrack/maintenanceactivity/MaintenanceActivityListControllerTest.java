package com.infratrack.maintenanceactivity;

import com.infratrack.config.GlobalExceptionHandler;
import com.infratrack.config.SecurityConfig;
import com.infratrack.maintenanceactivity.dto.MaintenanceActivityResponse;
import com.infratrack.observability.ObservabilityTestConfiguration;
import com.infratrack.security.JwtAuthenticationFilter;
import com.infratrack.security.JwtTokenProvider;
import com.infratrack.security.UserAccountStatusService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MaintenanceActivityListController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class, ObservabilityTestConfiguration.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MaintenanceActivityListControllerTest {

    private static final Long MANAGER_USER_ID = 30L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private MaintenanceActivityService maintenanceActivityService;

    @MockitoBean
    private UserAccountStatusService userAccountStatusService;

    @BeforeEach
    void setUpAccountStatus() {
        when(userAccountStatusService.isEnabled(any())).thenReturn(true);
    }

    @Test
    void listMaintenanceActivities_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/maintenance-activities"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void listMaintenanceActivities_returnsPaginatedPageWrapper() throws Exception {
        Page<MaintenanceActivityResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0);
        when(maintenanceActivityService.listPage(eq(MANAGER_USER_ID), any())).thenReturn(page);

        mockMvc.perform(get("/api/maintenance-activities?page=0&size=20")
                        .header("Authorization", bearerToken(MANAGER_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.size").value(20))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0))
                .andExpect(jsonPath("$.empty").value(true));

        verify(maintenanceActivityService).listPage(eq(MANAGER_USER_ID), any());
    }

    @Test
    void listMaintenanceActivities_eligibleForCompletionReview_usesEligibleService() throws Exception {
        Page<MaintenanceActivityResponse> page = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 20),
                0);
        when(maintenanceActivityService.listEligibleForCompletionReviewPage(eq(MANAGER_USER_ID), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/maintenance-activities?eligibleForCompletionReview=true")
                        .header("Authorization", bearerToken(MANAGER_USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        verify(maintenanceActivityService).listEligibleForCompletionReviewPage(eq(MANAGER_USER_ID), any());
    }

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, "manager@example.com");
    }
}
