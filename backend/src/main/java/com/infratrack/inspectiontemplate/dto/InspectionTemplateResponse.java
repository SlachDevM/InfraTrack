package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;

public class InspectionTemplateResponse {

    private Long id;
    private String name;
    private String description;
    private Long assetCategoryId;
    private String assetCategoryName;
    private Integer version;
    private InspectionTemplateStatus status;
    private Long createdAt;
    private Long updatedAt;

    public static InspectionTemplateResponse from(InspectionTemplate template) {
        InspectionTemplateResponse response = new InspectionTemplateResponse();
        response.id = template.getId();
        response.name = template.getName();
        response.description = template.getDescription();
        response.assetCategoryId = template.getAssetCategory().getId();
        response.assetCategoryName = template.getAssetCategory().getName();
        response.version = template.getVersion();
        response.status = template.getStatus();
        response.createdAt = template.getCreatedAt();
        response.updatedAt = template.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Long getAssetCategoryId() {
        return assetCategoryId;
    }

    public String getAssetCategoryName() {
        return assetCategoryName;
    }

    public Integer getVersion() {
        return version;
    }

    public InspectionTemplateStatus getStatus() {
        return status;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
