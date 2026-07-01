package com.infratrack.reporting;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class ReportingAuthorizationService {

    private final UserService userService;

    public ReportingAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public ExportScope resolveScope(Long userId) {
        User user = userService.getById(userId);
        UserRole role = user.getRole();

        if (role.isAdministrator()) {
            return ExportScope.global();
        }
        if (role.isManager() || role.isOperationalCoordinator()) {
            Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;
            if (departmentId == null) {
                throw new ForbiddenOperationException(
                        "You must belong to a department to export operational reports.");
            }
            return ExportScope.forDepartment(departmentId);
        }
        throw new ForbiddenOperationException("You do not have permission to export operational reports.");
    }
}
