package com.infratrack.mobile;

import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository;
import com.infratrack.mobile.dto.MobileChoiceResponse;
import com.infratrack.mobile.dto.MobileQuestionResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionChoiceDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionQuestionDeltaResponse;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Loads inspection checklist definitions using the same filtering rules as the mobile bundle endpoint.
 */
@Component
public class MobileInspectionChecklistLoader {

    private final InspectionTemplateQuestionRepository questionRepository;
    private final InspectionTemplateQuestionChoiceRepository choiceRepository;

    public MobileInspectionChecklistLoader(
            InspectionTemplateQuestionRepository questionRepository,
            InspectionTemplateQuestionChoiceRepository choiceRepository) {
        this.questionRepository = questionRepository;
        this.choiceRepository = choiceRepository;
    }

    public List<MobileQuestionResponse> loadMobileQuestions(Long templateId) {
        if (templateId == null) {
            return List.of();
        }
        return toMobileQuestions(loadChecklistForTemplate(templateId));
    }

    public Map<Long, List<SyncInspectionQuestionDeltaResponse>> loadSyncQuestionsByTemplateIds(
            Collection<Long> templateIds) {
        return loadChecklistPayloadByTemplateIds(templateIds).questionsByTemplateId();
    }

    public Map<Long, Map<String, Long>> loadChoiceIdLookupByTemplateIds(Collection<Long> templateIds) {
        return loadChecklistPayloadByTemplateIds(templateIds).choiceIdByQuestionAndCode();
    }

    public ChecklistPayload loadChecklistPayloadByTemplateIds(Collection<Long> templateIds) {
        if (templateIds == null || templateIds.isEmpty()) {
            return ChecklistPayload.empty();
        }
        Map<Long, TemplateChecklist> checklists = loadChecklistsByTemplateIds(templateIds);
        Map<Long, List<SyncInspectionQuestionDeltaResponse>> questionsByTemplateId = new LinkedHashMap<>();
        Map<Long, Map<String, Long>> choiceIdByQuestionAndCode = new HashMap<>();
        for (Map.Entry<Long, TemplateChecklist> entry : checklists.entrySet()) {
            questionsByTemplateId.put(entry.getKey(), toSyncQuestions(entry.getValue()));
            for (Map.Entry<Long, List<InspectionTemplateQuestionChoice>> choiceEntry :
                    entry.getValue().allChoicesByQuestionId().entrySet()) {
                Map<String, Long> codes = choiceEntry.getValue().stream()
                        .collect(Collectors.toMap(
                                InspectionTemplateQuestionChoice::getCode,
                                InspectionTemplateQuestionChoice::getId,
                                (left, right) -> left));
                choiceIdByQuestionAndCode.put(choiceEntry.getKey(), codes);
            }
        }
        return new ChecklistPayload(questionsByTemplateId, choiceIdByQuestionAndCode);
    }

    public record ChecklistPayload(
            Map<Long, List<SyncInspectionQuestionDeltaResponse>> questionsByTemplateId,
            Map<Long, Map<String, Long>> choiceIdByQuestionAndCode) {

        public static ChecklistPayload empty() {
            return new ChecklistPayload(Map.of(), Map.of());
        }
    }

    private TemplateChecklist loadChecklistForTemplate(Long templateId) {
        return loadChecklistsByTemplateIds(List.of(templateId)).getOrDefault(
                templateId, TemplateChecklist.empty());
    }

    private Map<Long, TemplateChecklist> loadChecklistsByTemplateIds(Collection<Long> templateIds) {
        List<Long> distinctTemplateIds = templateIds.stream().distinct().toList();
        List<InspectionTemplateQuestion> questions = questionRepository
                .findByInspectionTemplateIdInOrderByInspectionTemplateIdAscDisplayOrderAsc(distinctTemplateIds)
                .stream()
                .filter(InspectionTemplateQuestion::isActive)
                .toList();
        if (questions.isEmpty()) {
            return Map.of();
        }

        List<Long> questionIds = questions.stream().map(InspectionTemplateQuestion::getId).toList();
        List<InspectionTemplateQuestionChoice> choices =
                choiceRepository.findByQuestionIdInOrderByQuestionIdAscDisplayOrderAsc(questionIds);

        Map<Long, List<InspectionTemplateQuestionChoice>> allChoicesByQuestionId = choices.stream()
                .collect(Collectors.groupingBy(choice -> choice.getQuestion().getId()));

        Map<Long, List<InspectionTemplateQuestionChoice>> activeChoicesByQuestionId = choices.stream()
                .filter(InspectionTemplateQuestionChoice::isActive)
                .collect(Collectors.groupingBy(choice -> choice.getQuestion().getId()));

        Map<Long, List<InspectionTemplateQuestion>> questionsByTemplateId = questions.stream()
                .collect(Collectors.groupingBy(
                        question -> question.getInspectionTemplate().getId(),
                        LinkedHashMap::new,
                        Collectors.toList()));

        Map<Long, TemplateChecklist> result = new LinkedHashMap<>();
        for (Map.Entry<Long, List<InspectionTemplateQuestion>> entry : questionsByTemplateId.entrySet()) {
            result.put(
                    entry.getKey(),
                    new TemplateChecklist(
                            entry.getValue(), activeChoicesByQuestionId, allChoicesByQuestionId));
        }
        return result;
    }

    private List<MobileQuestionResponse> toMobileQuestions(TemplateChecklist checklist) {
        if (checklist.questions().isEmpty()) {
            return List.of();
        }
        List<MobileQuestionResponse> responses = new ArrayList<>(checklist.questions().size());
        for (InspectionTemplateQuestion question : checklist.questions()) {
            List<MobileChoiceResponse> choices = checklist.activeChoicesByQuestionId()
                    .getOrDefault(question.getId(), List.of())
                    .stream()
                    .map(MobileChoiceResponse::from)
                    .toList();
            responses.add(MobileQuestionResponse.from(question, choices));
        }
        return responses;
    }

    private List<SyncInspectionQuestionDeltaResponse> toSyncQuestions(TemplateChecklist checklist) {
        if (checklist.questions().isEmpty()) {
            return List.of();
        }
        List<SyncInspectionQuestionDeltaResponse> responses = new ArrayList<>(checklist.questions().size());
        for (InspectionTemplateQuestion question : checklist.questions()) {
            List<SyncInspectionChoiceDeltaResponse> choices = checklist.activeChoicesByQuestionId()
                    .getOrDefault(question.getId(), List.of())
                    .stream()
                    .map(SyncInspectionChoiceDeltaResponse::from)
                    .toList();
            responses.add(SyncInspectionQuestionDeltaResponse.from(question, choices));
        }
        return responses;
    }

    private record TemplateChecklist(
            List<InspectionTemplateQuestion> questions,
            Map<Long, List<InspectionTemplateQuestionChoice>> activeChoicesByQuestionId,
            Map<Long, List<InspectionTemplateQuestionChoice>> allChoicesByQuestionId) {

        private static TemplateChecklist empty() {
            return new TemplateChecklist(List.of(), Map.of(), Map.of());
        }
    }
}
