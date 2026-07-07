package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.ArrayList;
import java.util.List;

/**
 * Server-authoritative reference data snapshot for mobile offline display (M6.5-BE1).
 */
public class SyncReferenceDataDeltaResponse {

    public static final int CURRENT_SCHEMA_VERSION = 1;

    @Schema(description = "Snapshot generation time (sync watermark epoch millis)")
    private Long generatedAt;

    @Schema(description = "Reference data payload schema version")
    private int schemaVersion = CURRENT_SCHEMA_VERSION;

    private List<SyncReferenceItemResponse> assetCategories = new ArrayList<>();

    private List<SyncReferenceItemResponse> departments = new ArrayList<>();

    private List<SyncEnumItemResponse> workOrderTypes = new ArrayList<>();

    private List<SyncEnumItemResponse> inspectionStatuses = new ArrayList<>();

    private List<SyncEnumItemResponse> inspectionPriorities = new ArrayList<>();

    private List<SyncEnumItemResponse> workOrderStatuses = new ArrayList<>();

    private List<SyncEnumItemResponse> workOrderPriorities = new ArrayList<>();

    private List<SyncEnumItemResponse> assetStatuses = new ArrayList<>();

    private List<SyncEnumItemResponse> issueSeverities = new ArrayList<>();

    public Long getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Long generatedAt) {
        this.generatedAt = generatedAt;
    }

    public int getSchemaVersion() {
        return schemaVersion;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion;
    }

    public List<SyncReferenceItemResponse> getAssetCategories() {
        return assetCategories;
    }

    public void setAssetCategories(List<SyncReferenceItemResponse> assetCategories) {
        this.assetCategories = assetCategories != null ? assetCategories : new ArrayList<>();
    }

    public List<SyncReferenceItemResponse> getDepartments() {
        return departments;
    }

    public void setDepartments(List<SyncReferenceItemResponse> departments) {
        this.departments = departments != null ? departments : new ArrayList<>();
    }

    public List<SyncEnumItemResponse> getWorkOrderTypes() {
        return workOrderTypes;
    }

    public void setWorkOrderTypes(List<SyncEnumItemResponse> workOrderTypes) {
        this.workOrderTypes = workOrderTypes != null ? workOrderTypes : new ArrayList<>();
    }

    public List<SyncEnumItemResponse> getInspectionStatuses() {
        return inspectionStatuses;
    }

    public void setInspectionStatuses(List<SyncEnumItemResponse> inspectionStatuses) {
        this.inspectionStatuses = inspectionStatuses != null ? inspectionStatuses : new ArrayList<>();
    }

    public List<SyncEnumItemResponse> getInspectionPriorities() {
        return inspectionPriorities;
    }

    public void setInspectionPriorities(List<SyncEnumItemResponse> inspectionPriorities) {
        this.inspectionPriorities = inspectionPriorities != null ? inspectionPriorities : new ArrayList<>();
    }

    public List<SyncEnumItemResponse> getWorkOrderStatuses() {
        return workOrderStatuses;
    }

    public void setWorkOrderStatuses(List<SyncEnumItemResponse> workOrderStatuses) {
        this.workOrderStatuses = workOrderStatuses != null ? workOrderStatuses : new ArrayList<>();
    }

    public List<SyncEnumItemResponse> getWorkOrderPriorities() {
        return workOrderPriorities;
    }

    public void setWorkOrderPriorities(List<SyncEnumItemResponse> workOrderPriorities) {
        this.workOrderPriorities = workOrderPriorities != null ? workOrderPriorities : new ArrayList<>();
    }

    public List<SyncEnumItemResponse> getAssetStatuses() {
        return assetStatuses;
    }

    public void setAssetStatuses(List<SyncEnumItemResponse> assetStatuses) {
        this.assetStatuses = assetStatuses != null ? assetStatuses : new ArrayList<>();
    }

    public List<SyncEnumItemResponse> getIssueSeverities() {
        return issueSeverities;
    }

    public void setIssueSeverities(List<SyncEnumItemResponse> issueSeverities) {
        this.issueSeverities = issueSeverities != null ? issueSeverities : new ArrayList<>();
    }
}
