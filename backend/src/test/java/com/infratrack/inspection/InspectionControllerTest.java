package com.infratrack.inspection;

import com.infratrack.config.GlobalExceptionHandler;
import com.infratrack.config.SecurityConfig;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.dto.InspectionAnswerResponse;
import com.infratrack.inspectiontemplate.DecisionRuleEvaluationService;
import com.infratrack.security.JwtAuthenticationFilter;
import com.infratrack.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InspectionController.class)
@Import({GlobalExceptionHandler.class, SecurityConfig.class, JwtAuthenticationFilter.class, JwtTokenProvider.class})
@AutoConfigureMockMvc
@ActiveProfiles("test")
class InspectionControllerTest {

    private static final Long FIELD_USER_ID = 20L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private InspectionService inspectionService;

    @MockBean
    private DecisionRuleEvaluationService decisionRuleEvaluationService;

    @Test
    void saveInspectionAnswers_withoutToken_returnsForbidden() throws Exception {
        mockMvc.perform(put("/api/inspections/100/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden());
    }

    @Test
    void saveInspectionAnswers_success_returnsSavedAnswers() throws Exception {
        when(inspectionService.saveInspectionAnswers(eq(100L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID)))
                .thenReturn(List.of(answerResponse()));

        mockMvc.perform(put("/api/inspections/100/answers")
                        .header("Authorization", bearerToken(FIELD_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].questionId").value(12))
                .andExpect(jsonPath("$[0].booleanValue").value(true));
    }

    @Test
    void saveInspectionAnswers_invalidPayload_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/inspections/100/answers")
                        .header("Authorization", bearerToken(FIELD_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"answers\":[{\"questionId\":null,\"booleanValue\":true}]}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void saveInspectionAnswers_unknownInspection_returnsNotFound() throws Exception {
        when(inspectionService.saveInspectionAnswers(eq(999L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID)))
                .thenThrow(new NotFoundException("Inspection not found."));

        mockMvc.perform(put("/api/inspections/999/answers")
                        .header("Authorization", bearerToken(FIELD_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Inspection not found."));
    }

    @Test
    void saveInspectionAnswers_forbidden_returnsForbidden() throws Exception {
        when(inspectionService.saveInspectionAnswers(eq(100L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID)))
                .thenThrow(new ForbiddenOperationException("Only the assigned user can save inspection answers"));

        mockMvc.perform(put("/api/inspections/100/answers")
                        .header("Authorization", bearerToken(FIELD_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isForbidden())
                .andExpect(content().string("Only the assigned user can save inspection answers"));
    }

    @Test
    void saveInspectionAnswers_completedInspection_returnsConflict() throws Exception {
        when(inspectionService.saveInspectionAnswers(eq(100L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID)))
                .thenThrow(new ConflictException("Inspection answers cannot be modified after completion"));

        mockMvc.perform(put("/api/inspections/100/answers")
                        .header("Authorization", bearerToken(FIELD_USER_ID))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validPayload()))
                .andExpect(status().isConflict())
                .andExpect(content().string("Inspection answers cannot be modified after completion"));
    }

    private InspectionAnswerResponse answerResponse() {
        return InspectionAnswerResponse.from(savedAnswerEntity());
    }

    private InspectionAnswer savedAnswerEntity() {
        com.infratrack.asset.Asset asset = new com.infratrack.asset.Asset(
                "Pump",
                new com.infratrack.department.Department("Parks"),
                new com.infratrack.assetcategory.AssetCategory("Pump"),
                "Depot",
                com.infratrack.asset.AssetStatus.ACTIVE,
                java.time.LocalDate.now(),
                1L);
        com.infratrack.businesstrigger.BusinessTrigger trigger = new com.infratrack.businesstrigger.BusinessTrigger(
                asset,
                com.infratrack.businesstrigger.BusinessTriggerType.CUSTOMER_REQUEST,
                "Check",
                false,
                1L);
        Inspection inspection = new Inspection(
                asset,
                trigger,
                20L,
                10L,
                InspectionPriority.NORMAL,
                java.time.LocalDate.now().plusDays(3));
        inspection.setId(100L);
        com.infratrack.inspectiontemplate.InspectionTemplate template =
                new com.infratrack.inspectiontemplate.InspectionTemplate(
                        "Template",
                        null,
                        new com.infratrack.assetcategory.AssetCategory("Pump"),
                        1,
                        com.infratrack.inspectiontemplate.InspectionTemplateStatus.PUBLISHED);
        template.setId(50L);
        inspection.setInspectionTemplate(template);
        com.infratrack.inspectiontemplate.InspectionTemplateQuestion question =
                new com.infratrack.inspectiontemplate.InspectionTemplateQuestion(
                        template,
                        "Leak?",
                        "LEAK",
                        null,
                        com.infratrack.inspectiontemplate.InspectionTemplateQuestionType.BOOLEAN,
                        true,
                        1);
        question.setId(12L);
        InspectionAnswer answer = new InspectionAnswer(
                inspection,
                question,
                "LEAK",
                "Leak?",
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
        return answer;
    }

    private String validPayload() {
        return """
                {
                  "answers": [
                    {
                      "questionId": 12,
                      "booleanValue": true
                    }
                  ]
                }
                """;
    }

    private String bearerToken(Long userId) {
        return "Bearer " + jwtTokenProvider.generateToken(userId, "field@test.com");
    }
}
