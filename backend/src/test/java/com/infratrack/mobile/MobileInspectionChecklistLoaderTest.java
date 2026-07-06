package com.infratrack.mobile;

import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.mobile.dto.MobileChoiceResponse;
import com.infratrack.mobile.dto.MobileQuestionResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionChoiceDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionQuestionDeltaResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileInspectionChecklistLoaderTest {

    private static final long TEMPLATE_ID = 50L;

    @Mock
    private InspectionTemplateQuestionRepository questionRepository;

    @Mock
    private InspectionTemplateQuestionChoiceRepository choiceRepository;

    private MobileInspectionChecklistLoader loader;

    @BeforeEach
    void setUp() {
        loader = new MobileInspectionChecklistLoader(questionRepository, choiceRepository);
    }

    @Test
    void loadMobileQuestions_matchesActiveQuestionsAndChoicesLikeBundleEndpoint() {
        InspectionTemplate template = template();
        InspectionTemplateQuestion activeQuestion = question(template, 10L, 1);
        InspectionTemplateQuestion inactiveQuestion = question(template, 11L, 2);
        inactiveQuestion.setActive(false);
        InspectionTemplateQuestionChoice activeChoice = choice(activeQuestion, 1L, "YES", "Yes", 1);
        InspectionTemplateQuestionChoice inactiveChoice = choice(activeQuestion, 2L, "NO", "No", 2);
        inactiveChoice.setActive(false);

        when(questionRepository.findByInspectionTemplateIdInOrderByInspectionTemplateIdAscDisplayOrderAsc(
                        List.of(TEMPLATE_ID)))
                .thenReturn(List.of(activeQuestion, inactiveQuestion));
        when(choiceRepository.findByQuestionIdInOrderByQuestionIdAscDisplayOrderAsc(List.of(10L)))
                .thenReturn(List.of(activeChoice, inactiveChoice));

        List<MobileQuestionResponse> mobileQuestions = loader.loadMobileQuestions(TEMPLATE_ID);
        MobileInspectionChecklistLoader.ChecklistPayload syncPayload =
                loader.loadChecklistPayloadByTemplateIds(Set.of(TEMPLATE_ID));

        assertThat(mobileQuestions).hasSize(1);
        assertThat(mobileQuestions.get(0).getId()).isEqualTo(10L);
        assertThat(mobileQuestions.get(0).getChoices()).hasSize(1);
        assertThat(mobileQuestions.get(0).getChoices().get(0).getCode()).isEqualTo("YES");

        List<SyncInspectionQuestionDeltaResponse> syncQuestions =
                syncPayload.questionsByTemplateId().get(TEMPLATE_ID);
        assertThat(syncQuestions).hasSize(1);
        assertThat(syncQuestions.get(0).getQuestionId()).isEqualTo(10L);
        assertThat(syncQuestions.get(0).getLabel()).isEqualTo(mobileQuestions.get(0).getQuestionText());
        assertThat(syncQuestions.get(0).getCode()).isEqualTo(mobileQuestions.get(0).getCode());
        assertThat(syncQuestions.get(0).getQuestionType()).isEqualTo(mobileQuestions.get(0).getType());
        assertThat(syncQuestions.get(0).getChoices()).hasSize(1);
        assertThat(syncQuestions.get(0).getChoices().get(0).getCode())
                .isEqualTo(mobileQuestions.get(0).getChoices().get(0).getCode());
        assertThat(syncQuestions.get(0).getChoices().get(0).getLabel())
                .isEqualTo(mobileQuestions.get(0).getChoices().get(0).getLabel());
        assertThat(syncQuestions.get(0).getChoices().get(0).isActive()).isTrue();
        assertThat(syncPayload.choiceIdByQuestionAndCode().get(10L)).containsEntry("NO", 2L);
    }

    @Test
    void loadChecklistPayloadByTemplateIds_batchesQuestionsAndChoicesForMultipleTemplates() {
        InspectionTemplate firstTemplate = template();
        InspectionTemplate secondTemplate = template();
        secondTemplate.setId(51L);
        InspectionTemplateQuestion firstQuestion = question(firstTemplate, 10L, 1);
        InspectionTemplateQuestion secondQuestion = question(secondTemplate, 20L, 1);

        when(questionRepository.findByInspectionTemplateIdInOrderByInspectionTemplateIdAscDisplayOrderAsc(any()))
                .thenReturn(List.of(firstQuestion, secondQuestion));
        when(choiceRepository.findByQuestionIdInOrderByQuestionIdAscDisplayOrderAsc(List.of(10L, 20L)))
                .thenReturn(List.of());

        loader.loadChecklistPayloadByTemplateIds(Set.of(TEMPLATE_ID, 51L));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<Long>> templateIdsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(questionRepository)
                .findByInspectionTemplateIdInOrderByInspectionTemplateIdAscDisplayOrderAsc(templateIdsCaptor.capture());
        assertThat(templateIdsCaptor.getValue()).containsExactlyInAnyOrder(TEMPLATE_ID, 51L);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Long>> questionIdsCaptor = ArgumentCaptor.forClass(List.class);
        verify(choiceRepository).findByQuestionIdInOrderByQuestionIdAscDisplayOrderAsc(questionIdsCaptor.capture());
        assertThat(questionIdsCaptor.getValue()).containsExactly(10L, 20L);
    }

    private InspectionTemplate template() {
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        InspectionTemplate template = new InspectionTemplate(
                "Daily Checklist",
                "Daily asset inspection",
                category,
                3,
                InspectionTemplateStatus.PUBLISHED);
        template.setId(TEMPLATE_ID);
        return template;
    }

    private InspectionTemplateQuestion question(InspectionTemplate template, long id, int displayOrder) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Is equipment safe?",
                "SAFE",
                "Check equipment safety",
                InspectionTemplateQuestionType.CHOICE,
                true,
                displayOrder);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestionChoice choice(
            InspectionTemplateQuestion question,
            long id,
            String code,
            String label,
            int displayOrder) {
        InspectionTemplateQuestionChoice choice =
                new InspectionTemplateQuestionChoice(question, code, label, displayOrder);
        choice.setId(id);
        return choice;
    }
}
