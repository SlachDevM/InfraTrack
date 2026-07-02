package com.infratrack.organization.policy.visibility;

import com.infratrack.inspection.Inspection;
import com.infratrack.user.User;

/**
 * Organizational policy: determines inspection visibility.
 *
 * This is policy (configurable per organization in future), not a business rule.
 */
public interface InspectionVisibilityPolicy {

    void requireCanView(User user, Inspection inspection);

    InspectionVisibilityScope resolveListScope(User user);
}

