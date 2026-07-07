package com.infratrack.mobile.sync;

import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.dto.MobileDashboardResponse;
import com.infratrack.mobile.sync.dto.SyncDashboardDeltaResponse;
import com.infratrack.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Builds the dashboard section of the mobile sync delta (M6.2-BE1).
 */
@Service
class DashboardSyncDeltaService {

    private final MobileService mobileService;

    DashboardSyncDeltaService(MobileService mobileService) {
        this.mobileService = mobileService;
    }

    @Transactional(readOnly = true)
    SyncDashboardDeltaResponse buildSnapshot(User user, Instant generatedAt) {
        MobileDashboardResponse dashboard = mobileService.buildDashboard(user);
        return SyncDashboardDeltaResponse.from(dashboard, generatedAt);
    }
}
