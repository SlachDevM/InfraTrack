package com.infratrack.inspectiontemplate;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionChoiceRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionChoiceResponse;
import com.infratrack.inspectiontemplate.dto.ReorderInspectionTemplateQuestionChoicesRequest;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionChoiceRequest;
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
class InspectionTemplateQuestionChoiceServiceTest {

    @Mock
    private InspectionTemplateRepository inspectionTemplateRepository;

    @Mock
    private InspectionTemplateQuestionRepository questionRepository;

    @Mock
    private InspectionTemplateQuestionChoiceRepository choiceRepository;

    @InjectMocks
    private InspectionTemplateQuestionChoiceService choiceService;

    @Test
    void create_shouldAddChoiceToDraftChoiceQuestion() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = choiceQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(choiceRepository.findByQuestionIdOrderByDisplayOrderAsc(1L)).thenReturn(List.of());
        when(choiceRepository.existsByQuestionIdAndCode(1L, "GOOD")).thenReturn(false);
        when(choiceRepository.save(any(InspectionTemplateQuestionChoice.class))).thenAnswer(invocation -> {
            InspectionTemplateQuestionChoice choice = invocation.getArgument(0);
            choice.setId(10L);
            return choice;
        });

        CreateInspectionTemplateQuestionChoiceRequest request = new CreateInspectionTemplateQuestionChoiceRequest();
        request.setCode("GOOD");
        request.setLabel("Good");

        InspectionTemplateQuestionChoiceResponse response = choiceService.create(100L, 1L, request);

        assertThat(response.getCode()).isEqualTo("GOOD");
        assertThat(response.getLabel()).isEqualTo("Good");
        assertThat(response.isActive()).isTrue();
    }

    @Test
    void create_shouldRejectNonChoiceQuestion() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = booleanQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));

        CreateInspectionTemplateQuestionChoiceRequest request = new CreateInspectionTemplateQuestionChoiceRequest();
        request.setCode("GOOD");
        request.setLabel("Good");

        assertThatThrownBy(() -> choiceService.create(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Choices apply only to CHOICE checklist questions");
    }

    @Test
    void create_shouldRejectDuplicateChoiceCode() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = choiceQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(choiceRepository.existsByQuestionIdAndCode(1L, "GOOD")).thenReturn(true);

        CreateInspectionTemplateQuestionChoiceRequest request = new CreateInspectionTemplateQuestionChoiceRequest();
        request.setCode("GOOD");
        request.setLabel("Good");

        assertThatThrownBy(() -> choiceService.create(100L, 1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Choice code already exists for this question");
    }

    @Test
    void create_shouldRejectPublishedTemplate() {
        when(inspectionTemplateRepository.findDetailedById(100L))
                .thenReturn(Optional.of(template(100L, InspectionTemplateStatus.PUBLISHED)));

        CreateInspectionTemplateQuestionChoiceRequest request = new CreateInspectionTemplateQuestionChoiceRequest();
        request.setCode("GOOD");
        request.setLabel("Good");

        assertThatThrownBy(() -> choiceService.create(100L, 1L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Checklist question choices can only be modified on draft inspection templates");
    }

    @Test
    void reorder_shouldRejectDuplicateChoiceIds() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = choiceQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(choiceRepository.findByQuestionIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(activeChoice(10L, 1), activeChoice(11L, 2)));

        ReorderInspectionTemplateQuestionChoicesRequest request = new ReorderInspectionTemplateQuestionChoicesRequest();
        request.setOrderedChoiceIds(List.of(10L, 10L));

        assertThatThrownBy(() -> choiceService.reorder(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Reorder request contains duplicate choice IDs");
    }

    @Test
    void reorder_shouldRejectMissingChoiceIds() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = choiceQuestion(1L, template);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(choiceRepository.findByQuestionIdOrderByDisplayOrderAsc(1L))
                .thenReturn(List.of(activeChoice(10L, 1), activeChoice(11L, 2)));

        ReorderInspectionTemplateQuestionChoicesRequest request = new ReorderInspectionTemplateQuestionChoicesRequest();
        request.setOrderedChoiceIds(List.of(10L));

        assertThatThrownBy(() -> choiceService.reorder(100L, 1L, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Reorder request must include all active choices for the question");
    }

    @Test
    void deactivate_shouldDeactivateActiveChoice() {
        InspectionTemplate template = template(100L, InspectionTemplateStatus.DRAFT);
        InspectionTemplateQuestion question = choiceQuestion(1L, template);
        InspectionTemplateQuestionChoice choice = activeChoice(10L, 1);
        when(inspectionTemplateRepository.findDetailedById(100L)).thenReturn(Optional.of(template));
        when(questionRepository.findByIdAndInspectionTemplateId(1L, 100L)).thenReturn(Optional.of(question));
        when(choiceRepository.findByIdAndQuestionId(10L, 1L)).thenReturn(Optional.of(choice));
        when(choiceRepository.save(any(InspectionTemplateQuestionChoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InspectionTemplateQuestionChoiceResponse response = choiceService.deactivate(100L, 1L, 10L);

        assertThat(response.isActive()).isFalse();
    }

    private InspectionTemplate template(Long id, InspectionTemplateStatus status) {
        com.infratrack.assetcategory.AssetCategory category = new com.infratrack.assetcategory.AssetCategory("Pump");
        category.setId(10L);
        InspectionTemplate template = new InspectionTemplate("Pump Inspection", null, category, 1, status);
        template.setId(id);
        return template;
    }

    private InspectionTemplateQuestion choiceQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Condition",
                "CONDITION",
                null,
                InspectionTemplateQuestionType.CHOICE,
                true,
                1
        );
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestion booleanQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Leak?",
                "LEAK",
                null,
                InspectionTemplateQuestionType.BOOLEAN,
                true,
                1
        );
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestionChoice activeChoice(Long id, int displayOrder) {
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(
                choiceQuestion(1L, template(100L, InspectionTemplateStatus.DRAFT)),
                "GOOD",
                "Good",
                displayOrder
        );
        choice.setId(id);
        return choice;
    }
}
