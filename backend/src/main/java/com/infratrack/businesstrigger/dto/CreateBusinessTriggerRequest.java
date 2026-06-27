package com.infratrack.businesstrigger.dto;

import com.infratrack.businesstrigger.BusinessTriggerType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class CreateBusinessTriggerRequest {

    @NotNull
    @Positive
    private Long assetId;

    @NotNull
    private BusinessTriggerType type;

    @NotBlank
    @Size(max = 4000)
    private String reason;

    private Boolean urgent;

    public Long getAssetId() {
        return assetId;
    }

    public void setAssetId(Long assetId) {
        this.assetId = assetId;
    }

    public BusinessTriggerType getType() {
        return type;
    }

    public void setType(BusinessTriggerType type) {
        this.type = type;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Boolean getUrgent() {
        return urgent;
    }

    public void setUrgent(Boolean urgent) {
        this.urgent = urgent;
    }
}
