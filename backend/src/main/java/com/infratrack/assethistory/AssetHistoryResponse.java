package com.infratrack.assethistory;

import com.infratrack.asset.AssetHistoryEventType;

import java.time.LocalDate;

public class AssetHistoryResponse {

    private LocalDate eventDate;
    private AssetHistoryEventType eventType;
    private Long responsibleUserId;
    private String responsibleUserName;
    private String details;

    public AssetHistoryResponse(
            LocalDate eventDate,
            AssetHistoryEventType eventType,
            Long responsibleUserId,
            String responsibleUserName,
            String details) {
        this.eventDate = eventDate;
        this.eventType = eventType;
        this.responsibleUserId = responsibleUserId;
        this.responsibleUserName = responsibleUserName;
        this.details = details;
    }

    public LocalDate getEventDate() {
        return eventDate;
    }

    public AssetHistoryEventType getEventType() {
        return eventType;
    }

    public Long getResponsibleUserId() {
        return responsibleUserId;
    }

    public String getResponsibleUserName() {
        return responsibleUserName;
    }

    public String getDetails() {
        return details;
    }
}
