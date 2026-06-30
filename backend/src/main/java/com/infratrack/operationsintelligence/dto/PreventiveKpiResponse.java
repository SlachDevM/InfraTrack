package com.infratrack.operationsintelligence.dto;

public class PreventiveKpiResponse {

    private long activePreventivePlans;
    private long pausedPreventivePlans;
    private long pendingExecutionCandidates;
    private long approvedExecutionCandidates;
    private long rejectedExecutionCandidates;
    private long dismissedExecutionCandidates;
    private long schedulerRunsToday;
    private long eligiblePlansNow;

    public long getActivePreventivePlans() {
        return activePreventivePlans;
    }

    public void setActivePreventivePlans(long activePreventivePlans) {
        this.activePreventivePlans = activePreventivePlans;
    }

    public long getPausedPreventivePlans() {
        return pausedPreventivePlans;
    }

    public void setPausedPreventivePlans(long pausedPreventivePlans) {
        this.pausedPreventivePlans = pausedPreventivePlans;
    }

    public long getPendingExecutionCandidates() {
        return pendingExecutionCandidates;
    }

    public void setPendingExecutionCandidates(long pendingExecutionCandidates) {
        this.pendingExecutionCandidates = pendingExecutionCandidates;
    }

    public long getApprovedExecutionCandidates() {
        return approvedExecutionCandidates;
    }

    public void setApprovedExecutionCandidates(long approvedExecutionCandidates) {
        this.approvedExecutionCandidates = approvedExecutionCandidates;
    }

    public long getRejectedExecutionCandidates() {
        return rejectedExecutionCandidates;
    }

    public void setRejectedExecutionCandidates(long rejectedExecutionCandidates) {
        this.rejectedExecutionCandidates = rejectedExecutionCandidates;
    }

    public long getDismissedExecutionCandidates() {
        return dismissedExecutionCandidates;
    }

    public void setDismissedExecutionCandidates(long dismissedExecutionCandidates) {
        this.dismissedExecutionCandidates = dismissedExecutionCandidates;
    }

    public long getSchedulerRunsToday() {
        return schedulerRunsToday;
    }

    public void setSchedulerRunsToday(long schedulerRunsToday) {
        this.schedulerRunsToday = schedulerRunsToday;
    }

    public long getEligiblePlansNow() {
        return eligiblePlansNow;
    }

    public void setEligiblePlansNow(long eligiblePlansNow) {
        this.eligiblePlansNow = eligiblePlansNow;
    }
}
