package com.infratrack.inspection;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import com.infratrack.organization.policy.visibility.InspectionVisibilityPolicyService;
import org.springframework.stereotype.Service;

/**
 * Enforces role and assignment rules for inspection operations.
 */
@Service
public class InspectionAuthorizationService {

    private final UserService userService;
    private final InspectionVisibilityPolicyService visibilityPolicyService;

    public InspectionAuthorizationService(
            UserService userService,
            InspectionVisibilityPolicyService visibilityPolicyService) {
        this.userService = userService;
        this.visibilityPolicyService = visibilityPolicyService;
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
        visibilityPolicyService.getPolicy().requireCanView(user, inspection);
    }

    public void requireCanSaveInspectionAnswers(User user, Inspection inspection) {
        if (user.getRole() != null && user.getRole().isAdministrator()) {
            return;
        }
        if (user.getRole().isManager() || user.getRole().isOperationalCoordinator()) {
            requireCanViewInspection(user, inspection);
            return;
        }
        if (user.getRole().isFieldEmployee() || user.getRole().isContractor()) {
            if (!inspection.getAssignedToUserId().equals(user.getId())) {
                throw new ForbiddenOperationException(
                        "Only the assigned user can save inspection answers");
            }
            return;
        }
        throw new ForbiddenOperationException("You do not have permission to save inspection answers");
    }

    // Visibility rules are delegated to InspectionVisibilityPolicy (BDR-004).
}
