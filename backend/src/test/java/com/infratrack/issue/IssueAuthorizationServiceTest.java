package com.infratrack.issue;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetAuthorizationService;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IssueAuthorizationServiceTest {

    @Mock
    private AssetAuthorizationService assetAuthorizationService;

    @Mock
    private Issue issue;

    @Mock
    private Asset asset;

    @InjectMocks
    private IssueAuthorizationService issueAuthorizationService;

    @Test
    void requireCanViewIssue_shouldDelegateToAssetAuthorization() {
        User manager = new User("manager@test.com", "password", "Manager", UserRole.MANAGER);
        manager.setId(30L);
        when(issue.getAsset()).thenReturn(asset);

        issueAuthorizationService.requireCanViewIssue(manager, issue);

        verify(assetAuthorizationService).requireCanViewAsset(manager, asset);
    }
}
