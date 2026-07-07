package com.infratrack.mobile.sync;

import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.dto.MobileDashboardResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardSyncDeltaServiceTest {

    private static final Instant GENERATED_AT = Instant.parse("2026-07-05T08:30:00Z");

    @Mock
    private MobileService mobileService;

    private DashboardSyncDeltaService deltaService;

    @BeforeEach
    void setUp() {
        deltaService = new DashboardSyncDeltaService(mobileService);
    }

    @Test
    void buildSnapshot_mapsDashboardFieldsAndGeneratedAt() {
        User fieldUser = user(20L);
        MobileDashboardResponse dashboard = new MobileDashboardResponse(3L, 2L, 1L, 0L, 4L);
        when(mobileService.buildDashboard(fieldUser)).thenReturn(dashboard);

        var result = deltaService.buildSnapshot(fieldUser, GENERATED_AT);

        assertThat(result.getGeneratedAt()).isEqualTo(GENERATED_AT.toEpochMilli());
        assertThat(result.getAssignedInspections()).isEqualTo(3L);
        assertThat(result.getAssignedWorkOrders()).isEqualTo(2L);
        assertThat(result.getOverdueInspections()).isEqualTo(1L);
        assertThat(result.getOverdueWorkOrders()).isZero();
        assertThat(result.getCompletedToday()).isEqualTo(4L);
        verify(mobileService).buildDashboard(fieldUser);
    }

    @Test
    void buildSnapshot_reusesMobileDashboardLogic() {
        User manager = user(5L);
        MobileDashboardResponse dashboard = new MobileDashboardResponse(10L, 8L, 2L, 0L, 1L);
        when(mobileService.buildDashboard(manager)).thenReturn(dashboard);

        var result = deltaService.buildSnapshot(manager, GENERATED_AT);

        assertThat(result.getAssignedInspections()).isEqualTo(dashboard.getAssignedInspections());
        assertThat(result.getAssignedWorkOrders()).isEqualTo(dashboard.getAssignedWorkOrders());
        assertThat(result.getOverdueInspections()).isEqualTo(dashboard.getOverdueInspections());
        assertThat(result.getOverdueWorkOrders()).isEqualTo(dashboard.getOverdueWorkOrders());
        assertThat(result.getCompletedToday()).isEqualTo(dashboard.getCompletedToday());
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("user@test.com");
        user.setRole(UserRole.FIELD_EMPLOYEE);
        return user;
    }
}
