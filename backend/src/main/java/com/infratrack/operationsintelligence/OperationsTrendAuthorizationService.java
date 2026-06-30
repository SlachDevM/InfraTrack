package com.infratrack.operationsintelligence;

import com.infratrack.operationsintelligence.dto.TrendScopeResponse;
import org.springframework.stereotype.Service;

@Service
public class OperationsTrendAuthorizationService {

    private final OperationsKpiAuthorizationService kpiAuthorizationService;

    public OperationsTrendAuthorizationService(OperationsKpiAuthorizationService kpiAuthorizationService) {
        this.kpiAuthorizationService = kpiAuthorizationService;
    }

    public OperationsKpiScope resolveScope(Long userId) {
        return kpiAuthorizationService.resolveScope(userId);
    }

    public TrendScopeResponse resolveScopeResponse(Long userId) {
        OperationsKpiScope scope = resolveScope(userId);
        if (scope.isGlobal()) {
            return TrendScopeResponse.global();
        }
        return TrendScopeResponse.forDepartment(scope.departmentId());
    }

    public void requireCanViewTrends(Long userId) {
        kpiAuthorizationService.requireCanViewKpis(userId);
    }
}
