package com.infratrack.inspection;

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
        User user = userService.getById(userId);
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
}
