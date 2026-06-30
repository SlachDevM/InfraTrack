package com.infratrack.operationsintelligence;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class OperationsKpiAuthorizationService {

    private final UserService userService;

    public OperationsKpiAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public OperationsKpiScope resolveScope(Long userId) {
        User user = userService.getById(userId);
        UserRole role = user.getRole();

        if (role.isAdministrator()) {
            return OperationsKpiScope.global();
        }
        if (role.isManager() || role.isOperationalCoordinator()) {
            Long departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;
            if (departmentId == null) {
                throw new ForbiddenOperationException(
                        "You must belong to a department to view operational KPIs.");
            }
            return OperationsKpiScope.forDepartment(departmentId);
        }
        throw new ForbiddenOperationException("You do not have permission to view operational KPIs.");
    }

    public void requireCanViewKpis(Long userId) {
        resolveScope(userId);
    }

    static boolean canViewKpis(UserRole role) {
        return role.isAdministrator() || role.isManager() || role.isOperationalCoordinator();
    }
}
