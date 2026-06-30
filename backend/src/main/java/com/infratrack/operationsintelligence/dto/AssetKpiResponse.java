package com.infratrack.operationsintelligence.dto;

import java.util.Map;

public class AssetKpiResponse {

    private long totalAssets;
    private Map<Long, Long> assetsByDepartment;
    private Map<Long, Long> assetsByCategory;
    private long assetsWithoutCategory;
    private long assetsWithoutDepartment;

    public long getTotalAssets() {
        return totalAssets;
    }

    public void setTotalAssets(long totalAssets) {
        this.totalAssets = totalAssets;
    }

    public Map<Long, Long> getAssetsByDepartment() {
        return assetsByDepartment;
    }

    public void setAssetsByDepartment(Map<Long, Long> assetsByDepartment) {
        this.assetsByDepartment = assetsByDepartment;
    }

    public Map<Long, Long> getAssetsByCategory() {
        return assetsByCategory;
    }

    public void setAssetsByCategory(Map<Long, Long> assetsByCategory) {
        this.assetsByCategory = assetsByCategory;
    }

    public long getAssetsWithoutCategory() {
        return assetsWithoutCategory;
    }

    public void setAssetsWithoutCategory(long assetsWithoutCategory) {
        this.assetsWithoutCategory = assetsWithoutCategory;
    }

    public long getAssetsWithoutDepartment() {
        return assetsWithoutDepartment;
    }

    public void setAssetsWithoutDepartment(long assetsWithoutDepartment) {
        this.assetsWithoutDepartment = assetsWithoutDepartment;
    }
}
