package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Enum dictionary entry for mobile offline display (M6.5-BE1).
 */
public class SyncEnumItemResponse {

    @Schema(description = "Stable enum code")
    private String code;

    @Schema(description = "Server-authoritative display label")
    private String label;

    public SyncEnumItemResponse() {
    }

    public SyncEnumItemResponse(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
