package com.infratrack.issue;

import com.infratrack.asset.AssetAuthorizationService;
import com.infratrack.user.User;
import org.springframework.stereotype.Service;

/**
 * Enforces read authorization for issues based on the parent asset visibility rules.
 */
@Service
public class IssueAuthorizationService {

    private final AssetAuthorizationService assetAuthorizationService;

    public IssueAuthorizationService(AssetAuthorizationService assetAuthorizationService) {
        this.assetAuthorizationService = assetAuthorizationService;
    }

    public void requireCanViewIssue(User user, Issue issue) {
        assetAuthorizationService.requireCanViewAsset(user, issue.getAsset());
    }
}
