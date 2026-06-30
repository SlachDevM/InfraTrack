package com.infratrack.operationsintelligence;

/**
 * Department filter for KPI aggregation. {@code departmentId} is {@code null} for organisation-wide scope.
 */
public record OperationsKpiScope(Long departmentId) {

    public static OperationsKpiScope global() {
        return new OperationsKpiScope(null);
    }

    public static OperationsKpiScope forDepartment(Long departmentId) {
        return new OperationsKpiScope(departmentId);
    }

    public boolean isGlobal() {
        return departmentId == null;
    }
}
