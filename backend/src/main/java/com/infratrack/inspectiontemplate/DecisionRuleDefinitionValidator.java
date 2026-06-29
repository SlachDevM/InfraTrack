package com.infratrack.inspectiontemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessValidationException;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.Set;

/**
 * Validates decision rule definitions without evaluating them (V2 Domain Engine A3.1).
 */
public final class DecisionRuleDefinitionValidator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Set<DecisionRuleOperator> BOOLEAN_OPERATORS = EnumSet.of(
            DecisionRuleOperator.IS_TRUE,
            DecisionRuleOperator.IS_FALSE
    );

    private static final Set<DecisionRuleOperator> NUMBER_OPERATORS = EnumSet.of(
            DecisionRuleOperator.GREATER_THAN,
            DecisionRuleOperator.GREATER_THAN_OR_EQUAL,
            DecisionRuleOperator.LESS_THAN,
            DecisionRuleOperator.LESS_THAN_OR_EQUAL,
            DecisionRuleOperator.EQUALS,
            DecisionRuleOperator.NOT_EQUALS
    );

    private static final Set<DecisionRuleOperator> CHOICE_OPERATORS = EnumSet.of(
            DecisionRuleOperator.EQUALS,
            DecisionRuleOperator.NOT_EQUALS
    );

    private static final Set<DecisionRuleOperator> TEXT_OPERATORS = EnumSet.of(
            DecisionRuleOperator.EQUALS,
            DecisionRuleOperator.NOT_EQUALS,
            DecisionRuleOperator.CONTAINS,
            DecisionRuleOperator.STARTS_WITH,
            DecisionRuleOperator.ENDS_WITH
    );

    private DecisionRuleDefinitionValidator() {
    }

    public static void validateRuleDefinition(
            InspectionTemplateQuestion question,
            DecisionRuleConditionType conditionType,
            DecisionRuleOperator operator,
            String comparisonValue,
            DecisionRuleActionType actionType,
            String actionPayload) {
        if (conditionType == null) {
            throw new BusinessValidationException("Condition type is required");
        }
        if (operator == null) {
            throw new BusinessValidationException("Operator is required");
        }
        if (actionType == null) {
            throw new BusinessValidationException("Action type is required");
        }

        validateConditionTypeMatchesQuestion(question, conditionType);
        validateOperatorForConditionType(conditionType, operator);
        validateComparisonValue(question, conditionType, operator, comparisonValue);
        validateActionPayload(actionPayload);
    }

    private static void validateConditionTypeMatchesQuestion(
            InspectionTemplateQuestion question,
            DecisionRuleConditionType conditionType) {
        InspectionTemplateQuestionType expected = switch (conditionType) {
            case BOOLEAN -> InspectionTemplateQuestionType.BOOLEAN;
            case NUMBER -> InspectionTemplateQuestionType.NUMBER;
            case CHOICE -> InspectionTemplateQuestionType.CHOICE;
            case TEXT -> InspectionTemplateQuestionType.TEXT;
        };
        if (question.getQuestionType() != expected) {
            throw new BusinessValidationException(
                    "Condition type " + conditionType + " does not match question type "
                            + question.getQuestionType());
        }
    }

    private static void validateOperatorForConditionType(
            DecisionRuleConditionType conditionType,
            DecisionRuleOperator operator) {
        Set<DecisionRuleOperator> allowed = switch (conditionType) {
            case BOOLEAN -> BOOLEAN_OPERATORS;
            case NUMBER -> NUMBER_OPERATORS;
            case CHOICE -> CHOICE_OPERATORS;
            case TEXT -> TEXT_OPERATORS;
        };
        if (!allowed.contains(operator)) {
            throw new BusinessValidationException(
                    "Operator " + operator + " is not supported for condition type " + conditionType);
        }
    }

    private static void validateComparisonValue(
            InspectionTemplateQuestion question,
            DecisionRuleConditionType conditionType,
            DecisionRuleOperator operator,
            String comparisonValue) {
        String normalized = comparisonValue == null ? null : comparisonValue.trim();

        if (conditionType == DecisionRuleConditionType.BOOLEAN) {
            if (normalized != null && !normalized.isBlank()) {
                throw new BusinessValidationException(
                        "Boolean decision rules must not include a comparison value");
            }
            return;
        }

        if (normalized == null || normalized.isBlank()) {
            throw new BusinessValidationException("Comparison value is required for this decision rule");
        }

        switch (conditionType) {
            case NUMBER -> validateNumericComparison(normalized);
            case TEXT -> {
                if (normalized.length() > 500) {
                    throw new BusinessValidationException("Comparison value exceeds maximum length");
                }
            }
            default -> {
            }
        }
    }

    private static void validateNumericComparison(String comparisonValue) {
        try {
            new BigDecimal(comparisonValue);
        } catch (NumberFormatException ex) {
            throw new BusinessValidationException("Comparison value must be numeric for NUMBER decision rules");
        }
    }

    public static void validateChoiceCodeExists(
            InspectionTemplateQuestionChoiceRepository choiceRepository,
            Long questionId,
            String comparisonValue) {
        String choiceCode = comparisonValue.trim().toUpperCase();
        InspectionTemplateQuestionChoice choice = choiceRepository.findByQuestionIdAndCode(questionId, choiceCode)
                .orElseThrow(() -> new BusinessValidationException(
                        "Comparison value must reference an active choice code for this question"));
        if (!choice.isActive()) {
            throw new BusinessValidationException(
                    "Comparison value must reference an active choice code for this question");
        }
    }

    public static void validateActionPayload(String actionPayload) {
        if (actionPayload == null || actionPayload.isBlank()) {
            return;
        }
        try {
            OBJECT_MAPPER.readTree(actionPayload);
        } catch (JsonProcessingException ex) {
            throw new BusinessValidationException("Action payload must be valid JSON");
        }
    }

    public static String normalizeOptionalDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    public static String normalizeOptionalActionPayload(String actionPayload) {
        if (actionPayload == null || actionPayload.isBlank()) {
            return null;
        }
        return actionPayload.trim();
    }

    public static String normalizeComparisonValue(
            DecisionRuleConditionType conditionType,
            String comparisonValue) {
        if (conditionType == DecisionRuleConditionType.BOOLEAN) {
            return null;
        }
        if (comparisonValue == null || comparisonValue.isBlank()) {
            return null;
        }
        String normalized = comparisonValue.trim();
        if (conditionType == DecisionRuleConditionType.CHOICE) {
            return normalized.toUpperCase();
        }
        return normalized;
    }
}
