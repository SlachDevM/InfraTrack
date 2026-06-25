package com.infratrack.businesstrigger.dto;

import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;

public class BusinessTriggerResponse {

    private Long id;
    private Long assetId;
    private String assetName;
    private BusinessTriggerType type;
    private String reason;
    private boolean urgent;
    private Long createdByUserId;
    private Long createdAt;
    private Long updatedAt;

    public static BusinessTriggerResponse from(BusinessTrigger trigger) {
        BusinessTriggerResponse response = new BusinessTriggerResponse();
        response.id = trigger.getId();
        response.assetId = trigger.getAsset().getId();
        response.assetName = trigger.getAsset().getName();
        response.type = trigger.getType();
        response.reason = trigger.getReason();
        response.urgent = trigger.isUrgent();
        response.createdByUserId = trigger.getCreatedByUserId();
        response.createdAt = trigger.getCreatedAt();
        response.updatedAt = trigger.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getAssetId() {
        return assetId;
    }

    public String getAssetName() {
        return assetName;
    }

    public BusinessTriggerType getType() {
        return type;
    }

    public String getReason() {
        return reason;
    }

    public boolean isUrgent() {
        return urgent;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
