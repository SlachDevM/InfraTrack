package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;

public class InspectionTemplateQuestionChoiceResponse {

    private Long id;
    private Long questionId;
    private String code;
    private String label;
    private Integer displayOrder;
    private boolean active;
    private Long createdAt;
    private Long updatedAt;

    public static InspectionTemplateQuestionChoiceResponse from(InspectionTemplateQuestionChoice choice) {
        InspectionTemplateQuestionChoiceResponse response = new InspectionTemplateQuestionChoiceResponse();
        response.id = choice.getId();
        response.questionId = choice.getQuestion().getId();
        response.code = choice.getCode();
        response.label = choice.getLabel();
        response.displayOrder = choice.getDisplayOrder();
        response.active = choice.isActive();
        response.createdAt = choice.getCreatedAt();
        response.updatedAt = choice.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public boolean isActive() {
        return active;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }
}
