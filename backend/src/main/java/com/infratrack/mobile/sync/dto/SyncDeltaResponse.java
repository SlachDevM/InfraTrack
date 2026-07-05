package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Future incremental download envelope. All sections are empty until delta sync is implemented (M5.2-BE2+).
 */
public class SyncDeltaResponse {

    @Schema(description = "Changed or assigned asset summaries (future)")
    private List<Object> assets = new ArrayList<>();

    @Schema(description = "Changed or assigned inspection summaries (future)")
    private List<Object> inspections = new ArrayList<>();

    @Schema(description = "Changed or assigned work order summaries (future)")
    private List<Object> workOrders = new ArrayList<>();

    @Schema(description = "Changed document metadata (future)")
    private List<Object> documents = new ArrayList<>();

    @Schema(description = "Changed user profile data (future)")
    private List<Object> users = new ArrayList<>();

    @Schema(description = "Changed reference data snapshots (future)")
    private List<Object> referenceData = new ArrayList<>();

    public static SyncDeltaResponse empty() {
        return new SyncDeltaResponse();
    }

    public List<Object> getAssets() {
        return assets;
    }

    public void setAssets(List<Object> assets) {
        this.assets = assets != null ? assets : new ArrayList<>();
    }

    public List<Object> getInspections() {
        return inspections;
    }

    public void setInspections(List<Object> inspections) {
        this.inspections = inspections != null ? inspections : new ArrayList<>();
    }

    public List<Object> getWorkOrders() {
        return workOrders;
    }

    public void setWorkOrders(List<Object> workOrders) {
        this.workOrders = workOrders != null ? workOrders : new ArrayList<>();
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

    public List<Object> getReferenceData() {
        return referenceData;
    }

    public void setReferenceData(List<Object> referenceData) {
        this.referenceData = referenceData != null ? referenceData : new ArrayList<>();
    }
}
