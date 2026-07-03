package com.infratrack.organization.policy.approval;

import org.springframework.stereotype.Service;

/**
 * Selects the active approval policy for the organization.
 *
 * <p>No configurable modes exist yet; {@link DefaultApprovalPolicy} preserves existing behaviour.
 */
@Service
public class ApprovalPolicyService {

    private final ApprovalPolicy defaultPolicy = new DefaultApprovalPolicy();

    public ApprovalPolicy getPolicy() {
        return defaultPolicy;
    }
}
