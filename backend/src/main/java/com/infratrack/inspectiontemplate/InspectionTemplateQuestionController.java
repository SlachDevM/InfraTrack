package com.infratrack.inspectiontemplate;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionResponse;
import com.infratrack.inspectiontemplate.dto.ReorderInspectionTemplateQuestionsRequest;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionRequest;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inspection-templates/{templateId}/questions")
@Tag(name = "Inspection Template Questions", description = "Checklist questions on inspection templates (V2 Domain Engine)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class InspectionTemplateQuestionController {

    private final InspectionTemplateQuestionService questionService;
    private final InspectionTemplateAuthorizationService authorizationService;

    public InspectionTemplateQuestionController(
            InspectionTemplateQuestionService questionService,
            InspectionTemplateAuthorizationService authorizationService) {
        this.questionService = questionService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @Operation(summary = "List checklist questions for a template", description = "Sorted by display order ascending.")
    @ApiResponse(responseCode = "200", description = "Question list")
    public ResponseEntity<List<InspectionTemplateQuestionResponse>> listQuestions(
            @PathVariable Long templateId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewTemplates(userId);
        return ResponseEntity.ok(questionService.listByTemplateId(templateId));
    }

    @PostMapping
    @Operation(summary = "Create checklist question", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "201", description = "Question created")
    public ResponseEntity<InspectionTemplateQuestionResponse> createQuestion(
            @PathVariable Long templateId,
            @Valid @RequestBody CreateInspectionTemplateQuestionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.create(templateId, request));
    }

    @PutMapping("/{questionId}")
    @Operation(summary = "Update checklist question", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Question updated")
    public ResponseEntity<InspectionTemplateQuestionResponse> updateQuestion(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @Valid @RequestBody UpdateInspectionTemplateQuestionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(questionService.update(templateId, questionId, request));
    }

    @PostMapping("/{questionId}/deactivate")
    @Operation(summary = "Deactivate checklist question", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Question deactivated")
    public ResponseEntity<InspectionTemplateQuestionResponse> deactivateQuestion(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(questionService.deactivate(templateId, questionId));
    }

    @PostMapping("/reorder")
    @Operation(summary = "Reorder active checklist questions", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Questions reordered")
    public ResponseEntity<List<InspectionTemplateQuestionResponse>> reorderQuestions(
            @PathVariable Long templateId,
            @Valid @RequestBody ReorderInspectionTemplateQuestionsRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(questionService.reorder(templateId, request));
    }
}
