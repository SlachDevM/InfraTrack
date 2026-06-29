package com.infratrack.preventivemaintenance.dto;

import jakarta.validation.constraints.Size;

public class RejectPreventiveCandidateRequest {

    @Size(max = 4000)
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
