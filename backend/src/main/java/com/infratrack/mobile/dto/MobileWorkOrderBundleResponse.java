package com.infratrack.mobile.dto;

public class MobileWorkOrderBundleResponse {

    private MobileWorkOrderDetailResponse workOrder;
    private MobileAssetSummaryResponse asset;
    private MobileIssueSummaryResponse issue;
    private MobileDecisionSummaryResponse decision;
    private MobileMaintenanceActivitySummaryResponse maintenanceActivity;
    private MobileAllowedActionsResponse allowedActions;

    public MobileWorkOrderBundleResponse(
            MobileWorkOrderDetailResponse workOrder,
            MobileAssetSummaryResponse asset,
            MobileIssueSummaryResponse issue,
            MobileDecisionSummaryResponse decision,
            MobileMaintenanceActivitySummaryResponse maintenanceActivity,
            MobileAllowedActionsResponse allowedActions) {
        this.workOrder = workOrder;
        this.asset = asset;
        this.issue = issue;
        this.decision = decision;
        this.maintenanceActivity = maintenanceActivity;
        this.allowedActions = allowedActions;
    }

    public MobileWorkOrderDetailResponse getWorkOrder() {
        return workOrder;
    }

    public MobileAssetSummaryResponse getAsset() {
        return asset;
    }

    public MobileIssueSummaryResponse getIssue() {
        return issue;
    }

    public MobileDecisionSummaryResponse getDecision() {
        return decision;
    }

    public MobileMaintenanceActivitySummaryResponse getMaintenanceActivity() {
        return maintenanceActivity;
    }

    public MobileAllowedActionsResponse getAllowedActions() {
        return allowedActions;
    }
}
