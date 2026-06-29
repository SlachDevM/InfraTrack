package com.infratrack.preventivemaintenance;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class PreventiveSchedulerAuthorizationService {

    private final UserService userService;

    public PreventiveSchedulerAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public void requireCanRunScheduler(Long userId) {
        if (!canRunScheduler(userService.getById(userId).getRole())) {
            throw new ForbiddenOperationException(
                    "You do not have permission to run the preventive scheduler");
        }
    }

    public void requireCanViewSchedulerRuns(Long userId) {
        if (!canViewSchedulerRuns(userService.getById(userId).getRole())) {
            throw new ForbiddenOperationException(
                    "You do not have permission to view preventive scheduler runs");
        }
    }

    public Long resolveDepartmentScopeForManualRun(Long userId) {
        User user = userService.getById(userId);
        if (user.getRole().isAdministrator()) {
            return null;
        }
        if (user.getRole().isManager()) {
            return user.getDepartment() != null ? user.getDepartment().getId() : null;
        }
        throw new ForbiddenOperationException(
                "You do not have permission to run the preventive scheduler");
    }

    static boolean canRunScheduler(UserRole role) {
        return role.isAdministrator() || role.isManager();
    }

    static boolean canViewSchedulerRuns(UserRole role) {
        return role.isAdministrator() || role.isManager() || role.isOperationalCoordinator();
    }
}
