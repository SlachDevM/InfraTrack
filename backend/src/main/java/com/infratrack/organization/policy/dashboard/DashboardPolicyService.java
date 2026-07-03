package com.infratrack.organization.policy.dashboard;

import org.springframework.stereotype.Service;

/**
 * Selects the active dashboard policy for the organization.
 *
 * <p>No configurable modes exist yet; {@link DefaultDashboardPolicy} preserves existing behaviour.
 */
@Service
public class DashboardPolicyService {

    private final DashboardPolicy defaultPolicy = new DefaultDashboardPolicy();

    public DashboardPolicy getPolicy() {
        return defaultPolicy;
    }
}
