package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

/**
 * Enforces role and assignment rules for inspection operations.
 */
@Service
public class InspectionAuthorizationService {

    private final UserService userService;

    public InspectionAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public void requireCanAssignInspections(Long userId) {
        requireCanAssignInspections(userService.getById(userId));
    }

    public void requireCanAssignInspections(User user) {
        if (!user.getRole().isOperationalCoordinator()) {
            throw new ForbiddenOperationException(
                    "Only operational coordinators can assign inspections");
        }
    }

    public User requireAssignedPerformer(Long userId, Inspection inspection) {
        User user = userService.getById(userId);
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            throw new ForbiddenOperationException(
                    "Only field employees and contractors can perform inspections");
        }
        if (!inspection.getAssignedToUserId().equals(userId)) {
            throw new ForbiddenOperationException(
                    "Only the assigned user can perform this inspection");
        }
        return user;
    }

    public void requireCanViewInspection(User user, Inspection inspection) {
        if (user.getRole() != null && user.getRole().isAdministrator()) {
            return;
        }
        requireSameDepartment(user, inspection.getAsset());
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
