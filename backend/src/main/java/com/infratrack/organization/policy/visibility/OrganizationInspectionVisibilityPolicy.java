package com.infratrack.organization.policy.visibility;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;

/**
 * Small-council policy: organization-wide inspection visibility for operational users.
 *
 * View-only. Mutation permissions remain owned by domain authorization services.
 */
public class OrganizationInspectionVisibilityPolicy implements InspectionVisibilityPolicy {

    @Override
    public void requireCanView(User user, Inspection inspection) {
        UserRole role = user.getRole();
        if (role == null) {
            throw new ForbiddenOperationException("You may only view inspections for assets in your own department.");
        }
        if (role.isAdministrator()
                || role.isManager()
                || role.isOperationalCoordinator()
                || role.isFieldEmployee()
                || role.isContractor()) {
            return;
        }
        throw new ForbiddenOperationException("You may only view inspections for assets in your own department.");
    }

    @Override
    public InspectionVisibilityScope resolveListScope(User user) {
        return InspectionVisibilityScope.all();
    }
}

