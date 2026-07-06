package com.infratrack.mobile.sync.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import io.swagger.v3.oas.annotations.media.Schema;

public class SyncInspectionChoiceDeltaResponse {

    @Schema(description = "Choice identifier")
    private Long choiceId;

    private String code;

    private String label;

    private Integer displayOrder;

    private boolean active;

    public static SyncInspectionChoiceDeltaResponse from(InspectionTemplateQuestionChoice choice) {
        SyncInspectionChoiceDeltaResponse response = new SyncInspectionChoiceDeltaResponse();
        response.setChoiceId(choice.getId());
        response.setCode(choice.getCode());
        response.setLabel(choice.getLabel());
        response.setDisplayOrder(choice.getDisplayOrder());
        response.setActive(choice.isActive());
        return response;
    }

    public Long getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(Long choiceId) {
        this.choiceId = choiceId;
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

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}
