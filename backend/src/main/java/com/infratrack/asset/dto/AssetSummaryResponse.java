package com.infratrack.asset.dto;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;

import java.time.LocalDate;

public class AssetSummaryResponse {

    private Long id;
    private String name;
    private Long departmentId;
    private String departmentName;
    private Long assetCategoryId;
    private String assetCategoryName;
    private String location;
    private AssetStatus status;
    private LocalDate registrationDate;

    public static AssetSummaryResponse from(Asset asset) {
        AssetSummaryResponse response = new AssetSummaryResponse();
        response.id = asset.getId();
        response.name = asset.getName();
        response.departmentId = asset.getDepartment().getId();
        response.departmentName = asset.getDepartment().getName();
        response.assetCategoryId = asset.getAssetCategory().getId();
        response.assetCategoryName = asset.getAssetCategory().getName();
        response.location = asset.getLocation();
        response.status = asset.getStatus();
        response.registrationDate = asset.getRegistrationDate();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getDepartmentId() {
        return departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public Long getAssetCategoryId() {
        return assetCategoryId;
    }

    public String getAssetCategoryName() {
        return assetCategoryName;
    }

    public String getLocation() {
        return location;
    }

    public AssetStatus getStatus() {
        return status;
    }

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }
}
