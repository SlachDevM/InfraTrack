package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionChoiceResponse;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionResponse;
import com.infratrack.inspectiontemplate.dto.ReorderInspectionTemplateQuestionsRequest;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionRequest;
import com.infratrack.unitofmeasure.UnitOfMeasure;
import com.infratrack.unitofmeasure.UnitOfMeasureRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages checklist questions on inspection templates (V2 Domain Engine A2.2).
 */
@Service
public class InspectionTemplateQuestionService {

    private final InspectionTemplateRepository inspectionTemplateRepository;
    private final InspectionTemplateQuestionRepository questionRepository;
    private final InspectionTemplateQuestionChoiceRepository choiceRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;

    public InspectionTemplateQuestionService(
            InspectionTemplateRepository inspectionTemplateRepository,
            InspectionTemplateQuestionRepository questionRepository,
            InspectionTemplateQuestionChoiceRepository choiceRepository,
            UnitOfMeasureRepository unitOfMeasureRepository) {
        this.inspectionTemplateRepository = inspectionTemplateRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
    }

    @Transactional(readOnly = true)
    public List<InspectionTemplateQuestionResponse> listByTemplateId(Long templateId) {
        findTemplateOrThrow(templateId);
        List<InspectionTemplateQuestion> questions =
                questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(templateId);
        Map<Long, List<InspectionTemplateQuestionChoiceResponse>> choicesByQuestionId =
                loadChoicesByQuestionId(questions);
        return questions.stream()
                .map(question -> InspectionTemplateQuestionResponse.from(
                        question,
                        choicesByQuestionId.getOrDefault(question.getId(), List.of())))
                .toList();
    }

    @Transactional
    public InspectionTemplateQuestionResponse create(Long templateId, CreateInspectionTemplateQuestionRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);

        String questionText = normalizeQuestionText(request.getQuestionText());
        String code = normalizeAndValidateCode(request.getCode());
        requireUniqueCode(templateId, code);
        String helpText = normalizeOptionalHelpText(request.getHelpText());
        InspectionTemplateQuestionType questionType = requireQuestionType(request.getQuestionType());
        boolean required = Boolean.TRUE.equals(request.getRequired());
        int displayOrder = resolveDisplayOrder(templateId, request.getDisplayOrder());
        InspectionTemplateQuestionNumberConstraints.validateForQuestionType(
                questionType,
                request.getUnitOfMeasureId(),
                request.getMinValue(),
                request.getMaxValue(),
                request.getDecimalPlaces());

        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                questionText,
                code,
                helpText,
                questionType,
                required,
                displayOrder
        );
        applyNumberConstraints(question, questionType, request.getUnitOfMeasureId(),
                request.getMinValue(), request.getMaxValue(), request.getDecimalPlaces());
        question = questionRepository.save(question);
        template.touchUpdatedAt();
        return InspectionTemplateQuestionResponse.from(question);
    }

    @Transactional
    public InspectionTemplateQuestionResponse update(
            Long templateId,
            Long questionId,
            UpdateInspectionTemplateQuestionRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        requireActiveQuestion(question);

        question.setQuestionText(normalizeQuestionText(request.getQuestionText()));
        question.setHelpText(normalizeOptionalHelpText(request.getHelpText()));
        InspectionTemplateQuestionType questionType = requireQuestionType(request.getQuestionType());
        InspectionTemplateQuestionNumberConstraints.validateForQuestionType(
                questionType,
                request.getUnitOfMeasureId(),
                request.getMinValue(),
                request.getMaxValue(),
                request.getDecimalPlaces());
        question.setQuestionType(questionType);
        question.setRequired(Boolean.TRUE.equals(request.getRequired()));
        applyNumberConstraints(question, questionType, request.getUnitOfMeasureId(),
                request.getMinValue(), request.getMaxValue(), request.getDecimalPlaces());
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionResponse.from(questionRepository.save(question));
    }

    @Transactional
    public InspectionTemplateQuestionResponse deactivate(Long templateId, Long questionId) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        requireActiveQuestion(question);

        question.setActive(false);
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionResponse.from(questionRepository.save(question));
    }

    @Transactional
    public List<InspectionTemplateQuestionResponse> reorder(
            Long templateId,
            ReorderInspectionTemplateQuestionsRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);

        List<InspectionTemplateQuestion> questions =
                questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(templateId);
        List<Long> orderedIds = request.getOrderedQuestionIds();

        validateReorderRequest(questions, orderedIds);

        Map<Long, InspectionTemplateQuestion> questionById = questions.stream()
                .collect(Collectors.toMap(InspectionTemplateQuestion::getId, Function.identity()));

        for (int index = 0; index < orderedIds.size(); index++) {
            InspectionTemplateQuestion question = questionById.get(orderedIds.get(index));
            question.setDisplayOrder(index + 1);
            question.touchUpdatedAt();
        }
        template.touchUpdatedAt();
        questionRepository.saveAll(questions);

        return questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(templateId).stream()
                .map(InspectionTemplateQuestionResponse::from)
                .toList();
    }

    private void validateReorderRequest(List<InspectionTemplateQuestion> questions, List<Long> orderedIds) {
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw new BusinessValidationException("Reorder request contains duplicate question IDs");
        }

        List<InspectionTemplateQuestion> activeQuestions = questions.stream()
                .filter(InspectionTemplateQuestion::isActive)
                .toList();

        if (orderedIds.size() != activeQuestions.size()) {
            throw new BusinessValidationException(
                    "Reorder request must include all active questions for the template");
        }

        Set<Long> activeQuestionIds = activeQuestions.stream()
                .map(InspectionTemplateQuestion::getId)
                .collect(Collectors.toSet());

        if (!activeQuestionIds.equals(uniqueIds)) {
            throw new BusinessValidationException(
                    "Reorder request must include exactly the active questions for this template");
        }
    }

    private int resolveDisplayOrder(Long templateId, Integer requestedOrder) {
        if (requestedOrder != null) {
            if (requestedOrder <= 0) {
                throw new BusinessValidationException("Display order must be positive");
            }
            return requestedOrder;
        }
        List<InspectionTemplateQuestion> existing =
                questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(templateId);
        if (existing.isEmpty()) {
            return 1;
        }
        return existing.get(existing.size() - 1).getDisplayOrder() + 1;
    }

    private InspectionTemplate findTemplateOrThrow(Long templateId) {
        return inspectionTemplateRepository.findDetailedById(templateId)
                .orElseThrow(() -> new NotFoundException("Inspection template not found"));
    }

    private InspectionTemplateQuestion findQuestionOrThrow(Long templateId, Long questionId) {
        return questionRepository.findByIdAndInspectionTemplateId(questionId, templateId)
                .orElseThrow(() -> new NotFoundException("Inspection template question not found"));
    }

    private void requireDraftTemplate(InspectionTemplate template) {
        if (template.getStatus() != InspectionTemplateStatus.DRAFT) {
            throw new ConflictException(
                    "Checklist questions can only be modified on draft inspection templates");
        }
    }

    private void requireActiveQuestion(InspectionTemplateQuestion question) {
        if (!question.isActive()) {
            throw new BusinessValidationException("Inactive checklist questions cannot be modified");
        }
    }

    private String normalizeQuestionText(String questionText) {
        if (questionText == null || questionText.isBlank()) {
            throw new BusinessValidationException("Question text is required");
        }
        return questionText.trim();
    }

    private String normalizeOptionalHelpText(String helpText) {
        if (helpText == null || helpText.isBlank()) {
            return null;
        }
        return helpText.trim();
    }

    private InspectionTemplateQuestionType requireQuestionType(InspectionTemplateQuestionType questionType) {
        if (questionType == null) {
            throw new BusinessValidationException("Question type is required");
        }
        return questionType;
    }

    private String normalizeAndValidateCode(String code) {
        String normalized = InspectionTemplateQuestionCode.normalize(code);
        InspectionTemplateQuestionCode.validateFormat(normalized);
        return normalized;
    }

    private void requireUniqueCode(Long templateId, String code) {
        if (questionRepository.existsByInspectionTemplateIdAndCode(templateId, code)) {
            throw new ConflictException("Question code already exists for this template");
        }
    }

    private void applyNumberConstraints(
            InspectionTemplateQuestion question,
            InspectionTemplateQuestionType questionType,
            Long unitOfMeasureId,
            java.math.BigDecimal minValue,
            java.math.BigDecimal maxValue,
            Integer decimalPlaces) {
        if (questionType == InspectionTemplateQuestionType.NUMBER) {
            UnitOfMeasure unitOfMeasure = resolveActiveUnitOfMeasure(unitOfMeasureId);
            question.setUnitOfMeasure(unitOfMeasure);
            question.setUnit(unitOfMeasure != null ? unitOfMeasure.getSymbol() : null);
            question.setMinValue(minValue);
            question.setMaxValue(maxValue);
            question.setDecimalPlaces(decimalPlaces);
        } else {
            if (unitOfMeasureId != null) {
                throw new BusinessValidationException(
                        "Unit of measure applies only to NUMBER checklist questions");
            }
            question.setUnitOfMeasure(null);
            question.setUnit(null);
            question.setMinValue(null);
            question.setMaxValue(null);
            question.setDecimalPlaces(null);
        }
    }

    private UnitOfMeasure resolveActiveUnitOfMeasure(Long unitOfMeasureId) {
        if (unitOfMeasureId == null) {
            return null;
        }
        return unitOfMeasureRepository.findByIdAndActiveTrue(unitOfMeasureId)
                .orElseThrow(() -> new BusinessValidationException("Active unit of measure not found"));
    }

    private Map<Long, List<InspectionTemplateQuestionChoiceResponse>> loadChoicesByQuestionId(
            List<InspectionTemplateQuestion> questions) {
        List<Long> choiceQuestionIds = questions.stream()
                .filter(question -> question.getQuestionType() == InspectionTemplateQuestionType.CHOICE)
                .map(InspectionTemplateQuestion::getId)
                .toList();
        if (choiceQuestionIds.isEmpty()) {
            return Map.of();
        }
        return choiceRepository.findByQuestionIdInOrderByQuestionIdAscDisplayOrderAsc(choiceQuestionIds).stream()
                .map(InspectionTemplateQuestionChoiceResponse::from)
                .collect(Collectors.groupingBy(InspectionTemplateQuestionChoiceResponse::getQuestionId));
    }
}
