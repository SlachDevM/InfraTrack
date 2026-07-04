package com.infratrack.mobile.dto;

import java.util.List;

/**
 * Compact asset operational context returned to Android after a QR/barcode
 * scan resolves an asset business code (V2.4.0 Sprint M4-BE1, enriched M4-BE3).
 */
public class AssetContextResponse {

    private AssetContextSummaryResponse asset;
    private MobileAssetLastInspectionResponse lastInspection;
    private MobileAssetLastMaintenanceResponse lastMaintenance;
    private MobileAssetPreventivePlanResponse preventivePlan;
    private List<MobileIssueSummaryResponse> openIssues;
    private List<MobileInspectionSummaryResponse> activeInspections;
    private List<MobileWorkOrderSummaryResponse> activeWorkOrders;
    private AssetContextAllowedActionsResponse allowedActions;

    public AssetContextResponse(
            AssetContextSummaryResponse asset,
            MobileAssetLastInspectionResponse lastInspection,
            MobileAssetLastMaintenanceResponse lastMaintenance,
            MobileAssetPreventivePlanResponse preventivePlan,
            List<MobileIssueSummaryResponse> openIssues,
            List<MobileInspectionSummaryResponse> activeInspections,
            List<MobileWorkOrderSummaryResponse> activeWorkOrders,
            AssetContextAllowedActionsResponse allowedActions) {
        this.asset = asset;
        this.lastInspection = lastInspection;
        this.lastMaintenance = lastMaintenance;
        this.preventivePlan = preventivePlan;
        this.openIssues = openIssues;
        this.activeInspections = activeInspections;
        this.activeWorkOrders = activeWorkOrders;
        this.allowedActions = allowedActions;
    }

    public AssetContextSummaryResponse getAsset() {
        return asset;
    }

    public MobileAssetLastInspectionResponse getLastInspection() {
        return lastInspection;
    }

    public MobileAssetLastMaintenanceResponse getLastMaintenance() {
        return lastMaintenance;
    }

    public MobileAssetPreventivePlanResponse getPreventivePlan() {
        return preventivePlan;
    }

    public List<MobileIssueSummaryResponse> getOpenIssues() {
        return openIssues;
    }

    public List<MobileInspectionSummaryResponse> getActiveInspections() {
        return activeInspections;
    }

    public List<MobileWorkOrderSummaryResponse> getActiveWorkOrders() {
        return activeWorkOrders;
    }

    public AssetContextAllowedActionsResponse getAllowedActions() {
        return allowedActions;
    }
}
