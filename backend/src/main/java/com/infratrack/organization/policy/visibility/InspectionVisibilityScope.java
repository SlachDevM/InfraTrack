package com.infratrack.organization.policy.visibility;

/**
 * Lightweight scope description for visibility-aware list queries.
 *
 * This sprint does not apply this scope to any list endpoints (no behaviour change).
 */
public record InspectionVisibilityScope(
        InspectionVisibilityScopeType type,
        Long departmentId,
        Long assignedToUserId
) {
    public static InspectionVisibilityScope all() {
        return new InspectionVisibilityScope(InspectionVisibilityScopeType.ALL, null, null);
    }

    public static InspectionVisibilityScope department(Long departmentId) {
        return new InspectionVisibilityScope(InspectionVisibilityScopeType.DEPARTMENT, departmentId, null);
    }

    public static InspectionVisibilityScope assignedOnly(Long assignedToUserId) {
        return new InspectionVisibilityScope(InspectionVisibilityScopeType.ASSIGNED_ONLY, null, assignedToUserId);
    }
}

