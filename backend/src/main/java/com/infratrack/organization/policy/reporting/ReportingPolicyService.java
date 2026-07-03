package com.infratrack.organization.policy.reporting;

import org.springframework.stereotype.Service;

/**
 * Selects the active reporting policy for the organization.
 *
 * <p>No configurable modes exist yet; {@link DefaultReportingPolicy} preserves existing behaviour.
 */
@Service
public class ReportingPolicyService {

    private final ReportingPolicy defaultPolicy = new DefaultReportingPolicy();

    public ReportingPolicy getPolicy() {
        return defaultPolicy;
    }
}
