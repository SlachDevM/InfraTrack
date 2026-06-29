package com.infratrack.inspectiontemplate;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerQuestionTypeSnapshot;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspectiontemplate.dto.DecisionRuleEvaluationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionRuleEvaluationServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionAnswerRepository answerRepository;

    @Mock
    private InspectionTemplateQuestionRuleRepository ruleRepository;

    private DecisionRuleEvaluationService evaluationService;
    private InspectionTemplate template;
    private Inspection inspection;

    @BeforeEach
    void setUp() {
        evaluationService = new DecisionRuleEvaluationService(
                inspectionRepository, answerRepository, ruleRepository);
        template = template();
        inspection = inspection();
    }

    @Test
    void evaluateAnswer_booleanIsTrue_matchesTrueAnswer() {
        InspectionTemplateQuestion question = booleanQuestion(1L);
        InspectionAnswer answer = booleanAnswer(question, true);
        InspectionTemplateQuestionRule rule = rule(
                question, "LEAK_TRUE", DecisionRuleConditionType.BOOLEAN,
                DecisionRuleOperator.IS_TRUE, null, 100);

        List<DecisionRuleEvaluationResult> results = evaluationService.evaluateAnswer(contextFor(answer), List.of(rule));

        assertThat(results).hasSize(1);
        assertThat(results.get(0).isMatched()).isTrue();
        assertThat(results.get(0).getActualValue()).isEqualTo("true");
    }

    @Test
    void evaluateAnswer_booleanIsTrue_doesNotMatchFalseAnswer() {
        InspectionTemplateQuestion question = booleanQuestion(1L);
        InspectionAnswer answer = booleanAnswer(question, false);
        InspectionTemplateQuestionRule rule = rule(
                question, "LEAK_TRUE", DecisionRuleConditionType.BOOLEAN,
                DecisionRuleOperator.IS_TRUE, null, 100);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isFalse();
    }

    @Test
    void evaluateAnswer_booleanIsFalse_matchesFalseAnswer() {
        InspectionTemplateQuestion question = booleanQuestion(1L);
        InspectionAnswer answer = booleanAnswer(question, false);
        InspectionTemplateQuestionRule rule = rule(
                question, "LEAK_FALSE", DecisionRuleConditionType.BOOLEAN,
                DecisionRuleOperator.IS_FALSE, null, 100);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_numberGreaterThan_matches() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_numberGreaterThanOrEqual_matchesEqualValue() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("90"));
        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN_OR_EQUAL, "90", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_numberLessThan_matches() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("5"));
        InspectionTemplateQuestionRule rule = rule(
                question, "LOW_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.LESS_THAN, "10", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_numberLessThanOrEqual_matches() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("10"));
        InspectionTemplateQuestionRule rule = rule(
                question, "LOW_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.LESS_THAN_OR_EQUAL, "10", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_numberEquals_matches() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("90.0"));
        InspectionTemplateQuestionRule rule = rule(
                question, "EXACT_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.EQUALS, "90", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_numberNotEquals_matches() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule rule = rule(
                question, "NOT_NINETY", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.NOT_EQUALS, "90", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_numberUsesBigDecimalComparison() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("90.01"));
        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_choiceEquals_matchesCode() {
        InspectionTemplateQuestion question = choiceQuestion(2L);
        InspectionAnswer answer = choiceAnswer(question, "FAILED", "Failed");
        InspectionTemplateQuestionRule rule = rule(
                question, "FAILED_CONDITION", DecisionRuleConditionType.CHOICE,
                DecisionRuleOperator.EQUALS, "FAILED", 20);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_choiceEquals_ignoresLabelChanges() {
        InspectionTemplateQuestion question = choiceQuestion(2L);
        InspectionAnswer answer = choiceAnswer(question, "FAILED", "Renamed label");
        InspectionTemplateQuestionRule rule = rule(
                question, "FAILED_CONDITION", DecisionRuleConditionType.CHOICE,
                DecisionRuleOperator.EQUALS, "FAILED", 20);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_choiceNotEquals_matches() {
        InspectionTemplateQuestion question = choiceQuestion(2L);
        InspectionAnswer answer = choiceAnswer(question, "PASSED", "Passed");
        InspectionTemplateQuestionRule rule = rule(
                question, "NOT_FAILED", DecisionRuleConditionType.CHOICE,
                DecisionRuleOperator.NOT_EQUALS, "FAILED", 20);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_textEquals_isCaseInsensitive() {
        InspectionTemplateQuestion question = textQuestion(3L);
        InspectionAnswer answer = textAnswer(question, "Leak");
        InspectionTemplateQuestionRule rule = rule(
                question, "LEAK_TEXT", DecisionRuleConditionType.TEXT,
                DecisionRuleOperator.EQUALS, "leak", 100);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_textNotEquals_isCaseInsensitive() {
        InspectionTemplateQuestion question = textQuestion(3L);
        InspectionAnswer answer = textAnswer(question, "No issue");
        InspectionTemplateQuestionRule rule = rule(
                question, "NOT_LEAK", DecisionRuleConditionType.TEXT,
                DecisionRuleOperator.NOT_EQUALS, "LEAK", 100);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_textContains_isCaseInsensitive() {
        InspectionTemplateQuestion question = textQuestion(3L);
        InspectionAnswer answer = textAnswer(question, "Small LEAK detected");
        InspectionTemplateQuestionRule rule = rule(
                question, "CONTAINS_LEAK", DecisionRuleConditionType.TEXT,
                DecisionRuleOperator.CONTAINS, "leak", 100);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_textStartsWith_isCaseInsensitive() {
        InspectionTemplateQuestion question = textQuestion(3L);
        InspectionAnswer answer = textAnswer(question, "LEAK near valve");
        InspectionTemplateQuestionRule rule = rule(
                question, "STARTS_LEAK", DecisionRuleConditionType.TEXT,
                DecisionRuleOperator.STARTS_WITH, "leak", 100);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_textEndsWith_isCaseInsensitive() {
        InspectionTemplateQuestion question = textQuestion(3L);
        InspectionAnswer answer = textAnswer(question, "Detected a Leak");
        InspectionTemplateQuestionRule rule = rule(
                question, "ENDS_LEAK", DecisionRuleConditionType.TEXT,
                DecisionRuleOperator.ENDS_WITH, "leak", 100);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateAnswer_inactiveRuleIsIgnored() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);
        rule.setActive(false);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule))).isEmpty();
    }

    @Test
    void evaluateAnswer_wrongQuestionRuleIsIgnored() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionTemplateQuestion otherQuestion = numberQuestion(99L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule rule = rule(
                otherQuestion, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule))).isEmpty();
    }

    @Test
    void evaluateAnswer_mismatchedConditionTypeDoesNotMatch() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule rule = rule(
                question, "TEXT_RULE", DecisionRuleConditionType.TEXT,
                DecisionRuleOperator.EQUALS, "95", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isFalse();
    }

    @Test
    void evaluateAnswer_missingAnswerValueDoesNotMatch() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, null);
        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);

        assertThat(evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0).isMatched()).isFalse();
    }

    @Test
    void evaluateAnswer_ordersResultsByPriorityThenRuleCode() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule lowPriority = rule(
                question, "B_RULE", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 50);
        InspectionTemplateQuestionRule highPriority = rule(
                question, "A_RULE", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);
        InspectionTemplateQuestionRule samePriorityLaterCode = rule(
                question, "Z_RULE", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);

        List<DecisionRuleEvaluationResult> results = evaluationService.evaluateAnswer(
                contextFor(answer),
                List.of(lowPriority, samePriorityLaterCode, highPriority));

        assertThat(results).extracting(DecisionRuleEvaluationResult::getRuleCode)
                .containsExactly("A_RULE", "Z_RULE", "B_RULE");
    }

    @Test
    void evaluateAnswer_populatesResultMetadata() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);
        rule.setId(42L);
        rule.setRuleName("High temperature");
        rule.setActionPayload("{\"severity\":\"HIGH\"}");

        DecisionRuleEvaluationResult result = evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0);

        assertThat(result.getRuleId()).isEqualTo(42L);
        assertThat(result.getRuleName()).isEqualTo("High temperature");
        assertThat(result.getActionType()).isEqualTo(DecisionRuleActionType.SUGGEST_ISSUE);
        assertThat(result.getActionPayload()).contains("HIGH");
        assertThat(result.getPriority()).isEqualTo(10);
    }

    @Test
    void evaluateAnswer_populatesEvaluationMetadata() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);

        DecisionRuleEvaluationResult result = evaluationService.evaluateAnswer(contextFor(answer), List.of(rule)).get(0);

        assertThat(result.getEvaluatedAt()).isNotNull().isPositive();
        assertThat(result.getEvaluationDurationMs()).isGreaterThanOrEqualTo(0);
    }

    @Test
    void evaluateAnswer_usesRuleEvaluationContext() {
        InspectionTemplateQuestion question = numberQuestion(1L);
        InspectionAnswer answer = numberAnswer(question, new BigDecimal("95"));
        RuleEvaluationContext context = RuleEvaluationContext.from(inspection, answer);

        assertThat(context.getInspection()).isSameAs(inspection);
        assertThat(context.getAnswer()).isSameAs(answer);
        assertThat(context.getQuestion()).isSameAs(question);
        assertThat(context.getTemplate()).isNull();

        InspectionTemplateQuestionRule rule = rule(
                question, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN, "90", 10);

        assertThat(evaluationService.evaluateAnswer(context, List.of(rule)).get(0).isMatched()).isTrue();
    }

    @Test
    void evaluateInspection_evaluatesAllAnswersWithoutSideEffects() {
        InspectionTemplateQuestion temperatureQuestion = numberQuestion(1L);
        InspectionTemplateQuestion notesQuestion = textQuestion(3L);
        InspectionAnswer temperatureAnswer = numberAnswer(temperatureQuestion, new BigDecimal("95"));
        InspectionAnswer notesAnswer = textAnswer(notesQuestion, "Small leak detected");

        when(inspectionRepository.findWithEvaluationContextById(10L)).thenReturn(java.util.Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(10L))
                .thenReturn(List.of(temperatureAnswer, notesAnswer));
        when(ruleRepository.findByQuestionIdInAndActiveTrueOrderByPriorityAscRuleCodeAsc(List.of(1L, 3L)))
                .thenReturn(List.of(
                        rule(temperatureQuestion, "HIGH_TEMP", DecisionRuleConditionType.NUMBER,
                                DecisionRuleOperator.GREATER_THAN, "90", 10),
                        rule(notesQuestion, "CONTAINS_LEAK", DecisionRuleConditionType.TEXT,
                                DecisionRuleOperator.CONTAINS, "leak", 100)));

        List<DecisionRuleEvaluationResult> results = evaluationService.evaluateInspection(10L);

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(DecisionRuleEvaluationResult::isMatched);
        assertThat(results).allMatch(result -> result.getEvaluatedAt() != null);
        assertThat(results).allMatch(result -> result.getEvaluationDurationMs() >= 0);
        verify(ruleRepository).findByQuestionIdInAndActiveTrueOrderByPriorityAscRuleCodeAsc(List.of(1L, 3L));
        verify(ruleRepository, never()).findByQuestionIdAndActiveTrueOrderByPriorityAscRuleCodeAsc(anyLong());
    }

    private RuleEvaluationContext contextFor(InspectionAnswer answer) {
        return RuleEvaluationContext.from(inspection, answer);
    }

    private InspectionTemplate template() {
        AssetCategory category = new AssetCategory("Pump");
        category.setId(10L);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection", null, category, 1, InspectionTemplateStatus.PUBLISHED);
        template.setId(100L);
        return template;
    }

    private Inspection inspection() {
        return mock(Inspection.class);
    }

    private InspectionTemplateQuestion numberQuestion(Long id) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Temperature", "TEMPERATURE", null,
                InspectionTemplateQuestionType.NUMBER, true, 1);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion choiceQuestion(Long id) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Condition", "CONDITION", null,
                InspectionTemplateQuestionType.CHOICE, true, 2);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion booleanQuestion(Long id) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Leak?", "LEAK", null,
                InspectionTemplateQuestionType.BOOLEAN, true, 3);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion textQuestion(Long id) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Notes", "NOTES", null,
                InspectionTemplateQuestionType.TEXT, true, 4);
        question.setId(id);
        return question;
    }

    private InspectionAnswer numberAnswer(InspectionTemplateQuestion question, BigDecimal value) {
        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.NUMBER,
                null,
                null,
                value,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InspectionAnswer booleanAnswer(InspectionTemplateQuestion question, boolean value) {
        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                value,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InspectionAnswer choiceAnswer(
            InspectionTemplateQuestion question,
            String code,
            String label) {
        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.CHOICE,
                null,
                null,
                null,
                code,
                label,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InspectionAnswer textAnswer(InspectionTemplateQuestion question, String value) {
        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.TEXT,
                null,
                value,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InspectionTemplateQuestionRule rule(
            InspectionTemplateQuestion question,
            String ruleCode,
            DecisionRuleConditionType conditionType,
            DecisionRuleOperator operator,
            String comparisonValue,
            int priority) {
        InspectionTemplateQuestionRule rule = new InspectionTemplateQuestionRule(
                question,
                ruleCode,
                "Rule " + ruleCode,
                null,
                conditionType,
                operator,
                comparisonValue,
                DecisionRuleActionType.SUGGEST_ISSUE,
                null);
        rule.setPriority(priority);
        return rule;
    }
}
