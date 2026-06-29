package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionChoiceRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionChoiceResponse;
import com.infratrack.inspectiontemplate.dto.ReorderInspectionTemplateQuestionChoicesRequest;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionChoiceRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manages allowed choices on CHOICE checklist questions (V2 Domain Engine A2.3.2).
 */
@Service
public class InspectionTemplateQuestionChoiceService {

    private final InspectionTemplateRepository inspectionTemplateRepository;
    private final InspectionTemplateQuestionRepository questionRepository;
    private final InspectionTemplateQuestionChoiceRepository choiceRepository;

    public InspectionTemplateQuestionChoiceService(
            InspectionTemplateRepository inspectionTemplateRepository,
            InspectionTemplateQuestionRepository questionRepository,
            InspectionTemplateQuestionChoiceRepository choiceRepository) {
        this.inspectionTemplateRepository = inspectionTemplateRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
    }

    @Transactional(readOnly = true)
    public List<InspectionTemplateQuestionChoiceResponse> listByQuestionId(Long templateId, Long questionId) {
        findQuestionOrThrow(templateId, questionId);
        return choiceRepository.findByQuestionIdOrderByDisplayOrderAsc(questionId).stream()
                .map(InspectionTemplateQuestionChoiceResponse::from)
                .toList();
    }

    @Transactional
    public InspectionTemplateQuestionChoiceResponse create(
            Long templateId,
            Long questionId,
            CreateInspectionTemplateQuestionChoiceRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        requireChoiceQuestion(question);
        requireActiveQuestion(question);

        String code = normalizeAndValidateCode(request.getCode());
        requireUniqueCode(questionId, code);
        String label = normalizeLabel(request.getLabel());
        int displayOrder = resolveDisplayOrder(questionId, request.getDisplayOrder());

        InspectionTemplateQuestionChoice choice = choiceRepository.save(new InspectionTemplateQuestionChoice(
                question,
                code,
                label,
                displayOrder
        ));
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionChoiceResponse.from(choice);
    }

    @Transactional
    public InspectionTemplateQuestionChoiceResponse update(
            Long templateId,
            Long questionId,
            Long choiceId,
            UpdateInspectionTemplateQuestionChoiceRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        requireChoiceQuestion(question);
        InspectionTemplateQuestionChoice choice = findChoiceOrThrow(questionId, choiceId);
        requireActiveChoice(choice);

        choice.setLabel(normalizeLabel(request.getLabel()));
        choice.touchUpdatedAt();
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionChoiceResponse.from(choiceRepository.save(choice));
    }

    @Transactional
    public InspectionTemplateQuestionChoiceResponse deactivate(
            Long templateId,
            Long questionId,
            Long choiceId) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        requireChoiceQuestion(question);
        InspectionTemplateQuestionChoice choice = findChoiceOrThrow(questionId, choiceId);
        requireActiveChoice(choice);

        choice.setActive(false);
        choice.touchUpdatedAt();
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        return InspectionTemplateQuestionChoiceResponse.from(choiceRepository.save(choice));
    }

    @Transactional
    public List<InspectionTemplateQuestionChoiceResponse> reorder(
            Long templateId,
            Long questionId,
            ReorderInspectionTemplateQuestionChoicesRequest request) {
        InspectionTemplate template = findTemplateOrThrow(templateId);
        requireDraftTemplate(template);
        InspectionTemplateQuestion question = findQuestionOrThrow(templateId, questionId);
        requireChoiceQuestion(question);

        List<InspectionTemplateQuestionChoice> choices =
                choiceRepository.findByQuestionIdOrderByDisplayOrderAsc(questionId);
        List<Long> orderedIds = request.getOrderedChoiceIds();
        validateReorderRequest(choices, orderedIds);

        Map<Long, InspectionTemplateQuestionChoice> choiceById = choices.stream()
                .collect(Collectors.toMap(InspectionTemplateQuestionChoice::getId, Function.identity()));

        for (int index = 0; index < orderedIds.size(); index++) {
            InspectionTemplateQuestionChoice choice = choiceById.get(orderedIds.get(index));
            choice.setDisplayOrder(index + 1);
            choice.touchUpdatedAt();
        }
        question.touchUpdatedAt();
        template.touchUpdatedAt();
        choiceRepository.saveAll(choices);

        return choiceRepository.findByQuestionIdOrderByDisplayOrderAsc(questionId).stream()
                .map(InspectionTemplateQuestionChoiceResponse::from)
                .toList();
    }

    private void validateReorderRequest(
            List<InspectionTemplateQuestionChoice> choices,
            List<Long> orderedIds) {
        Set<Long> uniqueIds = new HashSet<>(orderedIds);
        if (uniqueIds.size() != orderedIds.size()) {
            throw new BusinessValidationException("Reorder request contains duplicate choice IDs");
        }

        List<InspectionTemplateQuestionChoice> activeChoices = choices.stream()
                .filter(InspectionTemplateQuestionChoice::isActive)
                .toList();

        if (orderedIds.size() != activeChoices.size()) {
            throw new BusinessValidationException(
                    "Reorder request must include all active choices for the question");
        }

        Set<Long> activeChoiceIds = activeChoices.stream()
                .map(InspectionTemplateQuestionChoice::getId)
                .collect(Collectors.toSet());

        if (!activeChoiceIds.equals(uniqueIds)) {
            throw new BusinessValidationException(
                    "Reorder request must include exactly the active choices for this question");
        }
    }

    private int resolveDisplayOrder(Long questionId, Integer requestedOrder) {
        if (requestedOrder != null) {
            if (requestedOrder <= 0) {
                throw new BusinessValidationException("Display order must be positive");
            }
            return requestedOrder;
        }
        List<InspectionTemplateQuestionChoice> existing =
                choiceRepository.findByQuestionIdOrderByDisplayOrderAsc(questionId);
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

    private InspectionTemplateQuestionChoice findChoiceOrThrow(Long questionId, Long choiceId) {
        return choiceRepository.findByIdAndQuestionId(choiceId, questionId)
                .orElseThrow(() -> new NotFoundException("Inspection template question choice not found"));
    }

    private void requireDraftTemplate(InspectionTemplate template) {
        if (template.getStatus() != InspectionTemplateStatus.DRAFT) {
            throw new ConflictException(
                    "Checklist question choices can only be modified on draft inspection templates");
        }
    }

    private void requireChoiceQuestion(InspectionTemplateQuestion question) {
        if (question.getQuestionType() != InspectionTemplateQuestionType.CHOICE) {
            throw new BusinessValidationException("Choices apply only to CHOICE checklist questions");
        }
    }

    private void requireActiveQuestion(InspectionTemplateQuestion question) {
        if (!question.isActive()) {
            throw new BusinessValidationException("Inactive checklist questions cannot be modified");
        }
    }

    private void requireActiveChoice(InspectionTemplateQuestionChoice choice) {
        if (!choice.isActive()) {
            throw new BusinessValidationException("Inactive choices cannot be modified");
        }
    }

    private String normalizeAndValidateCode(String code) {
        String normalized = InspectionTemplateQuestionCode.normalize(code);
        InspectionTemplateQuestionCode.validateFormat(normalized);
        return normalized;
    }

    private void requireUniqueCode(Long questionId, String code) {
        if (choiceRepository.existsByQuestionIdAndCode(questionId, code)) {
            throw new ConflictException("Choice code already exists for this question");
        }
    }

    private String normalizeLabel(String label) {
        if (label == null || label.isBlank()) {
            throw new BusinessValidationException("Choice label is required");
        }
        return label.trim();
    }
}
