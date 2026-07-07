package com.infratrack.mobile.sync.dto;

import com.infratrack.mobile.dto.AssetContextResponse;
import com.infratrack.mobile.dto.AssetContextSummaryResponse;
import com.infratrack.mobile.dto.MobileAssetDocumentSummaryResponse;
import com.infratrack.mobile.dto.MobileAssetLastInspectionResponse;
import com.infratrack.mobile.dto.MobileAssetLastMaintenanceResponse;
import com.infratrack.mobile.dto.MobileAssetPreventivePlanResponse;
import com.infratrack.mobile.dto.MobileInspectionSummaryResponse;
import com.infratrack.mobile.dto.MobileIssueSummaryResponse;
import com.infratrack.mobile.dto.MobileWorkOrderSummaryResponse;

import java.util.ArrayList;
import java.util.List;

/**
 * Compact asset operational context for mobile sync delta download (M6.3-BE1).
 * Aligns with {@code GET /api/mobile/assets/lookup} enrichment sections.
 */
public class SyncAssetDeltaResponse {

    private AssetContextSummaryResponse asset;

    private MobileAssetLastInspectionResponse lastInspection;

    private MobileAssetLastMaintenanceResponse lastMaintenance;

    private MobileAssetPreventivePlanResponse preventivePlan;

    private List<MobileIssueSummaryResponse> openIssues = new ArrayList<>();

    private List<MobileInspectionSummaryResponse> activeInspections = new ArrayList<>();

    private List<MobileWorkOrderSummaryResponse> activeWorkOrders = new ArrayList<>();

    private List<MobileAssetDocumentSummaryResponse> documents = new ArrayList<>();

    public static SyncAssetDeltaResponse from(AssetContextResponse context) {
        SyncAssetDeltaResponse response = new SyncAssetDeltaResponse();
        response.setAsset(context.getAsset());
        response.setLastInspection(context.getLastInspection());
        response.setLastMaintenance(context.getLastMaintenance());
        response.setPreventivePlan(context.getPreventivePlan());
        response.setOpenIssues(context.getOpenIssues() != null ? context.getOpenIssues() : List.of());
        response.setActiveInspections(
                context.getActiveInspections() != null ? context.getActiveInspections() : List.of());
        response.setActiveWorkOrders(
                context.getActiveWorkOrders() != null ? context.getActiveWorkOrders() : List.of());
        response.setDocuments(context.getDocuments() != null ? context.getDocuments() : List.of());
        return response;
    }

    public AssetContextSummaryResponse getAsset() {
        return asset;
    }

    public void setAsset(AssetContextSummaryResponse asset) {
        this.asset = asset;
    }

    public MobileAssetLastInspectionResponse getLastInspection() {
        return lastInspection;
    }

    public void setLastInspection(MobileAssetLastInspectionResponse lastInspection) {
        this.lastInspection = lastInspection;
    }

    public MobileAssetLastMaintenanceResponse getLastMaintenance() {
        return lastMaintenance;
    }

    public void setLastMaintenance(MobileAssetLastMaintenanceResponse lastMaintenance) {
        this.lastMaintenance = lastMaintenance;
    }

    public MobileAssetPreventivePlanResponse getPreventivePlan() {
        return preventivePlan;
    }

    public void setPreventivePlan(MobileAssetPreventivePlanResponse preventivePlan) {
        this.preventivePlan = preventivePlan;
    }

    public List<MobileIssueSummaryResponse> getOpenIssues() {
        return openIssues;
    }

    public void setOpenIssues(List<MobileIssueSummaryResponse> openIssues) {
        this.openIssues = openIssues != null ? openIssues : new ArrayList<>();
    }

    public List<MobileInspectionSummaryResponse> getActiveInspections() {
        return activeInspections;
    }

    public void setActiveInspections(List<MobileInspectionSummaryResponse> activeInspections) {
        this.activeInspections = activeInspections != null ? activeInspections : new ArrayList<>();
    }

    public List<MobileWorkOrderSummaryResponse> getActiveWorkOrders() {
        return activeWorkOrders;
    }

    public void setActiveWorkOrders(List<MobileWorkOrderSummaryResponse> activeWorkOrders) {
        this.activeWorkOrders = activeWorkOrders != null ? activeWorkOrders : new ArrayList<>();
    }

    public List<MobileAssetDocumentSummaryResponse> getDocuments() {
        return documents;
    }

    public void setDocuments(List<MobileAssetDocumentSummaryResponse> documents) {
        this.documents = documents != null ? documents : new ArrayList<>();
    }
}
