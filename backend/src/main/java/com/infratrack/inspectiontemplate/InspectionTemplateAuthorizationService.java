package com.infratrack.inspectiontemplate;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class InspectionTemplateAuthorizationService {

    private final UserService userService;

    public InspectionTemplateAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public void requireCanViewTemplates(Long userId) {
        if (!canViewTemplates(userService.getById(userId).getRole())) {
            throw new ForbiddenOperationException(
                    "You do not have permission to view inspection templates");
        }
    }

    public void requireAdministrator(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isAdministrator()) {
            throw new ForbiddenOperationException(
                    "Only administrators can manage inspection templates");
        }
    }

    static boolean canViewTemplates(UserRole role) {
        return role.isAdministrator() || role.isManager() || role.isOperationalCoordinator();
    }
}
