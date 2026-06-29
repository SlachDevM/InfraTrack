package com.infratrack.inspectiontemplate;

import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerQuestionTypeSnapshot;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspectiontemplate.dto.DecisionRuleEvaluationResult;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Evaluates decision rules against inspection answers (V2 Domain Engine A3.2).
 * Results are in-memory only — not persisted and no workflow side effects.
 */
@Service
public class DecisionRuleEvaluationService {

    private final InspectionRepository inspectionRepository;
    private final InspectionAnswerRepository answerRepository;
    private final InspectionTemplateQuestionRuleRepository ruleRepository;

    public DecisionRuleEvaluationService(
            InspectionRepository inspectionRepository,
            InspectionAnswerRepository answerRepository,
            InspectionTemplateQuestionRuleRepository ruleRepository) {
        this.inspectionRepository = inspectionRepository;
        this.answerRepository = answerRepository;
        this.ruleRepository = ruleRepository;
    }

    @Transactional(readOnly = true)
    public List<DecisionRuleEvaluationResult> evaluateInspection(Long inspectionId) {
        if (!inspectionRepository.existsById(inspectionId)) {
            throw new NotFoundException("Inspection not found");
        }

        List<InspectionAnswer> answers =
                answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(inspectionId);
        List<DecisionRuleEvaluationResult> results = new ArrayList<>();
        for (InspectionAnswer answer : answers) {
            List<InspectionTemplateQuestionRule> rules = ruleRepository
                    .findByQuestionIdAndActiveTrueOrderByPriorityAscRuleCodeAsc(answer.getQuestion().getId());
            results.addAll(evaluateAnswer(answer, rules));
        }
        return results;
    }

    public List<DecisionRuleEvaluationResult> evaluateAnswer(
            InspectionAnswer answer,
            List<InspectionTemplateQuestionRule> rules) {
        if (answer == null || rules == null) {
            return List.of();
        }

        Long questionId = answer.getQuestion().getId();
        return rules.stream()
                .filter(InspectionTemplateQuestionRule::isActive)
                .filter(rule -> Objects.equals(rule.getQuestion().getId(), questionId))
                .sorted(Comparator
                        .comparingInt(InspectionTemplateQuestionRule::getPriority)
                        .thenComparing(InspectionTemplateQuestionRule::getRuleCode))
                .map(rule -> evaluateRule(answer, rule))
                .toList();
    }

    private DecisionRuleEvaluationResult evaluateRule(
            InspectionAnswer answer,
            InspectionTemplateQuestionRule rule) {
        DecisionRuleEvaluationResult result = new DecisionRuleEvaluationResult();
        result.setRuleId(rule.getId());
        result.setRuleCode(rule.getRuleCode());
        result.setRuleName(rule.getRuleName());
        result.setConditionType(rule.getConditionType());
        result.setOperator(rule.getOperator());
        result.setComparisonValue(rule.getComparisonValue());
        result.setActionType(rule.getActionType());
        result.setActionPayload(rule.getActionPayload());
        result.setPriority(rule.getPriority());

        String actualValue = extractActualValue(answer, rule.getConditionType());
        result.setActualValue(actualValue);
        result.setMatched(matches(answer, rule, actualValue));
        return result;
    }

    private boolean matches(
            InspectionAnswer answer,
            InspectionTemplateQuestionRule rule,
            String actualValue) {
        if (!conditionTypeMatchesAnswer(rule.getConditionType(), answer.getQuestionTypeSnapshot())) {
            return false;
        }
        if (actualValue == null) {
            return false;
        }

        return switch (rule.getConditionType()) {
            case BOOLEAN -> evaluateBoolean(answer.getBooleanValue(), rule.getOperator());
            case NUMBER -> evaluateNumber(actualValue, rule.getComparisonValue(), rule.getOperator());
            case CHOICE -> evaluateChoice(actualValue, rule.getComparisonValue(), rule.getOperator());
            case TEXT -> evaluateText(actualValue, rule.getComparisonValue(), rule.getOperator());
        };
    }

    private static boolean conditionTypeMatchesAnswer(
            DecisionRuleConditionType conditionType,
            InspectionAnswerQuestionTypeSnapshot answerType) {
        return switch (conditionType) {
            case BOOLEAN -> answerType == InspectionAnswerQuestionTypeSnapshot.BOOLEAN;
            case NUMBER -> answerType == InspectionAnswerQuestionTypeSnapshot.NUMBER;
            case CHOICE -> answerType == InspectionAnswerQuestionTypeSnapshot.CHOICE;
            case TEXT -> answerType == InspectionAnswerQuestionTypeSnapshot.TEXT;
        };
    }

    private static String extractActualValue(
            InspectionAnswer answer,
            DecisionRuleConditionType conditionType) {
        return switch (conditionType) {
            case BOOLEAN -> answer.getBooleanValue() == null
                    ? null
                    : answer.getBooleanValue().toString();
            case NUMBER -> answer.getNumberValue() == null
                    ? null
                    : answer.getNumberValue().toPlainString();
            case CHOICE -> blankToNull(answer.getChoiceCodeValue());
            case TEXT -> blankToNull(answer.getTextValue());
        };
    }

    private static boolean evaluateBoolean(Boolean value, DecisionRuleOperator operator) {
        if (value == null) {
            return false;
        }
        return switch (operator) {
            case IS_TRUE -> value;
            case IS_FALSE -> !value;
            default -> false;
        };
    }

    private static boolean evaluateNumber(
            String actualValue,
            String comparisonValue,
            DecisionRuleOperator operator) {
        if (comparisonValue == null || comparisonValue.isBlank()) {
            return false;
        }
        try {
            BigDecimal actual = new BigDecimal(actualValue);
            BigDecimal expected = new BigDecimal(comparisonValue.trim());
            int comparison = actual.compareTo(expected);
            return switch (operator) {
                case GREATER_THAN -> comparison > 0;
                case GREATER_THAN_OR_EQUAL -> comparison >= 0;
                case LESS_THAN -> comparison < 0;
                case LESS_THAN_OR_EQUAL -> comparison <= 0;
                case EQUALS -> comparison == 0;
                case NOT_EQUALS -> comparison != 0;
                default -> false;
            };
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private static boolean evaluateChoice(
            String actualCode,
            String comparisonValue,
            DecisionRuleOperator operator) {
        if (comparisonValue == null || comparisonValue.isBlank()) {
            return false;
        }
        boolean equals = actualCode.equals(comparisonValue.trim());
        return switch (operator) {
            case EQUALS -> equals;
            case NOT_EQUALS -> !equals;
            default -> false;
        };
    }

    private static boolean evaluateText(
            String actualValue,
            String comparisonValue,
            DecisionRuleOperator operator) {
        if (comparisonValue == null || comparisonValue.isBlank()) {
            return false;
        }
        String actual = actualValue.toLowerCase(Locale.ROOT);
        String expected = comparisonValue.trim().toLowerCase(Locale.ROOT);
        return switch (operator) {
            case EQUALS -> actual.equals(expected);
            case NOT_EQUALS -> !actual.equals(expected);
            case CONTAINS -> actual.contains(expected);
            case STARTS_WITH -> actual.startsWith(expected);
            case ENDS_WITH -> actual.endsWith(expected);
            default -> false;
        };
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value;
    }
}
