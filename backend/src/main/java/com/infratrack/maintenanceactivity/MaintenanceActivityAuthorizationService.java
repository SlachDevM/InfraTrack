package com.infratrack.maintenanceactivity;

import com.infratrack.asset.Asset;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Enforces read authorization for maintenance activities (R-Auth-6).
 */
@Service
public class MaintenanceActivityAuthorizationService {

    private final DelegatedAuthorityService delegatedAuthorityService;

    public MaintenanceActivityAuthorizationService(DelegatedAuthorityService delegatedAuthorityService) {
        this.delegatedAuthorityService = delegatedAuthorityService;
    }

    public void requireCanViewMaintenanceActivity(User user, MaintenanceActivity maintenanceActivity) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbidden();
        }
        if (role.isAdministrator()) {
            return;
        }

        Asset asset = maintenanceActivity.getAsset();
        WorkOrder workOrder = maintenanceActivity.getWorkOrder();

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
            requireAssignedOrPerformed(user, maintenanceActivity, workOrder);
            return;
        }
        throw forbidden();
    }

    private void requireAssignedOrPerformed(
            User user,
            MaintenanceActivity maintenanceActivity,
            WorkOrder workOrder) {
        if (Objects.equals(maintenanceActivity.getPerformedByUserId(), user.getId())) {
            return;
        }
        if (workOrder.getAssignedToUserId() != null
                && workOrder.getAssignedToUserId().equals(user.getId())) {
            return;
        }
        throw new ForbiddenOperationException(
                "You may only view maintenance activities for work orders assigned to you.");
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
                "You may only view maintenance activities for assets in your own department.");
    }

    private static ForbiddenOperationException forbidden() {
        return new ForbiddenOperationException("You are not authorized to view maintenance activities.");
    }
}
