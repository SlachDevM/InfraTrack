package com.infratrack.suggestedaction.dto;

import com.infratrack.issue.dto.IssueResponse;

public class ApproveSuggestedActionResponse {

    private SuggestedActionDetailResponse suggestedAction;
    private IssueResponse issue;

    public ApproveSuggestedActionResponse(SuggestedActionDetailResponse suggestedAction, IssueResponse issue) {
        this.suggestedAction = suggestedAction;
        this.issue = issue;
    }

    public SuggestedActionDetailResponse getSuggestedAction() {
        return suggestedAction;
    }

    public IssueResponse getIssue() {
        return issue;
    }
}
