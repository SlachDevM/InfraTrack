package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionRuleRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionRuleResponse;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionRuleRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionTemplateQuestionRuleServiceTest {

    @Mock
    private InspectionTemplateRepository inspectionTemplateRepository;

    @Mock
    private InspectionTemplateQuestionRepository questionRepository;

    @Mock
    private InspectionTemplateQuestionChoiceRepository choiceRepository;

    @Mock
    private InspectionTemplateQuestionRuleRepository ruleRepository;

    @InjectMocks
    private InspectionTemplateQuestionRuleService ruleService;

    @Test
    void create_shouldCreateRuleOnDraftTemplate() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(false);
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestionRule rule = invocation.getArgument(0);
            rule.setId(10L);
            return rule;
        });

        InspectionTemplateQuestionRuleResponse response = ruleService.create(100L, 1L, numberRuleRequest());

        assertThat(response.getRuleCode()).isEqualTo("HIGH_TEMPERATURE");
        assertThat(response.getConditionType()).isEqualTo(DecisionRuleConditionType.NUMBER);
        assertThat(response.getOperator()).isEqualTo(DecisionRuleOperator.GREATER_THAN);
        assertThat(response.getComparisonValue()).isEqualTo("90");
        assertThat(response.getActionType()).isEqualTo(DecisionRuleActionType.SUGGEST_ISSUE);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void create_shouldRejectPublishedTemplate() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.PUBLISHED)));

        assertThatThrownBy(() -> ruleService.create(100L, 1L, numberRuleRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Decision rules can only be modified on draft inspection templates");
    }

    @Test
    void create_shouldRejectArchivedTemplate() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.ARCHIVED)));

        assertThatThrownBy(() -> ruleService.create(100L, 1L, numberRuleRequest()))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_shouldRejectDuplicateRuleCode() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(true);

        assertThatThrownBy(() -> ruleService.create(100L, 1L, numberRuleRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Rule code already exists for this question");
    }

    @Test
    void create_shouldRejectInvalidRuleCode() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));

        CreateInspectionTemplateQuestionRuleRequest request = numberRuleRequest();
        request.setRuleCode("bad code");

        assertThatThrownBy(() -> ruleService.create(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void create_shouldRejectIncompatibleOperator() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(false);

        CreateInspectionTemplateQuestionRuleRequest request = numberRuleRequest();
        request.setOperator(DecisionRuleOperator.IS_TRUE);

        assertThatThrownBy(() -> ruleService.create(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Operator IS_TRUE is not supported for condition type NUMBER");
    }

    @Test
    void create_shouldRejectNonNumericComparisonForNumberRule() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(false);

        CreateInspectionTemplateQuestionRuleRequest request = numberRuleRequest();
        request.setComparisonValue("hot");

        assertThatThrownBy(() -> ruleService.create(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Comparison value must be numeric for NUMBER decision rules");
    }

    @Test
    void create_shouldAcceptChoiceRuleWithActiveChoiceCode() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = choiceQuestion(2L, template);
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(
                question, "FAILED", "Failed", 1);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(2L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(2L, "FAILED_CONDITION")).thenReturn(false);
        when(choiceRepository.findByQuestionIdAndCode(2L, "FAILED")).thenReturn(Optional.of(choice));
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestionRule rule = invocation.getArgument(0);
            rule.setId(11L);
            return rule;
        });

        CreateInspectionTemplateQuestionRuleRequest request = new CreateInspectionTemplateQuestionRuleRequest();
        request.setRuleCode("FAILED_CONDITION");
        request.setRuleName("Failed condition detected");
        request.setConditionType(DecisionRuleConditionType.CHOICE);
        request.setOperator(DecisionRuleOperator.EQUALS);
        request.setComparisonValue("FAILED");
        request.setActionType(DecisionRuleActionType.SUGGEST_SEVERITY);
        request.setActionPayload("{\"severity\":\"HIGH\"}");

        InspectionTemplateQuestionRuleResponse response = ruleService.create(100L, 2L, request);

        assertThat(response.getComparisonValue()).isEqualTo("FAILED");
    }

    @Test
    void create_shouldRejectInactiveChoiceCode() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = choiceQuestion(2L, template);
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(
                question, "FAILED", "Failed", 1);
        choice.setActive(false);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(2L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(2L, "FAILED_CONDITION")).thenReturn(false);
        when(choiceRepository.findByQuestionIdAndCode(2L, "FAILED")).thenReturn(Optional.of(choice));

        CreateInspectionTemplateQuestionRuleRequest request = new CreateInspectionTemplateQuestionRuleRequest();
        request.setRuleCode("FAILED_CONDITION");
        request.setRuleName("Failed condition detected");
        request.setConditionType(DecisionRuleConditionType.CHOICE);
        request.setOperator(DecisionRuleOperator.EQUALS);
        request.setComparisonValue("FAILED");
        request.setActionType(DecisionRuleActionType.SUGGEST_SEVERITY);

        assertThatThrownBy(() -> ruleService.create(100L, 2L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Comparison value must reference an active choice code for this question");
    }

    @Test
    void create_shouldAcceptBooleanRuleWithoutComparisonValue() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = booleanQuestion(3L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(3L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(3L, "LEAK_DETECTED")).thenReturn(false);
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestionRule rule = invocation.getArgument(0);
            rule.setId(12L);
            return rule;
        });

        CreateInspectionTemplateQuestionRuleRequest request = new CreateInspectionTemplateQuestionRuleRequest();
        request.setRuleCode("LEAK_DETECTED");
        request.setRuleName("Leak detected");
        request.setConditionType(DecisionRuleConditionType.BOOLEAN);
        request.setOperator(DecisionRuleOperator.IS_TRUE);
        request.setActionType(DecisionRuleActionType.FLAG_FOR_REVIEW);

        InspectionTemplateQuestionRuleResponse response = ruleService.create(100L, 3L, request);

        assertThat(response.getComparisonValue()).isNull();
    }

    @Test
    void create_shouldRejectInvalidJsonActionPayload() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(false);

        CreateInspectionTemplateQuestionRuleRequest request = numberRuleRequest();
        request.setActionPayload("{invalid");

        assertThatThrownBy(() -> ruleService.create(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Action payload must be valid JSON");
    }

    @Test
    void listByQuestionId_shouldReturnRulesSortedByPriorityThenRuleCode() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        InspectionTemplateQuestionRule ruleB = savedRule(question, "B_RULE", 50);
        InspectionTemplateQuestionRule ruleA = savedRule(question, "A_RULE", 10);
        InspectionTemplateQuestionRule ruleZ = savedRule(question, "Z_RULE", 10);
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.findByQuestionIdOrderByPriorityAscRuleCodeAsc(1L))
                .thenReturn(List.of(ruleA, ruleZ, ruleB));

        List<InspectionTemplateQuestionRuleResponse> responses = ruleService.listByQuestionId(100L, 1L);

        assertThat(responses).extracting(InspectionTemplateQuestionRuleResponse::getRuleCode)
                .containsExactly("A_RULE", "Z_RULE", "B_RULE");
    }

    @Test
    void create_shouldDefaultPriorityTo100() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(false);
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestionRule rule = invocation.getArgument(0);
            rule.setId(10L);
            return rule;
        });

        InspectionTemplateQuestionRuleResponse response = ruleService.create(100L, 1L, numberRuleRequest());

        assertThat(response.getPriority()).isEqualTo(100);
    }

    @Test
    void create_shouldPersistCustomPriority() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(false);
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestionRule rule = invocation.getArgument(0);
            rule.setId(10L);
            return rule;
        });

        CreateInspectionTemplateQuestionRuleRequest request = numberRuleRequest();
        request.setPriority(10);
        InspectionTemplateQuestionRuleResponse response = ruleService.create(100L, 1L, request);

        assertThat(response.getPriority()).isEqualTo(10);
    }

    @Test
    void create_shouldRejectNonPositivePriority() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.existsByQuestionIdAndRuleCode(1L, "HIGH_TEMPERATURE")).thenReturn(false);

        CreateInspectionTemplateQuestionRuleRequest request = numberRuleRequest();
        request.setPriority(0);

        assertThatThrownBy(() -> ruleService.create(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Rule priority must be a positive integer");
    }

    @Test
    void deactivate_shouldStoreDisabledReasonWhenProvided() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        InspectionTemplateQuestionRule rule = savedRule(question, "HIGH_TEMPERATURE");
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.findByIdAndQuestionId(10L, 1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionTemplateQuestionRuleResponse response = ruleService.deactivate(
                100L, 1L, 10L, "Replaced by HIGH_TEMPERATURE_V2.");

        assertThat(response.isActive()).isFalse();
        assertThat(response.getDisabledReason()).isEqualTo("Replaced by HIGH_TEMPERATURE_V2.");
    }

    @Test
    void deactivate_shouldDeactivateActiveRule() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        InspectionTemplateQuestionRule rule = savedRule(question, "HIGH_TEMPERATURE");
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.findByIdAndQuestionId(10L, 1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionTemplateQuestionRuleResponse response = ruleService.deactivate(100L, 1L, 10L, null);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    void update_shouldUpdateRuleMetadataWithoutChangingRuleCode() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = numberQuestion(1L, template);
        InspectionTemplateQuestionRule rule = savedRule(question, "HIGH_TEMPERATURE");
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(ruleRepository.findByIdAndQuestionId(10L, 1L)).thenReturn(Optional.of(rule));
        when(ruleRepository.save(any(InspectionTemplateQuestionRule.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateInspectionTemplateQuestionRuleRequest request = new UpdateInspectionTemplateQuestionRuleRequest();
        request.setRuleName("Updated temperature rule");
        request.setConditionType(DecisionRuleConditionType.NUMBER);
        request.setOperator(DecisionRuleOperator.GREATER_THAN_OR_EQUAL);
        request.setComparisonValue("95");
        request.setActionType(DecisionRuleActionType.SUGGEST_SEVERITY);
        request.setActionPayload("{\"severity\":\"HIGH\"}");
        request.setPriority(20);

        InspectionTemplateQuestionRuleResponse response = ruleService.update(100L, 1L, 10L, request);

        assertThat(response.getRuleCode()).isEqualTo("HIGH_TEMPERATURE");
        assertThat(response.getRuleName()).isEqualTo("Updated temperature rule");
        assertThat(response.getComparisonValue()).isEqualTo("95");
    }

    private CreateInspectionTemplateQuestionRuleRequest numberRuleRequest() {
        CreateInspectionTemplateQuestionRuleRequest request = new CreateInspectionTemplateQuestionRuleRequest();
        request.setRuleCode("HIGH_TEMPERATURE");
        request.setRuleName("High temperature");
        request.setConditionType(DecisionRuleConditionType.NUMBER);
        request.setOperator(DecisionRuleOperator.GREATER_THAN);
        request.setComparisonValue("90");
        request.setActionType(DecisionRuleActionType.SUGGEST_ISSUE);
        request.setActionPayload("{\"severity\":\"HIGH\",\"message\":\"Temperature exceeds safe operating range.\"}");
        return request;
    }

    private InspectionTemplate template(Long id, InspectionTemplateStatus status) {
        com.infratrack.assetcategory.AssetCategory category = new com.infratrack.assetcategory.AssetCategory("Pump");
        category.setId(10L);
        InspectionTemplate template = new InspectionTemplate("Pump Inspection", null, category, 1, status);
        template.setId(id);
        return template;
    }

    private InspectionTemplateQuestion numberQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Temperature", "TEMPERATURE", null,
                InspectionTemplateQuestionType.NUMBER, true, 1);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion choiceQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Condition", "CONDITION", null,
                InspectionTemplateQuestionType.CHOICE, true, 2);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion booleanQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template, "Leak?", "LEAK", null,
                InspectionTemplateQuestionType.BOOLEAN, true, 3);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestionRule savedRule(InspectionTemplateQuestion question, String ruleCode) {
        return savedRule(question, ruleCode, 100);
    }

    private InspectionTemplateQuestionRule savedRule(
            InspectionTemplateQuestion question,
            String ruleCode,
            int priority) {
        InspectionTemplateQuestionRule rule = new InspectionTemplateQuestionRule(
                question,
                ruleCode,
                "Rule name",
                null,
                DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN,
                "90",
                DecisionRuleActionType.SUGGEST_ISSUE,
                null
        );
        rule.setId(10L);
        rule.setPriority(priority);
        return rule;
    }
}
