package com.infratrack.suggestedaction.dto;

import jakarta.validation.constraints.Size;

public class RejectSuggestedActionRequest {

    @Size(max = 2000)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
