package com.infratrack.organization.policy.visibility;

import org.springframework.stereotype.Service;

/**
 * Selects the active inspection visibility policy for the organization.
 *
 * This sprint always returns the department policy (no configuration yet).
 */
@Service
public class InspectionVisibilityPolicyService {

    private final InspectionVisibilityPolicy policy = new DepartmentInspectionVisibilityPolicy();

    public InspectionVisibilityPolicy getPolicy() {
        return policy;
    }
}

