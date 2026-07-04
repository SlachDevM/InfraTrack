package com.infratrack.mobile.dto;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;

public class AssetContextSummaryResponse {

    private Long id;
    private String code;
    private String name;
    private String category;
    private String department;
    private String location;
    private AssetStatus status;

    public static AssetContextSummaryResponse from(Asset asset) {
        AssetContextSummaryResponse response = new AssetContextSummaryResponse();
        response.id = asset.getId();
        response.code = asset.getCode();
        response.name = asset.getName();
        if (asset.getAssetCategory() != null) {
            response.category = asset.getAssetCategory().getName();
        }
        if (asset.getDepartment() != null) {
            response.department = asset.getDepartment().getName();
        }
        response.location = asset.getLocation();
        response.status = asset.getStatus();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getDepartment() {
        return department;
    }

    public String getLocation() {
        return location;
    }

    public AssetStatus getStatus() {
        return status;
    }
}
