package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SyncInspectionAssetDeltaResponse {

    @Schema(description = "Asset identifier")
    private Long assetId;

    @Schema(description = "Asset display name")
    private String assetName;

    @Schema(description = "Asset category name when available")
    private String assetCategoryName;

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public void setAssetName(String assetName) {
        this.assetName = assetName;
    }

    public String getAssetCategoryName() {
        return assetCategoryName;
    }

    public void setAssetCategoryName(String assetCategoryName) {
        this.assetCategoryName = assetCategoryName;
    }
}
