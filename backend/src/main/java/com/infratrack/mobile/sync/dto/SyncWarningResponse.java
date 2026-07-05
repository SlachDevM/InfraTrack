package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class SyncWarningResponse {

    @Schema(description = "Machine-readable warning code", example = "SYNC_TOKEN_EXPIRED")
    private SyncWarningCode code;

    @Schema(description = "Human-readable warning detail")
    private String message;

    public SyncWarningResponse() {
    }

    public SyncWarningResponse(SyncWarningCode code, String message) {
        this.code = code;
        this.message = message;
    }

    public SyncWarningCode getCode() {
        return code;
    }

    public void setCode(SyncWarningCode code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
