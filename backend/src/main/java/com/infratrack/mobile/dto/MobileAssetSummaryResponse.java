package com.infratrack.mobile.dto;

import com.infratrack.asset.Asset;

public class MobileAssetSummaryResponse {

    private Long id;
    private String name;
    private String category;
    private String department;
    private String location;

    public static MobileAssetSummaryResponse from(Asset asset) {
        MobileAssetSummaryResponse response = new MobileAssetSummaryResponse();
        response.id = asset.getId();
        response.name = asset.getName();
        response.location = asset.getLocation();
        if (asset.getAssetCategory() != null) {
            response.category = asset.getAssetCategory().getName();
        }
        if (asset.getDepartment() != null) {
            response.department = asset.getDepartment().getName();
        }
        return response;
    }

    public Long getId() {
        return id;
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
}
