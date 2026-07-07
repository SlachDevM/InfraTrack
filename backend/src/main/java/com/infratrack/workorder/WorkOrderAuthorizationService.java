package com.infratrack.workorder;

import com.infratrack.asset.Asset;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Enforces role, department and assignee eligibility rules for work order operations.
 */
@Service
public class WorkOrderAuthorizationService {

    private final UserService userService;
    private final DelegatedAuthorityService delegatedAuthorityService;

    public WorkOrderAuthorizationService(
            UserService userService,
            DelegatedAuthorityService delegatedAuthorityService) {
        this.userService = userService;
        this.delegatedAuthorityService = delegatedAuthorityService;
    }

    public void requireCanViewWorkOrder(User user, WorkOrder workOrder) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbidden();
        }
        if (role.isAdministrator()) {
            return;
        }

        Asset asset = workOrder.getAsset();
        if (role.isManager()) {
            if (!delegatedAuthorityService.canManagerActForAssetDepartment(
                    user, asset.getDepartment(), LocalDateTime.now())) {
                throw crossDepartmentDenied();
            }
            return;
        }
        if (role.isOperationalCoordinator()) {
            requireSameDepartment(user, asset);
            return;
        }
        if (role.isFieldEmployee() || role.isContractor()) {
            requireAssignedWorkOrder(user, workOrder);
            return;
        }
        throw forbidden();
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
                    "You may only manage work orders for assets in your own department.");
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

    private void requireAssignedWorkOrder(User user, WorkOrder workOrder) {
        if (workOrder.getAssignedToUserId() == null
                || !workOrder.getAssignedToUserId().equals(user.getId())) {
            throw new ForbiddenOperationException(
                    "You may only view work orders assigned to you.");
        }
    }

    private void requireSameDepartment(User user, Asset asset) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (userDepartment == null || assetDepartment == null
                || !userDepartment.getId().equals(assetDepartment.getId())) {
            throw crossDepartmentDenied();
        }
    }

    private static ForbiddenOperationException crossDepartmentDenied() {
        return new ForbiddenOperationException(
                "You may only view work orders for assets in your own department.");
    }

    private static ForbiddenOperationException forbidden() {
        return new ForbiddenOperationException("You are not authorized to view work orders.");
    }
}
