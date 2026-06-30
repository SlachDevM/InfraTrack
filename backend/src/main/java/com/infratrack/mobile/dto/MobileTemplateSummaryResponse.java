package com.infratrack.mobile.dto;

import com.infratrack.inspectiontemplate.InspectionTemplate;

public class MobileTemplateSummaryResponse {

    private Long id;
    private String name;
    private Integer version;
    private String status;

    public static MobileTemplateSummaryResponse from(InspectionTemplate template) {
        MobileTemplateSummaryResponse response = new MobileTemplateSummaryResponse();
        response.id = template.getId();
        response.name = template.getName();
        response.version = template.getVersion();
        response.status = template.getStatus() != null ? template.getStatus().name() : null;
        return response;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getVersion() {
        return version;
    }

    public String getStatus() {
        return status;
    }
}
