package com.infratrack.mobile.sync.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Compact reference data item for mobile offline display (M6.5-BE1).
 */
public class SyncReferenceItemResponse {

    @Schema(description = "Reference record id")
    private Long id;

    @Schema(description = "Optional business code when available on the source record")
    private String code;

    @Schema(description = "Display label")
    private String label;

    @Schema(description = "Whether the record is active when the source model exposes it")
    private Boolean active;

    public SyncReferenceItemResponse() {
    }

    public SyncReferenceItemResponse(Long id, String label) {
        this.id = id;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
