package com.infratrack.workorder;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

/**
 * Enforces role and assignment rules for work order operations.
 */
@Service
public class WorkOrderAuthorizationService {

    private final UserService userService;

    public WorkOrderAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public User requireOperationalCoordinator(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isOperationalCoordinator()) {
            throw new ForbiddenOperationException(
                    "Only operational coordinators can create work orders");
        }
        return user;
    }

    public User requireOperationalCoordinatorForAssignment(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isOperationalCoordinator()) {
            throw new ForbiddenOperationException(
                    "Only operational coordinators can assign work orders");
        }
        return user;
    }
}
