package com.infratrack.operationsintelligence.dto;

import java.util.Map;

public class IssueKpiResponse {

    private long openIssues;
    private long resolvedIssues;
    private long normalIssues;
    private long reworkIssues;
    private Map<String, Long> issuesBySeverity;
    private Map<String, Long> issuesByType;

    public long getOpenIssues() {
        return openIssues;
    }

    public void setOpenIssues(long openIssues) {
        this.openIssues = openIssues;
    }

    public long getResolvedIssues() {
        return resolvedIssues;
    }

    public void setResolvedIssues(long resolvedIssues) {
        this.resolvedIssues = resolvedIssues;
    }

    public long getNormalIssues() {
        return normalIssues;
    }

    public void setNormalIssues(long normalIssues) {
        this.normalIssues = normalIssues;
    }

    public long getReworkIssues() {
        return reworkIssues;
    }

    public void setReworkIssues(long reworkIssues) {
        this.reworkIssues = reworkIssues;
    }

    public Map<String, Long> getIssuesBySeverity() {
        return issuesBySeverity;
    }

    public void setIssuesBySeverity(Map<String, Long> issuesBySeverity) {
        this.issuesBySeverity = issuesBySeverity;
    }

    public Map<String, Long> getIssuesByType() {
        return issuesByType;
    }

    public void setIssuesByType(Map<String, Long> issuesByType) {
        this.issuesByType = issuesByType;
    }
}
