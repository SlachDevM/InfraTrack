package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Incremental download envelope for mobile sync (M5.2-BE2+).
 */
public class SyncDeltaResponse {

    @Schema(description = "Asset context records for assets linked to scoped sync operations (M6.3-BE1+)")
    private List<SyncAssetDeltaResponse> assets = new ArrayList<>();

    @Schema(description = "Changed or assigned inspection sync records (M5.4-BE+)")
    private List<SyncInspectionDeltaResponse> inspections = new ArrayList<>();

    @Schema(description = "Changed or assigned work order sync records (M6.1-BE2+)")
    private List<SyncWorkOrderDeltaResponse> workOrders = new ArrayList<>();

    @Schema(description = "Server-computed mobile dashboard snapshot (M6.2-BE1+)")
    private SyncDashboardDeltaResponse dashboard;

    @Schema(description = "Server-authoritative reference data snapshot (M6.5-BE1+)")
    private SyncReferenceDataDeltaResponse referenceData;

    @Schema(description = "Changed document metadata (future)")
    private List<Object> documents = new ArrayList<>();

    @Schema(description = "Changed user profile data (future)")
    private List<Object> users = new ArrayList<>();

    public static SyncDeltaResponse empty() {
        return new SyncDeltaResponse();
    }

    public List<SyncAssetDeltaResponse> getAssets() {
        return assets;
    }

    public void setAssets(List<SyncAssetDeltaResponse> assets) {
        this.assets = assets != null ? assets : new ArrayList<>();
    }

    public List<SyncInspectionDeltaResponse> getInspections() {
        return inspections;
    }

    public void setInspections(List<SyncInspectionDeltaResponse> inspections) {
        this.inspections = inspections != null ? inspections : new ArrayList<>();
    }

    public List<SyncWorkOrderDeltaResponse> getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(List<SyncWorkOrderDeltaResponse> workOrders) {
        this.workOrders = workOrders != null ? workOrders : new ArrayList<>();
    }

    public SyncDashboardDeltaResponse getDashboard() {
        return dashboard;
    }

    public void setDashboard(SyncDashboardDeltaResponse dashboard) {
        this.dashboard = dashboard;
    }

    public SyncReferenceDataDeltaResponse getReferenceData() {
        return referenceData;
    }

    public void setReferenceData(SyncReferenceDataDeltaResponse referenceData) {
        this.referenceData = referenceData;
    }

    public List<Object> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Object> documents) {
        this.documents = documents != null ? documents : new ArrayList<>();
    }

    public List<Object> getUsers() {
        return users;
    }

    public void setUsers(List<Object> users) {
        this.users = users != null ? users : new ArrayList<>();
    }
}
