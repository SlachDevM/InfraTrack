package com.infratrack.reporting;

/**
 * Department filter for reporting exports. {@code departmentId} is {@code null} for organisation-wide scope.
 */
public record ExportScope(Long departmentId) {

    public static ExportScope global() {
        return new ExportScope(null);
    }

    public static ExportScope forDepartment(Long departmentId) {
        return new ExportScope(departmentId);
    }

    public boolean isGlobal() {
        return departmentId == null;
    }
}
