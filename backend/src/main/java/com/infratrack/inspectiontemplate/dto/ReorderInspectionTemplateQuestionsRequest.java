package com.infratrack.inspectiontemplate.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReorderInspectionTemplateQuestionsRequest {

    @NotEmpty
    private List<Long> orderedQuestionIds;

    public List<Long> getOrderedQuestionIds() {
        return orderedQuestionIds;
    }

    public void setOrderedQuestionIds(List<Long> orderedQuestionIds) {
        this.orderedQuestionIds = orderedQuestionIds;
    }
}
