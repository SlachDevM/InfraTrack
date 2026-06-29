package com.infratrack.inspectiontemplate;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DecisionRuleDefinitionValidatorTest {

    @Test
    void validateRuleDefinition_shouldRequireTextComparisonValue() {
        InspectionTemplateQuestion question = textQuestion();

        assertThatThrownBy(() -> DecisionRuleDefinitionValidator.validateRuleDefinition(
                question,
                DecisionRuleConditionType.TEXT,
                DecisionRuleOperator.CONTAINS,
                " ",
                DecisionRuleActionType.FLAG_FOR_REVIEW,
                null))
                .isInstanceOf(com.infratrack.exception.BusinessValidationException.class)
                .hasMessage("Comparison value is required for this decision rule");
    }
    
    private InspectionTemplateQuestion textQuestion() {
        com.infratrack.assetcategory.AssetCategory category = new com.infratrack.assetcategory.AssetCategory("Pump");
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection", null, category, 1, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Notes", "NOTES", null,
                InspectionTemplateQuestionType.TEXT, false, 1);
        question.setId(1L);
        return question;
    }
}
