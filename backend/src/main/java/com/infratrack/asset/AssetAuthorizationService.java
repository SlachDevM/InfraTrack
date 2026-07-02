package com.infratrack.asset;

import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Enforces asset view authorization for read operations such as asset history (R-Auth-5).
 */
@Service
public class AssetAuthorizationService {

    private final DelegatedAuthorityService delegatedAuthorityService;

    public AssetAuthorizationService(DelegatedAuthorityService delegatedAuthorityService) {
        this.delegatedAuthorityService = delegatedAuthorityService;
    }

    public void requireCanViewAsset(User user, Asset asset) {
        UserRole role = user.getRole();
        if (role == null) {
            throw forbidden();
        }
        if (role.isAdministrator()) {
            return;
        }
        if (role.isManager()) {
            if (!delegatedAuthorityService.canManagerActForAssetDepartment(
                    user, asset.getDepartment(), LocalDateTime.now())) {
                throw crossDepartmentDenied();
            }
            return;
        }
        if (role.isOperationalCoordinator() || role.isFieldEmployee() || role.isContractor()) {
            requireSameDepartment(user, asset);
            return;
        }
        throw forbidden();
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
                "You may only view asset history for assets in your own department.");
    }

    private static ForbiddenOperationException forbidden() {
        return new ForbiddenOperationException("You are not authorized to view asset history.");
    }
}
