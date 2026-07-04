package com.infratrack.mobile;

import com.infratrack.asset.Asset;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
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

import java.time.LocalDateTime;

/**
 * Enforces mobile API access and read scoping for field-oriented endpoints.
 */
@Service
public class MobileAuthorizationService {

    private final UserService userService;
    private final InspectionAuthorizationService inspectionAuthorizationService;
    private final DelegatedAuthorityService delegatedAuthorityService;

    public MobileAuthorizationService(
            UserService userService,
            InspectionAuthorizationService inspectionAuthorizationService,
            DelegatedAuthorityService delegatedAuthorityService) {
        this.userService = userService;
        this.inspectionAuthorizationService = inspectionAuthorizationService;
        this.delegatedAuthorityService = delegatedAuthorityService;
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

    /**
     * Authorizes mobile asset context lookup by QR/barcode scan (M4-BE1).
     * Administrators may look up any asset. Managers may look up assets in
     * their own or a delegated department. Operational coordinators, field
     * employees and contractors are limited to their own department, which
     * is the existing conservative asset visibility rule for these roles.
     */
    public void requireCanViewAssetContext(User user, Asset asset) {
        UserRole role = user.getRole();
        if (role == null) {
            throw new ForbiddenOperationException("Mobile asset lookup is not available for this role.");
        }
        if (role.isAdministrator()) {
            return;
        }
        if (role.isManager()) {
            if (delegatedAuthorityService.canManagerActForAssetDepartment(
                    user, asset.getDepartment(), LocalDateTime.now())) {
                return;
            }
            throw new ForbiddenOperationException(
                    "You may only look up assets in your own or delegated department.");
        }
        if (role.isOperationalCoordinator() || role.isFieldEmployee() || role.isContractor()) {
            requireSameDepartmentForAssetLookup(user, asset);
            return;
        }
        throw new ForbiddenOperationException("Mobile asset lookup is not available for this role.");
    }

    /**
     * Conservative approximation of the existing "assign inspection" rule
     * (Operational Coordinator, own department) for the asset context screen.
     */
    public boolean canCreateInspectionForAsset(User user, Asset asset) {
        return user.getRole() != null
                && user.getRole().isOperationalCoordinator()
                && isSameDepartment(user, asset);
    }

    /**
     * Conservative approximation of the existing "record issue" rule
     * (field employee or contractor, own department) for the asset context screen.
     */
    public boolean canCreateIssueForAsset(User user, Asset asset) {
        return user.getRole() != null
                && (user.getRole().isFieldEmployee() || user.getRole().isContractor())
                && isSameDepartment(user, asset);
    }

    private void requireSameDepartmentForAssetLookup(User user, Asset asset) {
        if (!isSameDepartment(user, asset)) {
            throw new ForbiddenOperationException(
                    "You may only look up assets in your own department.");
        }
    }

    private boolean isSameDepartment(User user, Asset asset) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        return userDepartment != null
                && assetDepartment != null
                && userDepartment.getId().equals(assetDepartment.getId());
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
