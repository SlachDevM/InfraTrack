package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionResponse;
import com.infratrack.inspectiontemplate.dto.ReorderInspectionTemplateQuestionsRequest;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionRequest;
import com.infratrack.unitofmeasure.QuantityType;
import com.infratrack.unitofmeasure.UnitOfMeasure;
import com.infratrack.unitofmeasure.UnitOfMeasureRepository;
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
class InspectionTemplateQuestionServiceTest {

    @Mock
    private InspectionTemplateRepository inspectionTemplateRepository;

    @Mock
    private InspectionTemplateQuestionRepository questionRepository;

    @Mock
    private InspectionTemplateQuestionChoiceRepository choiceRepository;

    @Mock
    private UnitOfMeasureRepository unitOfMeasureRepository;

    @InjectMocks
    private InspectionTemplateQuestionService questionService;

    @Test
    void create_shouldCreateQuestionOnDraftTemplate() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(100L)).thenReturn(List.of());
        when(questionRepository.existsByInspectionTemplateIdAndCode(100L, "VISIBLE_LEAK")).thenReturn(false);
        when(questionRepository.save(any(InspectionTemplateQuestion.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestion question = invocation.getArgument(0);
            question.setId(1L);
            return question;
        });

        CreateInspectionTemplateQuestionRequest request = createRequest();
        InspectionTemplateQuestionResponse response = questionService.create(100L, request);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getQuestionText()).isEqualTo("Is there any visible leak?");
        assertThat(response.getCode()).isEqualTo("VISIBLE_LEAK");
        assertThat(response.getQuestionType()).isEqualTo(InspectionTemplateQuestionType.BOOLEAN);
        assertThat(response.isRequired()).isFalse();
        assertThat(response.getDisplayOrder()).isEqualTo(1);
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void create_shouldRejectPublishedTemplate() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.PUBLISHED)));

        assertThatThrownBy(() -> questionService.create(100L, createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Checklist questions can only be modified on draft inspection templates");

        verify(questionRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectArchivedTemplate() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.ARCHIVED)));

        assertThatThrownBy(() -> questionService.create(100L, createRequest()))
                .isInstanceOf(ConflictException.class);

        verify(questionRepository, never()).save(any());
    }

    @Test
    void listByTemplateId_shouldReturnQuestionsSortedByDisplayOrder() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(100L))
                .thenReturn(List.of(question(2L, 2, true), question(1L, 1, true)));

        List<InspectionTemplateQuestionResponse> responses = questionService.listByTemplateId(100L);

        assertThat(responses).hasSize(2);
        assertThat(responses.get(0).getDisplayOrder()).isEqualTo(2);
        assertThat(responses.get(1).getDisplayOrder()).isEqualTo(1);
    }

    @Test
    void create_shouldAcceptNumberConstraintsOnNumberQuestion() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        UnitOfMeasure celsius = celsiusUnit();
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(100L)).thenReturn(List.of());
        when(questionRepository.existsByInspectionTemplateIdAndCode(100L, "TEMPERATURE")).thenReturn(false);
        when(unitOfMeasureRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.of(celsius));
        when(questionRepository.save(any(InspectionTemplateQuestion.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestion question = invocation.getArgument(0);
            question.setId(2L);
            return question;
        });

        CreateInspectionTemplateQuestionRequest request = createRequest();
        request.setCode("TEMPERATURE");
        request.setQuestionText("Temperature");
        request.setQuestionType(InspectionTemplateQuestionType.NUMBER);
        request.setUnitOfMeasureId(1L);
        request.setMinValue(new java.math.BigDecimal("0"));
        request.setMaxValue(new java.math.BigDecimal("120"));
        request.setDecimalPlaces(1);

        InspectionTemplateQuestionResponse response = questionService.create(100L, request);

        assertThat(response.getUnitOfMeasureId()).isEqualTo(1L);
        assertThat(response.getUnitSymbol()).isEqualTo("°C");
        assertThat(response.getUnitName()).isEqualTo("Celsius");
        assertThat(response.getMinValue()).isEqualByComparingTo("0");
        assertThat(response.getMaxValue()).isEqualByComparingTo("120");
        assertThat(response.getDecimalPlaces()).isEqualTo(1);
    }

    @Test
    void create_shouldRejectNumberConstraintsOnNonNumberQuestion() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.DRAFT)));

        CreateInspectionTemplateQuestionRequest request = createRequest();
        request.setUnitOfMeasureId(1L);

        assertThatThrownBy(() -> questionService.create(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Number constraints apply only to NUMBER checklist questions");
    }

    @Test
    void create_shouldRejectInactiveUnitOfMeasure() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.DRAFT)));
        when(unitOfMeasureRepository.findByIdAndActiveTrue(1L)).thenReturn(Optional.empty());

        CreateInspectionTemplateQuestionRequest request = createRequest();
        request.setQuestionType(InspectionTemplateQuestionType.NUMBER);
        request.setUnitOfMeasureId(1L);

        assertThatThrownBy(() -> questionService.create(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Active unit of measure not found");
    }

    @Test
    void create_shouldRejectInvalidNumberConstraintRange() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.DRAFT)));

        CreateInspectionTemplateQuestionRequest request = createRequest();
        request.setQuestionType(InspectionTemplateQuestionType.NUMBER);
        request.setMinValue(new java.math.BigDecimal("120"));
        request.setMaxValue(new java.math.BigDecimal("0"));

        assertThatThrownBy(() -> questionService.create(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Minimum value cannot exceed maximum value");
    }

    @Test
    void update_shouldUpdateQuestionOnDraftTemplate() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = question(1L, 1, true);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(InspectionTemplateQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UpdateInspectionTemplateQuestionRequest request = new UpdateInspectionTemplateQuestionRequest();
        request.setQuestionText("Updated question");
        request.setQuestionType(InspectionTemplateQuestionType.TEXT);
        request.setRequired(true);

        InspectionTemplateQuestionResponse response = questionService.update(100L, 1L, request);

        assertThat(response.getQuestionText()).isEqualTo("Updated question");
        assertThat(response.getQuestionType()).isEqualTo(InspectionTemplateQuestionType.TEXT);
        assertThat(response.isRequired()).isTrue();
    }

    @Test
    void update_shouldRejectPublishedTemplate() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.PUBLISHED)));

        UpdateInspectionTemplateQuestionRequest request = new UpdateInspectionTemplateQuestionRequest();
        request.setQuestionText("Updated question");
        request.setQuestionType(InspectionTemplateQuestionType.TEXT);

        assertThatThrownBy(() -> questionService.update(100L, 1L, request))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deactivate_shouldDeactivateQuestionOnDraftTemplate() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = question(1L, 1, true);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(questionRepository.save(any(InspectionTemplateQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionTemplateQuestionResponse response = questionService.deactivate(100L, 1L);

        assertThat(response.isActive()).isFalse();
    }

    @Test
    void deactivate_shouldRejectInactiveQuestion() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L))
                .thenReturn(Optional.of(question(1L, 1, false)));

        assertThatThrownBy(() -> questionService.deactivate(100L, 1L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Inactive checklist questions cannot be modified");
    }

    @Test
    void reorder_shouldUpdateDisplayOrderOnDraftTemplate() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion q1 = question(1L, 1, true);
        InspectionTemplateQuestion q2 = question(2L, 2, true);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(100L))
                .thenReturn(List.of(q1, q2))
                .thenReturn(List.of(reordered(q2, 1), reordered(q1, 2)));

        ReorderInspectionTemplateQuestionsRequest request = new ReorderInspectionTemplateQuestionsRequest();
        request.setOrderedQuestionIds(List.of(2L, 1L));

        List<InspectionTemplateQuestionResponse> responses = questionService.reorder(100L, request);

        assertThat(responses).hasSize(2);
        assertThat(q2.getDisplayOrder()).isEqualTo(1);
        assertThat(q1.getDisplayOrder()).isEqualTo(2);
        verify(questionRepository).saveAll(anyList());
    }

    @Test
    void reorder_shouldRejectMissingActiveQuestionIds() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(100L))
                .thenReturn(List.of(question(1L, 1, true), question(2L, 2, true)));

        ReorderInspectionTemplateQuestionsRequest request = new ReorderInspectionTemplateQuestionsRequest();
        request.setOrderedQuestionIds(List.of(1L));

        assertThatThrownBy(() -> questionService.reorder(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Reorder request must include all active questions for the template");
    }

    @Test
    void reorder_shouldRejectDuplicateIds() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(100L))
                .thenReturn(List.of(question(1L, 1, true), question(2L, 2, true)));

        ReorderInspectionTemplateQuestionsRequest request = new ReorderInspectionTemplateQuestionsRequest();
        request.setOrderedQuestionIds(List.of(1L, 1L));

        assertThatThrownBy(() -> questionService.reorder(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Reorder request contains duplicate question IDs");
    }

    @Test
    void reorder_shouldRejectIdsFromAnotherTemplate() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(100L))
                .thenReturn(List.of(question(1L, 1, true)));

        ReorderInspectionTemplateQuestionsRequest request = new ReorderInspectionTemplateQuestionsRequest();
        request.setOrderedQuestionIds(List.of(99L));

        assertThatThrownBy(() -> questionService.reorder(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Reorder request must include exactly the active questions for this template");
    }

    @Test
    void create_shouldRejectBlankQuestionText() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.DRAFT)));

        CreateInspectionTemplateQuestionRequest request = createRequest();
        request.setQuestionText("   ");

        assertThatThrownBy(() -> questionService.create(100L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Question text is required");
    }

    @Test
    void create_shouldRejectInvalidCode() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.DRAFT)));

        CreateInspectionTemplateQuestionRequest request = createRequest();
        request.setCode("invalid-code");

        assertThatThrownBy(() -> questionService.create(100L, request))
                .isInstanceOf(BusinessValidationException.class);

        verify(questionRepository, never()).save(any());
    }

    @Test
    void create_shouldRejectDuplicateCodeWithinTemplate() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.DRAFT)));
        when(questionRepository.existsByInspectionTemplateIdAndCode(100L, "VISIBLE_LEAK")).thenReturn(true);

        assertThatThrownBy(() -> questionService.create(100L, createRequest()))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Question code already exists for this template");

        verify(questionRepository, never()).save(any());
    }

    @Test
    void getByTemplate_shouldRejectMissingTemplate() {
        when(inspectionTemplateRepository.findDetailedById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> questionService.listByTemplateId(999L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Inspection template not found");
    }

    private CreateInspectionTemplateQuestionRequest createRequest() {
        CreateInspectionTemplateQuestionRequest request = new CreateInspectionTemplateQuestionRequest();
        request.setQuestionText("Is there any visible leak?");
        request.setCode("VISIBLE_LEAK");
        request.setQuestionType(InspectionTemplateQuestionType.BOOLEAN);
        return request;
    }

    private InspectionTemplate template(Long id, InspectionTemplateStatus status) {
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection Template",
                null,
                new com.infratrack.assetcategory.AssetCategory("Pump"),
                1,
                status
        );
        template.setId(id);
        return template;
    }

    private InspectionTemplateQuestion question(Long id, int displayOrder, boolean active) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template(100L, InspectionTemplateStatus.DRAFT),
                "Sample question",
                "SAMPLE_QUESTION",
                null,
                InspectionTemplateQuestionType.BOOLEAN,
                false,
                displayOrder
        );
        question.setId(id);
        if (!active) {
            question.setActive(false);
        }
        return question;
    }

    private InspectionTemplateQuestion reordered(InspectionTemplateQuestion question, int displayOrder) {
        question.setDisplayOrder(displayOrder);
        return question;
    }

    private UnitOfMeasure celsiusUnit() {
        UnitOfMeasure unit = new UnitOfMeasure("CELSIUS", "°C", "Celsius", QuantityType.TEMPERATURE);
        unit.setId(1L);
        return unit;
    }
}
