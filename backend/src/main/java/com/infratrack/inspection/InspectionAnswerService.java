package com.infratrack.inspection;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.inspection.dto.InspectionAnswerRequest;
import com.infratrack.inspection.dto.InspectionAnswerResponse;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionNumberConstraints;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.unitofmeasure.UnitOfMeasure;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Validates and persists structured inspection answers (V2 Domain Engine A2.3.1–A2.3.3).
 */
@Service
public class InspectionAnswerService {

    private static final Set<InspectionTemplateQuestionType> SUPPORTED_TYPES = EnumSet.of(
            InspectionTemplateQuestionType.BOOLEAN,
            InspectionTemplateQuestionType.TEXT,
            InspectionTemplateQuestionType.NUMBER,
            InspectionTemplateQuestionType.CHOICE
    );

    private final InspectionAnswerRepository inspectionAnswerRepository;
    private final InspectionTemplateQuestionRepository questionRepository;
    private final InspectionTemplateQuestionChoiceRepository choiceRepository;

    public InspectionAnswerService(
            InspectionAnswerRepository inspectionAnswerRepository,
            InspectionTemplateQuestionRepository questionRepository,
            InspectionTemplateQuestionChoiceRepository choiceRepository) {
        this.inspectionAnswerRepository = inspectionAnswerRepository;
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
    }

    @Transactional(readOnly = true)
    public List<InspectionAnswerResponse> listByInspectionId(Long inspectionId) {
        return inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(inspectionId).stream()
                .map(InspectionAnswerResponse::from)
                .toList();
    }

    /**
     * Upserts answers during progressive save. Partial payloads are supported; omitted questions are unchanged.
     */
    @Transactional
    public List<InspectionAnswerResponse> upsertProgressiveAnswers(
            Inspection inspection,
            List<InspectionAnswerRequest> requests) {
        if (inspection.getInspectionTemplate() == null) {
            if (requests != null && !requests.isEmpty()) {
                throw new BusinessValidationException(
                        "Structured answers are only supported for templated inspections");
            }
            return listByInspectionId(inspection.getId());
        }

        List<InspectionAnswerRequest> answerRequests = requests == null ? List.of() : requests;
        if (answerRequests.isEmpty()) {
            return listByInspectionId(inspection.getId());
        }

        Map<Long, InspectionTemplateQuestion> questionById = loadQuestionMap(inspection);
        validateSubmittedAnswersForUpsert(answerRequests, questionById);
        upsertAnswers(inspection, answerRequests, questionById);
        return listByInspectionId(inspection.getId());
    }

    /**
     * Merges any submitted answers with previously saved answers and validates mandatory questions at completion.
     */
    @Transactional
    public int saveAnswers(Inspection inspection, List<InspectionAnswerRequest> requests) {
        if (inspection.getInspectionTemplate() == null) {
            if (requests != null && !requests.isEmpty()) {
                throw new BusinessValidationException(
                        "Structured answers are only supported for templated inspections");
            }
            return 0;
        }

        List<InspectionAnswerRequest> answerRequests = requests == null ? List.of() : requests;
        List<InspectionTemplateQuestion> templateQuestions = questionRepository
                .findByInspectionTemplateIdOrderByDisplayOrderAsc(inspection.getInspectionTemplate().getId());
        Map<Long, InspectionTemplateQuestion> questionById = templateQuestions.stream()
                .collect(Collectors.toMap(InspectionTemplateQuestion::getId, Function.identity()));

        if (!answerRequests.isEmpty()) {
            validateSubmittedAnswersForUpsert(answerRequests, questionById);
            upsertAnswers(inspection, answerRequests, questionById);
        }

        validateAllRequiredQuestionsAnswered(inspection, templateQuestions);
        return inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(inspection.getId()).size();
    }

    private Map<Long, InspectionTemplateQuestion> loadQuestionMap(Inspection inspection) {
        return questionRepository
                .findByInspectionTemplateIdOrderByDisplayOrderAsc(inspection.getInspectionTemplate().getId())
                .stream()
                .collect(Collectors.toMap(InspectionTemplateQuestion::getId, Function.identity()));
    }

    private void upsertAnswers(
            Inspection inspection,
            List<InspectionAnswerRequest> answerRequests,
            Map<Long, InspectionTemplateQuestion> questionById) {
        for (InspectionAnswerRequest request : answerRequests) {
            InspectionTemplateQuestion question = questionById.get(request.getQuestionId());
            InspectionAnswer built = buildAnswer(inspection, question, request);
            Optional<InspectionAnswer> existing = inspectionAnswerRepository.findByInspectionIdAndQuestionId(
                    inspection.getId(), question.getId());
            if (existing.isPresent()) {
                InspectionAnswer answer = existing.get();
                answer.applyValuesFrom(built);
                inspectionAnswerRepository.save(answer);
            } else {
                inspectionAnswerRepository.save(built);
            }
        }
    }

    private void validateAllRequiredQuestionsAnswered(
            Inspection inspection,
            List<InspectionTemplateQuestion> templateQuestions) {
        Set<Long> answeredQuestionIds = inspectionAnswerRepository
                .findByInspectionIdOrderByQuestionDisplayOrder(inspection.getId())
                .stream()
                .map(answer -> answer.getQuestion().getId())
                .collect(Collectors.toSet());

        for (InspectionTemplateQuestion question : templateQuestions) {
            if (!question.isActive() || !SUPPORTED_TYPES.contains(question.getQuestionType())) {
                continue;
            }
            if (question.isRequired() && !answeredQuestionIds.contains(question.getId())) {
                throw new BusinessValidationException(
                        "Required checklist question '" + question.getCode() + "' must be answered");
            }
        }
    }

    private void validateSubmittedAnswersForUpsert(
            List<InspectionAnswerRequest> answerRequests,
            Map<Long, InspectionTemplateQuestion> questionById) {
        Set<Long> seenQuestionIds = new HashSet<>();
        for (InspectionAnswerRequest request : answerRequests) {
            if (request.getQuestionId() == null) {
                throw new BusinessValidationException("Question ID is required for each answer");
            }
            if (!seenQuestionIds.add(request.getQuestionId())) {
                throw new ConflictException("Duplicate answer submitted for the same checklist question");
            }

            InspectionTemplateQuestion question = questionById.get(request.getQuestionId());
            if (question == null) {
                throw new BusinessValidationException(
                        "Checklist question does not belong to this inspection template");
            }
            if (!question.isActive()) {
                throw new BusinessValidationException("Inactive checklist questions cannot be answered");
            }
            if (!SUPPORTED_TYPES.contains(question.getQuestionType())) {
                throw new BusinessValidationException(
                        "Answers for question type " + question.getQuestionType() + " are not supported yet");
            }
            validateValueMatchesQuestionType(question, request);
        }
    }

    private void validateValueMatchesQuestionType(
            InspectionTemplateQuestion question,
            InspectionAnswerRequest request) {
        if (hasMultipleValues(request)) {
            throw new BusinessValidationException(
                    "Checklist question '" + question.getCode() + "' accepts only one answer value");
        }

        switch (question.getQuestionType()) {
            case BOOLEAN -> {
                if (request.getBooleanValue() == null) {
                    throw new BusinessValidationException(
                            "Boolean checklist question '" + question.getCode() + "' requires a yes/no answer");
                }
            }
            case TEXT -> {
                if (request.getTextValue() == null || request.getTextValue().isBlank()) {
                    throw new BusinessValidationException(
                            "Text checklist question '" + question.getCode() + "' requires a text answer");
                }
                if (request.getTextValue().trim().length() > 4000) {
                    throw new BusinessValidationException("Text answer exceeds maximum length");
                }
            }
            case NUMBER -> {
                if (request.getNumberValue() == null) {
                    throw new BusinessValidationException(
                            "Number checklist question '" + question.getCode() + "' requires a numeric answer");
                }
                InspectionTemplateQuestionNumberConstraints.validateNumberAnswer(question, request.getNumberValue());
            }
            case CHOICE -> {
                if (request.getChoiceCodeValue() == null || request.getChoiceCodeValue().isBlank()) {
                    throw new BusinessValidationException(
                            "Choice checklist question '" + question.getCode() + "' requires a selected option");
                }
                String choiceCode = request.getChoiceCodeValue().trim().toUpperCase();
                InspectionTemplateQuestionChoice choice = choiceRepository
                        .findByQuestionIdAndCode(question.getId(), choiceCode)
                        .orElseThrow(() -> new BusinessValidationException(
                                "Invalid choice for checklist question '" + question.getCode() + "'"));
                if (!choice.isActive()) {
                    throw new BusinessValidationException(
                            "Inactive choice cannot be selected for checklist question '"
                                    + question.getCode() + "'");
                }
            }
            default -> throw new BusinessValidationException(
                    "Answers for question type " + question.getQuestionType() + " are not supported yet");
        }
    }

    private boolean hasMultipleValues(InspectionAnswerRequest request) {
        int populated = 0;
        if (request.getBooleanValue() != null) {
            populated++;
        }
        if (request.getTextValue() != null && !request.getTextValue().isBlank()) {
            populated++;
        }
        if (request.getNumberValue() != null) {
            populated++;
        }
        if (request.getChoiceCodeValue() != null && !request.getChoiceCodeValue().isBlank()) {
            populated++;
        }
        return populated != 1;
    }

    private InspectionAnswer buildAnswer(
            Inspection inspection,
            InspectionTemplateQuestion question,
            InspectionAnswerRequest request) {
        Boolean booleanValue = null;
        String textValue = null;
        BigDecimal numberValue = null;
        String choiceCodeValue = null;
        String choiceLabelSnapshot = null;
        String numberUnitSnapshot = null;
        BigDecimal numberMinSnapshot = null;
        BigDecimal numberMaxSnapshot = null;
        Integer decimalPlacesSnapshot = null;
        String unitCodeSnapshot = null;
        String unitSymbolSnapshot = null;
        String unitNameSnapshot = null;
        Integer questionVersionSnapshot = question.getInspectionTemplate().getVersion();

        switch (question.getQuestionType()) {
            case BOOLEAN -> booleanValue = request.getBooleanValue();
            case TEXT -> textValue = request.getTextValue().trim();
            case NUMBER -> {
                numberValue = request.getNumberValue();
                numberMinSnapshot = question.getMinValue();
                numberMaxSnapshot = question.getMaxValue();
                decimalPlacesSnapshot = question.getDecimalPlaces();
                UnitOfMeasure unitOfMeasure = question.getUnitOfMeasure();
                if (unitOfMeasure != null) {
                    unitCodeSnapshot = unitOfMeasure.getCode();
                    unitSymbolSnapshot = unitOfMeasure.getSymbol();
                    unitNameSnapshot = unitOfMeasure.getName();
                    numberUnitSnapshot = unitOfMeasure.getSymbol();
                } else if (question.getUnit() != null) {
                    unitSymbolSnapshot = question.getUnit();
                    unitNameSnapshot = question.getUnit();
                    numberUnitSnapshot = question.getUnit();
                }
            }
            case CHOICE -> {
                choiceCodeValue = request.getChoiceCodeValue().trim().toUpperCase();
                choiceLabelSnapshot = choiceRepository.findByQuestionIdAndCode(question.getId(), choiceCodeValue)
                        .map(InspectionTemplateQuestionChoice::getLabel)
                        .orElseThrow(() -> new BusinessValidationException(
                                "Invalid choice for checklist question '" + question.getCode() + "'"));
            }
            default -> throw new BusinessValidationException(
                    "Answers for question type " + question.getQuestionType() + " are not supported yet");
        }

        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.valueOf(question.getQuestionType().name()),
                booleanValue,
                textValue,
                numberValue,
                choiceCodeValue,
                choiceLabelSnapshot,
                numberUnitSnapshot,
                numberMinSnapshot,
                numberMaxSnapshot,
                decimalPlacesSnapshot,
                unitCodeSnapshot,
                unitSymbolSnapshot,
                unitNameSnapshot,
                questionVersionSnapshot
        );
    }
}
