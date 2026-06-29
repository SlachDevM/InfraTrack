package com.infratrack.preventivemaintenance;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class PreventiveMaintenancePlanAuthorizationService {

    private final UserService userService;

    public PreventiveMaintenancePlanAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public void requireCanViewPlans(Long userId) {
        if (!canViewPlans(userService.getById(userId).getRole())) {
            throw new ForbiddenOperationException(
                    "You do not have permission to view preventive maintenance plans");
        }
    }

    public void requireAdministrator(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isAdministrator()) {
            throw new ForbiddenOperationException(
                    "Only administrators can manage preventive maintenance plans");
        }
    }

    static boolean canViewPlans(UserRole role) {
        return role.isAdministrator() || role.isManager() || role.isOperationalCoordinator();
    }
}
