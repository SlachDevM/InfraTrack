package com.infratrack.assetcategory.dto;

import com.infratrack.assetcategory.AssetCategory;

public class AssetCategoryResponse {

    private Long id;
    private String name;
    private Long createdAt;
    private Long updatedAt;

    public AssetCategoryResponse() {
    }

    public AssetCategoryResponse(Long id, String name, Long createdAt, Long updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static AssetCategoryResponse from(AssetCategory category) {
        return new AssetCategoryResponse(
                category.getId(),
                category.getName(),
                category.getCreatedAt(),
                category.getUpdatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
