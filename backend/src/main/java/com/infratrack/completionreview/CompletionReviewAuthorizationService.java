package com.infratrack.completionreview;

import com.infratrack.asset.Asset;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Enforces role and department ownership rules for completion review operations.
 */
@Service
public class CompletionReviewAuthorizationService {

    private final UserService userService;
    private final DelegatedAuthorityService delegatedAuthorityService;

    public CompletionReviewAuthorizationService(
            UserService userService,
            DelegatedAuthorityService delegatedAuthorityService) {
        this.userService = userService;
        this.delegatedAuthorityService = delegatedAuthorityService;
    }

    public User requireManager(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager()) {
            throw new ForbiddenOperationException(
                    "Only managers can record completion reviews");
        }
        return user;
    }

    public void requireManagerAuthorizedForAsset(User manager, Asset asset, LocalDateTime reviewedAt) {
        if (!delegatedAuthorityService.canManagerActForAssetDepartment(
                manager,
                asset.getDepartment(),
                reviewedAt)) {
            throw new ForbiddenOperationException(
                    "You may only record completion reviews for assets in your own department.");
        }
    }
}
