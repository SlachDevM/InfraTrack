package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionRuleRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionRuleResponse;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionRuleRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Manages decision rule definitions on checklist questions (V2 Domain Engine A3.1).
 * Rules are stored in A3.1; evaluated in A3.2 without persistence or workflow side effects.
 */
@Service
public class InspectionTemplateQuestionRuleService {

    private final InspectionTemplateRepository inspectionTemplateRepository;
    private final InspectionTemplateQuestionRepository questionRepository;
    private final InspectionTemplateQuestionChoiceRepository choiceRepository;
    private final InspectionTemplateQuestionRuleRepository ruleRepository;

    public InspectionTemplateQuestionRuleService(
            InspectionTemplateRepository inspectionTemplateRepository,
            InspectionTemplateQuestionRepository questionRepository,
            InspectionTemplateQuestionChoiceRepository choiceRepository,
            InspectionTemplateQuestionRuleRepository ruleRepository) {
        this.inspectionTemplateRepository = inspectionTemplateRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.ruleRepository = ruleRepository;
    }

    @Transactional(readOnly = true)
    public List<InspectionTemplateQuestionRuleResponse> listByQuestionId(Long templateId, Long questionId) {
        findQuestionOrThrow(templateId, questionId);
        return ruleRepository.findByQuestionIdOrderByPriorityAscRuleCodeAsc(questionId).stream()
                .map(InspectionTemplateQuestionRuleResponse::from)
                .toList();
    }

    @Transactional
    public InspectionTemplateQuestionRuleResponse create(
            Long templateId,
            Long questionId,
            CreateInspectionTemplateQuestionRuleRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        requireActiveQuestion(question);

        String ruleCode = normalizeAndValidateRuleCode(request.getRuleCode());
        requireUniqueRuleCode(questionId, ruleCode);
        String ruleName = normalizeRuleName(request.getRuleName());
        String description = DecisionRuleDefinitionValidator.normalizeOptionalDescription(request.getDescription());
        String actionPayload = DecisionRuleDefinitionValidator.normalizeOptionalActionPayload(request.getActionPayload());

        DecisionRuleDefinitionValidator.validateRuleDefinition(
                question,
                request.getConditionType(),
                request.getOperator(),
                request.getComparisonValue(),
                request.getActionType(),
                actionPayload);

        if (request.getConditionType() == DecisionRuleConditionType.CHOICE) {
            DecisionRuleDefinitionValidator.validateChoiceCodeExists(
                    choiceRepository,
                    questionId,
                    request.getComparisonValue());
        }

        String comparisonValue = DecisionRuleDefinitionValidator.normalizeComparisonValue(
                request.getConditionType(),
                request.getComparisonValue());
        int priority = requirePositivePriority(request.getPriority());

        InspectionTemplateQuestionRule rule = new InspectionTemplateQuestionRule(
                question,
                ruleCode,
                ruleName,
                description,
                request.getConditionType(),
                request.getOperator(),
                comparisonValue,
                request.getActionType(),
                actionPayload
        );
        rule.setPriority(priority);
        ruleRepository.save(rule);
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionRuleResponse.from(rule);
    }

    @Transactional
    public InspectionTemplateQuestionRuleResponse update(
            Long templateId,
            Long questionId,
            Long ruleId,
            UpdateInspectionTemplateQuestionRuleRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        InspectionTemplateQuestionRule rule = findRuleOrThrow(questionId, ruleId);
        requireActiveRule(rule);

        String ruleName = normalizeRuleName(request.getRuleName());
        String description = DecisionRuleDefinitionValidator.normalizeOptionalDescription(request.getDescription());
        String actionPayload = DecisionRuleDefinitionValidator.normalizeOptionalActionPayload(request.getActionPayload());

        DecisionRuleDefinitionValidator.validateRuleDefinition(
                question,
                request.getConditionType(),
                request.getOperator(),
                request.getComparisonValue(),
                request.getActionType(),
                actionPayload);

        if (request.getConditionType() == DecisionRuleConditionType.CHOICE) {
            DecisionRuleDefinitionValidator.validateChoiceCodeExists(
                    choiceRepository,
                    questionId,
                    request.getComparisonValue());
        }

        rule.setRuleName(ruleName);
        rule.setDescription(description);
        rule.setConditionType(request.getConditionType());
        rule.setOperator(request.getOperator());
        rule.setComparisonValue(DecisionRuleDefinitionValidator.normalizeComparisonValue(
                request.getConditionType(),
                request.getComparisonValue()));
        rule.setActionType(request.getActionType());
        rule.setActionPayload(actionPayload);
        rule.setPriority(requirePositivePriority(request.getPriority()));
        rule.touchUpdatedAt();
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionRuleResponse.from(ruleRepository.save(rule));
    }

    @Transactional
    public InspectionTemplateQuestionRuleResponse deactivate(
            Long templateId,
            Long questionId,
            Long ruleId,
            String disabledReason) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        InspectionTemplateQuestionRule rule = findRuleOrThrow(questionId, ruleId);
        requireActiveRule(rule);

        rule.setActive(false);
        if (disabledReason != null && !disabledReason.isBlank()) {
            rule.setDisabledReason(disabledReason.trim());
        }
        rule.touchUpdatedAt();
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionRuleResponse.from(ruleRepository.save(rule));
    }

    private InspectionTemplate findTemplateOrThrow(Long templateId) {
        return inspectionTemplateRepository.findDetailedById(templateId)
                .orElseThrow(() -> new NotFoundException("Inspection template not found"));
    }

    private InspectionTemplateQuestion findQuestionOrThrow(Long templateId, Long questionId) {
        return questionRepository.findByIdAndInspectionTemplateId(questionId, templateId)
                .orElseThrow(() -> new NotFoundException("Inspection template question not found"));
    }

    private InspectionTemplateQuestionRule findRuleOrThrow(Long questionId, Long ruleId) {
        return ruleRepository.findByIdAndQuestionId(ruleId, questionId)
                .orElseThrow(() -> new NotFoundException("Inspection template question rule not found"));
    }

    private void requireDraftTemplate(InspectionTemplate template) {
        if (template.getStatus() != InspectionTemplateStatus.DRAFT) {
            throw new ConflictException(
                    "Decision rules can only be modified on draft inspection templates");
        }
    }

    private void requireActiveQuestion(InspectionTemplateQuestion question) {
        if (!question.isActive()) {
            throw new BusinessValidationException("Inactive checklist questions cannot be modified");
        }
    }

    private void requireActiveRule(InspectionTemplateQuestionRule rule) {
        if (!rule.isActive()) {
            throw new BusinessValidationException("Inactive decision rules cannot be modified");
        }
    }

    private String normalizeAndValidateRuleCode(String ruleCode) {
        String normalized = InspectionTemplateQuestionRuleCode.normalize(ruleCode);
        InspectionTemplateQuestionRuleCode.validateFormat(normalized);
        return normalized;
    }

    private void requireUniqueRuleCode(Long questionId, String ruleCode) {
        if (ruleRepository.existsByQuestionIdAndRuleCode(questionId, ruleCode)) {
            throw new ConflictException("Rule code already exists for this question");
        }
    }

    private String normalizeRuleName(String ruleName) {
        if (ruleName == null || ruleName.isBlank()) {
            throw new BusinessValidationException("Rule name is required");
        }
        return ruleName.trim();
    }

    private int requirePositivePriority(Integer priority) {
        int resolved = priority == null ? 100 : priority;
        if (resolved <= 0) {
            throw new BusinessValidationException("Rule priority must be a positive integer");
        }
        return resolved;
    }
}
