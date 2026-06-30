package com.infratrack.mobile.dto;

import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;

public class MobileIssueSummaryResponse {

    private Long issueId;
    private String issueDescription;
    private IssueSeverity issueSeverity;

    public static MobileIssueSummaryResponse from(Issue issue) {
        MobileIssueSummaryResponse response = new MobileIssueSummaryResponse();
        response.issueId = issue.getId();
        response.issueDescription = issue.getDescription();
        response.issueSeverity = issue.getSeverity();
        return response;
    }

    public Long getIssueId() {
        return issueId;
    }

    public String getIssueDescription() {
        return issueDescription;
    }

    public IssueSeverity getIssueSeverity() {
        return issueSeverity;
    }
}
