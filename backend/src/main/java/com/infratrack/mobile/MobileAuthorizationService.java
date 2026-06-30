package com.infratrack.mobile;

import com.infratrack.asset.Asset;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.springframework.stereotype.Service;

/**
 * Enforces mobile API access and read scoping for field-oriented endpoints.
 */
@Service
public class MobileAuthorizationService {

    private final UserService userService;
    private final InspectionAuthorizationService inspectionAuthorizationService;

    public MobileAuthorizationService(
            UserService userService,
            InspectionAuthorizationService inspectionAuthorizationService) {
        this.userService = userService;
        this.inspectionAuthorizationService = inspectionAuthorizationService;
    }

    public User requireMobileUser(Long userId) {
        User user = userService.getById(userId);
        requireMobileRole(user);
        return user;
    }

    public void requireCanViewInspectionBundle(User user, Inspection inspection) {
        requireMobileRole(user);
        if (user.getRole().isAdministrator()) {
            return;
        }
        if (user.getRole().isManager()) {
            inspectionAuthorizationService.requireCanViewInspection(user, inspection);
            return;
        }
        requireAssignedInspection(user, inspection);
    }

    public void requireCanViewWorkOrderBundle(User user, WorkOrder workOrder) {
        requireMobileRole(user);
        if (user.getRole().isAdministrator()) {
            return;
        }
        if (user.getRole().isManager()) {
            requireSameDepartment(user, workOrder.getAsset());
            return;
        }
        requireAssignedWorkOrder(user, workOrder);
    }

    public boolean canCompleteInspection(User user, Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.ASSIGNED) {
            return false;
        }
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            return false;
        }
        return inspection.getAssignedToUserId().equals(user.getId());
    }

    public boolean canCompleteMaintenance(User user, WorkOrder workOrder) {
        if (workOrder.getStatus() != WorkOrderStatus.ASSIGNED) {
            return false;
        }
        if (workOrder.getAssignedToUserId() == null
                || !workOrder.getAssignedToUserId().equals(user.getId())) {
            return false;
        }
        if (workOrder.getWorkType() == WorkType.INTERNAL_MAINTENANCE) {
            return user.getRole().isFieldEmployee();
        }
        if (workOrder.getWorkType() == WorkType.CONTRACTOR_WORK) {
            return user.getRole().isContractor();
        }
        return false;
    }

    private void requireMobileRole(User user) {
        UserRole role = user.getRole();
        if (role == null
                || (!role.isFieldEmployee()
                && !role.isContractor()
                && !role.isManager()
                && !role.isAdministrator())) {
            throw new ForbiddenOperationException("Mobile API access is not available for this role.");
        }
    }

    private void requireAssignedInspection(User user, Inspection inspection) {
        if (!inspection.getAssignedToUserId().equals(user.getId())) {
            throw new ForbiddenOperationException(
                    "You may only access inspections assigned to you.");
        }
    }

    private void requireAssignedWorkOrder(User user, WorkOrder workOrder) {
        if (workOrder.getAssignedToUserId() == null
                || !workOrder.getAssignedToUserId().equals(user.getId())) {
            throw new ForbiddenOperationException(
                    "You may only access work orders assigned to you.");
        }
    }

    private void requireSameDepartment(User user, Asset asset) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (userDepartment == null || assetDepartment == null
                || !userDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "You may only view work orders for assets in your own department.");
        }
    }
}
