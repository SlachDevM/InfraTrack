package com.infratrack.mobile.sync.dto;

import com.infratrack.inspectiontemplate.InspectionTemplate;
import io.swagger.v3.oas.annotations.media.Schema;

public class SyncInspectionTemplateDeltaResponse {

    @Schema(description = "Inspection template identifier")
    private Long templateId;

    private String templateName;

    private Integer templateVersion;

    public static SyncInspectionTemplateDeltaResponse from(InspectionTemplate template) {
        SyncInspectionTemplateDeltaResponse response = new SyncInspectionTemplateDeltaResponse();
        response.setTemplateId(template.getId());
        response.setTemplateName(template.getName());
        response.setTemplateVersion(template.getVersion());
        return response;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public Integer getTemplateVersion() {
        return templateVersion;
    }

    public void setTemplateVersion(Integer templateVersion) {
        this.templateVersion = templateVersion;
    }
}
