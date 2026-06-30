package com.infratrack.operationsintelligence.dashboard;

import com.infratrack.operationsintelligence.OperationsKpiAuthorizationService;
import org.springframework.stereotype.Service;

@Service
public class DashboardPreferencesAuthorizationService {

    private final OperationsKpiAuthorizationService kpiAuthorizationService;

    public DashboardPreferencesAuthorizationService(OperationsKpiAuthorizationService kpiAuthorizationService) {
        this.kpiAuthorizationService = kpiAuthorizationService;
    }

    public void requireCanManageOwnPreferences(Long userId) {
        kpiAuthorizationService.requireCanViewKpis(userId);
    }
}
