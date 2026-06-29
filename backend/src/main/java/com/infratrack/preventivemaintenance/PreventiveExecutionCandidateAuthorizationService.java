package com.infratrack.preventivemaintenance;

import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

@Service
public class PreventiveExecutionCandidateAuthorizationService {

    private final UserService userService;

    public PreventiveExecutionCandidateAuthorizationService(UserService userService) {
        this.userService = userService;
    }

    public void requireCanViewCandidates(Long userId) {
        if (!canViewCandidates(userService.getById(userId).getRole())) {
            throw new ForbiddenOperationException(
                    "You do not have permission to view preventive execution candidates");
        }
    }

    public void requireCanGenerateCandidates(Long userId) {
        UserRole role = userService.getById(userId).getRole();
        if (!canGenerateCandidates(role)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to generate preventive execution candidates");
        }
    }

    static boolean canViewCandidates(UserRole role) {
        return role.isAdministrator() || role.isManager() || role.isOperationalCoordinator();
    }

    static boolean canGenerateCandidates(UserRole role) {
        return role.isAdministrator() || role.isManager();
    }

    public void requireCanReviewCandidates(Long userId) {
        UserRole role = userService.getById(userId).getRole();
        if (!canReviewCandidates(role)) {
            throw new ForbiddenOperationException(
                    "You do not have permission to review preventive execution candidates");
        }
    }

    static boolean canReviewCandidates(UserRole role) {
        return role.isAdministrator() || role.isManager();
    }

    public void requireAuthorizedForCandidateAsset(Long userId, com.infratrack.asset.Asset asset) {
        com.infratrack.user.User user = userService.getById(userId);
        if (user.getRole().isAdministrator()) {
            return;
        }
        if (!user.getRole().isManager()) {
            throw new ForbiddenOperationException(
                    "You do not have permission to review preventive execution candidates");
        }
        requireManagerOwnDepartment(user, asset);
    }

    public void requireAuthorizedToViewReportAsset(Long userId, com.infratrack.asset.Asset asset) {
        com.infratrack.user.User user = userService.getById(userId);
        if (user.getRole().isAdministrator() || user.getRole().isOperationalCoordinator()) {
            return;
        }
        if (user.getRole().isManager()) {
            requireManagerOwnDepartment(user, asset);
            return;
        }
        throw new ForbiddenOperationException(
                "You do not have permission to view preventive execution reports");
    }

    private static void requireManagerOwnDepartment(com.infratrack.user.User user, com.infratrack.asset.Asset asset) {
        Long managerDepartmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;
        Long assetDepartmentId = asset.getDepartment().getId();
        if (managerDepartmentId == null || !managerDepartmentId.equals(assetDepartmentId)) {
            throw new ForbiddenOperationException(
                    "You may only access preventive execution candidates for assets in your own department.");
        }
    }
}
