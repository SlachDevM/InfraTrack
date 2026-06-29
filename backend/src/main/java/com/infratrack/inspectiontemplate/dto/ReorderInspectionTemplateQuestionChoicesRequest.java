package com.infratrack.inspectiontemplate.dto;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReorderInspectionTemplateQuestionChoicesRequest {

    @NotEmpty
    private List<Long> orderedChoiceIds;

    public List<Long> getOrderedChoiceIds() {
        return orderedChoiceIds;
    }

    public void setOrderedChoiceIds(List<Long> orderedChoiceIds) {
        this.orderedChoiceIds = orderedChoiceIds;
    }
}
