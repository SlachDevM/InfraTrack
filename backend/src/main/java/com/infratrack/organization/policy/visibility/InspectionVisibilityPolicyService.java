package com.infratrack.organization.policy.visibility;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Selects the active inspection visibility policy for the organization.
 *
 * Default is DEPARTMENT to preserve backwards compatibility.
 */
@Service
public class InspectionVisibilityPolicyService {

    private final InspectionVisibilityMode mode;

    private final InspectionVisibilityPolicy departmentPolicy = new DepartmentInspectionVisibilityPolicy();
    private final InspectionVisibilityPolicy organizationPolicy = new OrganizationInspectionVisibilityPolicy();

    public InspectionVisibilityPolicyService(
            @Value("${INSPECTION_VISIBILITY_POLICY:${app.policies.inspection.visibility:DEPARTMENT}}")
            String configuredMode) {
        this.mode = parseMode(configuredMode);
    }

    public InspectionVisibilityPolicy getPolicy() {
        return switch (mode) {
            case DEPARTMENT -> departmentPolicy;
            case ORGANIZATION -> organizationPolicy;
        };
    }

    InspectionVisibilityMode getMode() {
        return mode;
    }

    private static InspectionVisibilityMode parseMode(String configuredMode) {
        if (configuredMode == null || configuredMode.isBlank()) {
            return InspectionVisibilityMode.DEPARTMENT;
        }
        try {
            return InspectionVisibilityMode.valueOf(configuredMode.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Invalid inspection visibility policy: " + configuredMode
                            + " (expected DEPARTMENT or ORGANIZATION)");
        }
    }
}

