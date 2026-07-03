package com.infratrack.inspection;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.inspection.dto.InspectionAnswerRequest;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.unitofmeasure.QuantityType;
import com.infratrack.unitofmeasure.UnitOfMeasure;
import com.infratrack.assetcategory.AssetCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionAnswerServiceTest {

    @Mock
    private InspectionAnswerRepository inspectionAnswerRepository;

    @Mock
    private InspectionTemplateQuestionRepository questionRepository;

    @Mock
    private InspectionTemplateQuestionChoiceRepository choiceRepository;

    private InspectionAnswerService inspectionAnswerService;

    @BeforeEach
    void setUp() {
        inspectionAnswerService = new InspectionAnswerService(
                inspectionAnswerRepository, questionRepository, choiceRepository);
    }

    @Test
    void saveAnswers_shouldRejectAnswersWithoutTemplate() {
        Inspection inspection = inspection(100L, null);

        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setBooleanValue(true);

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Structured answers are only supported for templated inspections");
    }

    @Test
    void saveAnswers_shouldPersistBooleanAnswerWithSnapshots() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = question(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setBooleanValue(true);

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> {
            InspectionAnswer answer = invocation.getArgument(0);
            answer.setId(500L);
            return answer;
        });
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenAnswer(invocation -> {
                    InspectionAnswer answer = new InspectionAnswer(
                            inspection,
                            question,
                            "LEAK",
                            "Is there a visible leak?",
                            InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                            true,
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
                            1);
                    answer.setId(500L);
                    return List.of(answer);
                });

        int saved = inspectionAnswerService.saveAnswers(inspection, List.of(request));

        assertThat(saved).isEqualTo(1);
        verify(inspectionAnswerRepository).save(argThat(answer ->
                "LEAK".equals(answer.getQuestionCodeSnapshot())
                        && Boolean.TRUE.equals(answer.getBooleanValue())));
    }

    @Test
    void upsertProgressiveAnswers_shouldUpdateExistingAnswer() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = question(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setBooleanValue(false);

        InspectionAnswer existing = new InspectionAnswer(
                inspection,
                question,
                "LEAK",
                "Is there a leak?",
                InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                true,
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
                1);
        existing.setId(500L);

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L))
                .thenReturn(Optional.of(existing));
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(existing));

        List<com.infratrack.inspection.dto.InspectionAnswerResponse> responses =
                inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        assertThat(responses).hasSize(1);
        verify(inspectionAnswerRepository).save(argThat(answer ->
                Boolean.FALSE.equals(answer.getBooleanValue())));
    }

    @Test
    void upsertProgressiveAnswers_shouldSupportPartialSave() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question1 = question(1L, inspection.getInspectionTemplate());
        InspectionTemplateQuestion question2 = question(2L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(2L);
        request.setBooleanValue(true);

        InspectionAnswer existingAnswer = new InspectionAnswer(
                inspection,
                question1,
                "LEAK",
                "Is there a leak?",
                InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                true,
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
                1);
        existingAnswer.setId(500L);

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question1, question2));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 2L))
                .thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> {
            InspectionAnswer answer = invocation.getArgument(0);
            answer.setId(501L);
            return answer;
        });
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(existingAnswer));

        inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        verify(inspectionAnswerRepository).save(any(InspectionAnswer.class));
        verify(inspectionAnswerRepository, never()).delete(any());
    }

    @Test
    void upsertProgressiveAnswers_shouldIgnoreEmptyAnswerPayload() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);

        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        List<com.infratrack.inspection.dto.InspectionAnswerResponse> responses =
                inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        assertThat(responses).isEmpty();
        verify(questionRepository, never()).findByInspectionTemplateIdOrderByDisplayOrderAsc(anyLong());
        verify(inspectionAnswerRepository, never()).save(any(InspectionAnswer.class));
    }

    @Test
    void upsertProgressiveAnswers_shouldIgnoreNullBooleanValue() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setBooleanValue(null);

        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        verify(inspectionAnswerRepository, never()).save(any(InspectionAnswer.class));
    }

    @Test
    void upsertProgressiveAnswers_shouldPersistTextAnswer() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = textQuestion(3L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(3L);
        request.setTextValue("Leak near valve.");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 3L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenAnswer(invocation -> List.of(savedTextAnswer(inspection, question)));

        inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        verify(inspectionAnswerRepository).save(argThat(answer ->
                "Leak near valve.".equals(answer.getTextValue())));
    }

    @Test
    void upsertProgressiveAnswers_shouldPersistNumberAnswer() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = numberQuestion(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setNumberValue(new BigDecimal("42"));

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(savedNumberAnswer(inspection, question)));

        inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        verify(inspectionAnswerRepository).save(argThat(answer ->
                new BigDecimal("42").equals(answer.getNumberValue())));
    }

    @Test
    void upsertProgressiveAnswers_shouldPersistChoiceAnswer() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = choiceQuestion(2L, inspection.getInspectionTemplate());
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(
                question, "GOOD", "Good", 1);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(2L);
        request.setChoiceCodeValue("GOOD");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(choiceRepository.findByQuestionIdAndCode(2L, "GOOD")).thenReturn(Optional.of(choice));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 2L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(savedChoiceAnswer(inspection, question)));

        inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        verify(inspectionAnswerRepository).save(argThat(answer ->
                "GOOD".equals(answer.getChoiceCodeValue())));
    }

    @Test
    void upsertProgressiveAnswers_shouldRejectWrongValueType() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = question(1L, inspection.getInspectionTemplate());
        question.setQuestionType(InspectionTemplateQuestionType.NUMBER);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setTextValue("not-a-number");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Number checklist question 'LEAK' requires a numeric answer");
    }

    @Test
    void upsertProgressiveAnswers_shouldIgnoreEmptyAnswersOnNonTemplatedInspection() {
        Inspection inspection = inspection(100L, null);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);

        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request));

        verify(inspectionAnswerRepository, never()).save(any(InspectionAnswer.class));
    }

    @Test
    void upsertProgressiveAnswers_shouldRejectCompletedInspectionTemplateMissing() {
        Inspection inspection = inspection(100L, null);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setBooleanValue(true);

        assertThatThrownBy(() -> inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Structured answers are only supported for templated inspections");
    }

    @Test
    void saveAnswers_shouldRejectDuplicateAnswerInSameRequest() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = question(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request1 = new InspectionAnswerRequest();
        request1.setQuestionId(1L);
        request1.setBooleanValue(true);
        InspectionAnswerRequest request2 = new InspectionAnswerRequest();
        request2.setQuestionId(1L);
        request2.setBooleanValue(false);

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request1, request2)))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Duplicate answer submitted for the same checklist question");
    }

    @Test
    void upsertProgressiveAnswers_shouldRejectInvalidQuestion() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = question(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(99L);
        request.setBooleanValue(true);

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.upsertProgressiveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Checklist question does not belong to this inspection template");
    }

    @Test
    void saveAnswers_shouldRejectPhotoQuestionAnswer() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = question(1L, inspection.getInspectionTemplate());
        question.setQuestionType(InspectionTemplateQuestionType.PHOTO);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setTextValue("photo");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answers for question type PHOTO are not supported yet");
    }

    @Test
    void saveAnswers_shouldRejectWrongValueType() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = question(1L, inspection.getInspectionTemplate());
        question.setQuestionType(InspectionTemplateQuestionType.NUMBER);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setTextValue("not-a-number");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Number checklist question 'LEAK' requires a numeric answer");
    }

    @Test
    void saveAnswers_shouldAcceptNumberAnswerWithSnapshots() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = numberQuestion(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setNumberValue(new BigDecimal("87.5"));

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(savedNumberAnswer(inspection, question)));

        int saved = inspectionAnswerService.saveAnswers(inspection, List.of(request));

        assertThat(saved).isEqualTo(1);
        verify(inspectionAnswerRepository).save(argThat(answer ->
                new BigDecimal("87.5").equals(answer.getNumberValue())
                        && "CELSIUS".equals(answer.getUnitCodeSnapshot())
                        && "°C".equals(answer.getUnitSymbolSnapshot())
                        && "Celsius".equals(answer.getUnitNameSnapshot())
                        && "°C".equals(answer.getNumberUnitSnapshot())
                        && new BigDecimal("0").equals(answer.getNumberMinSnapshot())
                        && new BigDecimal("120").equals(answer.getNumberMaxSnapshot())
                        && Integer.valueOf(1).equals(answer.getDecimalPlacesSnapshot())
                        && Integer.valueOf(1).equals(answer.getQuestionVersionSnapshot())));
    }

    @Test
    void saveAnswers_shouldRejectNumberBelowMin() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = numberQuestion(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setNumberValue(new BigDecimal("-1"));

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answer for 'TEMPERATURE' is below the minimum allowed value");
    }

    @Test
    void saveAnswers_shouldRejectNumberAboveMax() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = numberQuestion(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setNumberValue(new BigDecimal("121"));

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answer for 'TEMPERATURE' exceeds the maximum allowed value");
    }

    @Test
    void saveAnswers_shouldRejectNumberWithTooManyDecimals() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = numberQuestion(1L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(1L);
        request.setNumberValue(new BigDecimal("87.55"));

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answer for 'TEMPERATURE' exceeds allowed decimal places");
    }

    @Test
    void saveAnswers_shouldAcceptChoiceAnswerWithLabelSnapshot() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = choiceQuestion(2L, inspection.getInspectionTemplate());
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(
                question, "GOOD", "Good", 1);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(2L);
        request.setChoiceCodeValue("GOOD");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(choiceRepository.findByQuestionIdAndCode(2L, "GOOD")).thenReturn(Optional.of(choice));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 2L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(savedChoiceAnswer(inspection, question)));

        int saved = inspectionAnswerService.saveAnswers(inspection, List.of(request));

        assertThat(saved).isEqualTo(1);
        verify(inspectionAnswerRepository).save(argThat(answer ->
                "GOOD".equals(answer.getChoiceCodeValue())
                        && "Good".equals(answer.getChoiceLabelSnapshot())));
    }

    @Test
    void saveAnswers_shouldRejectInvalidChoiceCode() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = choiceQuestion(2L, inspection.getInspectionTemplate());
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(2L);
        request.setChoiceCodeValue("UNKNOWN");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(choiceRepository.findByQuestionIdAndCode(2L, "UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Invalid choice for checklist question 'CONDITION'");
    }

    @Test
    void saveAnswers_shouldRejectInactiveChoice() {
        Inspection inspection = inspection(100L, template(50L));
        InspectionTemplateQuestion question = choiceQuestion(2L, inspection.getInspectionTemplate());
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(
                question, "GOOD", "Good", 1);
        choice.setActive(false);
        InspectionAnswerRequest request = new InspectionAnswerRequest();
        request.setQuestionId(2L);
        request.setChoiceCodeValue("GOOD");

        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(choiceRepository.findByQuestionIdAndCode(2L, "GOOD")).thenReturn(Optional.of(choice));

        assertThatThrownBy(() -> inspectionAnswerService.saveAnswers(inspection, List.of(request)))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Inactive choice cannot be selected for checklist question 'CONDITION'");
    }

    private Inspection inspection(Long id, InspectionTemplate template) {
        com.infratrack.asset.Asset asset = new com.infratrack.asset.Asset(
                "Pump 1",
                new com.infratrack.department.Department("Water"),
                new AssetCategory("Pump"),
                "Depot",
                com.infratrack.asset.AssetStatus.ACTIVE,
                java.time.LocalDate.now(),
                1L
        );
        com.infratrack.businesstrigger.BusinessTrigger trigger = new com.infratrack.businesstrigger.BusinessTrigger(
                asset,
                com.infratrack.businesstrigger.BusinessTriggerType.CUSTOMER_REQUEST,
                "Check pump",
                false,
                1L
        );
        Inspection inspection = new Inspection(
                asset,
                trigger,
                20L,
                10L,
                InspectionPriority.NORMAL,
                java.time.LocalDate.now().plusDays(3)
        );
        inspection.setId(id);
        if (template != null) {
            inspection.setInspectionTemplate(template);
        }
        return inspection;
    }

    private InspectionTemplate template(Long id) {
        AssetCategory category = new AssetCategory("Pump");
        category.setId(10L);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection",
                null,
                category,
                1,
                InspectionTemplateStatus.PUBLISHED
        );
        template.setId(id);
        return template;
    }

    private InspectionTemplateQuestion question(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Is there a visible leak?",
                "LEAK",
                null,
                InspectionTemplateQuestionType.BOOLEAN,
                true,
                1
        );
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion numberQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Temperature",
                "TEMPERATURE",
                null,
                InspectionTemplateQuestionType.NUMBER,
                true,
                1
        );
        question.setId(id);
        UnitOfMeasure celsius = new UnitOfMeasure("CELSIUS", "°C", "Celsius", QuantityType.TEMPERATURE);
        celsius.setId(1L);
        question.setUnitOfMeasure(celsius);
        question.setUnit("°C");
        question.setMinValue(new BigDecimal("0"));
        question.setMaxValue(new BigDecimal("120"));
        question.setDecimalPlaces(1);
        return question;
    }

    private InspectionTemplateQuestion choiceQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Condition",
                "CONDITION",
                null,
                InspectionTemplateQuestionType.CHOICE,
                true,
                2
        );
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion textQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Describe the issue",
                "DESCRIPTION",
                null,
                InspectionTemplateQuestionType.TEXT,
                true,
                3
        );
        question.setId(id);
        return question;
    }

    private InspectionAnswer savedTextAnswer(Inspection inspection, InspectionTemplateQuestion question) {
        InspectionAnswer answer = new InspectionAnswer(
                inspection,
                question,
                "DESCRIPTION",
                "Describe the issue",
                InspectionAnswerQuestionTypeSnapshot.TEXT,
                null,
                "Leak near valve.",
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
                1);
        answer.setId(502L);
        return answer;
    }

    private InspectionAnswer savedNumberAnswer(Inspection inspection, InspectionTemplateQuestion question) {
        InspectionAnswer answer = new InspectionAnswer(
                inspection,
                question,
                "TEMPERATURE",
                "Temperature",
                InspectionAnswerQuestionTypeSnapshot.NUMBER,
                null,
                null,
                new BigDecimal("87.5"),
                null,
                null,
                "°C",
                new BigDecimal("0"),
                new BigDecimal("120"),
                1,
                "CELSIUS",
                "°C",
                "Celsius",
                1);
        answer.setId(500L);
        return answer;
    }

    private InspectionAnswer savedChoiceAnswer(Inspection inspection, InspectionTemplateQuestion question) {
        InspectionAnswer answer = new InspectionAnswer(
                inspection,
                question,
                "CONDITION",
                "Condition",
                InspectionAnswerQuestionTypeSnapshot.CHOICE,
                null,
                null,
                null,
                "GOOD",
                "Good",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1);
        answer.setId(501L);
        return answer;
    }
}
