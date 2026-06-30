package com.infratrack.operationsintelligence;

import org.springframework.stereotype.Service;

@Service
public class OperationsRecentActivityAuthorizationService {

    private final OperationsKpiAuthorizationService kpiAuthorizationService;

    public OperationsRecentActivityAuthorizationService(OperationsKpiAuthorizationService kpiAuthorizationService) {
        this.kpiAuthorizationService = kpiAuthorizationService;
    }

    public OperationsKpiScope resolveScope(Long userId) {
        return kpiAuthorizationService.resolveScope(userId);
    }

    public void requireCanViewRecentActivity(Long userId) {
        kpiAuthorizationService.requireCanViewKpis(userId);
    }
}
