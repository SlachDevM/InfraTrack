package com.infratrack.mobile.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;

public class MobileChoiceResponse {

    private String code;
    private String label;
    private Integer displayOrder;

    public static MobileChoiceResponse from(InspectionTemplateQuestionChoice choice) {
        MobileChoiceResponse response = new MobileChoiceResponse();
        response.code = choice.getCode();
        response.label = choice.getLabel();
        response.displayOrder = choice.getDisplayOrder();
        return response;
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
}
