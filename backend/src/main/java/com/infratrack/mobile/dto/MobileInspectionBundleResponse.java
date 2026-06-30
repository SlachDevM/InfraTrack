package com.infratrack.mobile.dto;

import java.util.List;

public class MobileInspectionBundleResponse {

    private MobileInspectionDetailResponse inspection;
    private MobileAssetSummaryResponse asset;
    private MobileTemplateSummaryResponse template;
    private List<MobileQuestionResponse> questions;
    private List<MobileAnswerResponse> answers;
    private MobileAllowedActionsResponse allowedActions;

    public MobileInspectionBundleResponse(
            MobileInspectionDetailResponse inspection,
            MobileAssetSummaryResponse asset,
            MobileTemplateSummaryResponse template,
            List<MobileQuestionResponse> questions,
            List<MobileAnswerResponse> answers,
            MobileAllowedActionsResponse allowedActions) {
        this.inspection = inspection;
        this.asset = asset;
        this.template = template;
        this.questions = questions;
        this.answers = answers;
        this.allowedActions = allowedActions;
    }

    public MobileInspectionDetailResponse getInspection() {
        return inspection;
    }

    public MobileAssetSummaryResponse getAsset() {
        return asset;
    }

    public MobileTemplateSummaryResponse getTemplate() {
        return template;
    }

    public List<MobileQuestionResponse> getQuestions() {
        return questions;
    }

    public List<MobileAnswerResponse> getAnswers() {
        return answers;
    }

    public MobileAllowedActionsResponse getAllowedActions() {
        return allowedActions;
    }
}
