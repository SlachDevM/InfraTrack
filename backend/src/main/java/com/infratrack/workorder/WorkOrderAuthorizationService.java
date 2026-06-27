package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

/**
 * Enforces role, department and assignee eligibility rules for work order operations.
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

    public void requireCoordinatorOwnDepartment(User coordinator, Asset asset) {
        Department coordinatorDepartment = coordinator.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (coordinatorDepartment == null || assetDepartment == null
                || !coordinatorDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "You may only create work orders for operational decisions in your own department.");
        }
    }

    public User requireEligibleAssignee(Long assignedToUserId, WorkType workType, Asset asset) {
        if (assignedToUserId == null) {
            throw new BusinessValidationException("Assigned user is required");
        }
        User assignee = userService.getById(assignedToUserId);
        if (workType == WorkType.INTERNAL_MAINTENANCE && !assignee.getRole().isFieldEmployee()) {
            throw new BusinessValidationException(
                    "Internal maintenance work orders must be assigned to a field employee");
        }
        if (workType == WorkType.CONTRACTOR_WORK && !assignee.getRole().isContractor()) {
            throw new BusinessValidationException(
                    "Contractor work orders must be assigned to a contractor");
        }
        if (!Boolean.TRUE.equals(assignee.getEnabled())) {
            throw new ForbiddenOperationException("Assigned worker is disabled.");
        }
        requireAssigneeDepartment(assignee, asset);
        return assignee;
    }

    private void requireAssigneeDepartment(User assignee, Asset asset) {
        Department assigneeDepartment = assignee.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (assigneeDepartment == null || assetDepartment == null
                || !assigneeDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "Assigned worker must belong to the work order asset department.");
        }
    }
}
