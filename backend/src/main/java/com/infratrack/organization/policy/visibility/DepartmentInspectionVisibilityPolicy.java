package com.infratrack.organization.policy.visibility;

import com.infratrack.asset.Asset;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.user.User;

/**
 * Default policy: department-based inspection visibility.
 *
 * This implementation reproduces the existing behaviour embedded in {@code InspectionAuthorizationService}.
 */
public class DepartmentInspectionVisibilityPolicy implements InspectionVisibilityPolicy {

    @Override
    public void requireCanView(User user, Inspection inspection) {
        if (user.getRole() != null && user.getRole().isAdministrator()) {
            return;
        }
        requireSameDepartment(user, inspection.getAsset());
    }

    @Override
    public InspectionVisibilityScope resolveListScope(User user) {
        if (user.getRole() != null && user.getRole().isAdministrator()) {
            return InspectionVisibilityScope.all();
        }
        Department department = user.getDepartment();
        if (department == null) {
            // Preserve existing conventions: callers can treat null/unknown department as "no scope".
            return InspectionVisibilityScope.department(null);
        }
        return InspectionVisibilityScope.department(department.getId());
    }

    private void requireSameDepartment(User user, Asset asset) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (userDepartment == null || assetDepartment == null
                || !userDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "You may only view inspections for assets in your own department.");
        }
    }
}

