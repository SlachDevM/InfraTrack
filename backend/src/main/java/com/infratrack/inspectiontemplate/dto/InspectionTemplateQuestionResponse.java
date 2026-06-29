package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;

public class InspectionTemplateQuestionResponse {

    private Long id;
    private Long inspectionTemplateId;
    private String code;
    private String questionText;
    private String helpText;
    private InspectionTemplateQuestionType questionType;
    private boolean required;
    private Integer displayOrder;
    private boolean active;
    private Long createdAt;
    private Long updatedAt;

    public static InspectionTemplateQuestionResponse from(InspectionTemplateQuestion question) {
        InspectionTemplateQuestionResponse response = new InspectionTemplateQuestionResponse();
        response.id = question.getId();
        response.inspectionTemplateId = question.getInspectionTemplate().getId();
        response.code = question.getCode();
        response.questionText = question.getQuestionText();
        response.helpText = question.getHelpText();
        response.questionType = question.getQuestionType();
        response.required = question.isRequired();
        response.displayOrder = question.getDisplayOrder();
        response.active = question.isActive();
        response.createdAt = question.getCreatedAt();
        response.updatedAt = question.getUpdatedAt();
        return response;
    }

    public Long getId() {
        return id;
    }

    public Long getInspectionTemplateId() {
        return inspectionTemplateId;
    }

    public String getCode() {
        return code;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getHelpText() {
        return helpText;
    }

    public InspectionTemplateQuestionType getQuestionType() {
        return questionType;
    }

    public boolean isRequired() {
        return required;
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
