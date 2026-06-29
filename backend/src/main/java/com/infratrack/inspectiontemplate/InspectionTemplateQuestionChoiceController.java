package com.infratrack.inspectiontemplate;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionChoiceRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionChoiceResponse;
import com.infratrack.inspectiontemplate.dto.ReorderInspectionTemplateQuestionChoicesRequest;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionChoiceRequest;
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
@RequestMapping("/api/inspection-templates/{templateId}/questions/{questionId}/choices")
@Tag(name = "Inspection Template Question Choices", description = "Allowed choices on CHOICE checklist questions (V2 Domain Engine)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class InspectionTemplateQuestionChoiceController {

    private final InspectionTemplateQuestionChoiceService choiceService;
    private final InspectionTemplateAuthorizationService authorizationService;

    public InspectionTemplateQuestionChoiceController(
            InspectionTemplateQuestionChoiceService choiceService,
            InspectionTemplateAuthorizationService authorizationService) {
        this.choiceService = choiceService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @Operation(summary = "List choices for a CHOICE question")
    @ApiResponse(responseCode = "200", description = "Choice list")
    public ResponseEntity<List<InspectionTemplateQuestionChoiceResponse>> listChoices(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewTemplates(userId);
        return ResponseEntity.ok(choiceService.listByQuestionId(templateId, questionId));
    }

    @PostMapping
    @Operation(summary = "Create choice", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "201", description = "Choice created")
    public ResponseEntity<InspectionTemplateQuestionChoiceResponse> createChoice(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @Valid @RequestBody CreateInspectionTemplateQuestionChoiceRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(choiceService.create(templateId, questionId, request));
    }

    @PutMapping("/{choiceId}")
    @Operation(summary = "Update choice label", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Choice updated")
    public ResponseEntity<InspectionTemplateQuestionChoiceResponse> updateChoice(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @PathVariable Long choiceId,
            @Valid @RequestBody UpdateInspectionTemplateQuestionChoiceRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(choiceService.update(templateId, questionId, choiceId, request));
    }

    @PostMapping("/{choiceId}/deactivate")
    @Operation(summary = "Deactivate choice", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Choice deactivated")
    public ResponseEntity<InspectionTemplateQuestionChoiceResponse> deactivateChoice(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @PathVariable Long choiceId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(choiceService.deactivate(templateId, questionId, choiceId));
    }

    @PostMapping("/reorder")
    @Operation(summary = "Reorder active choices", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Choices reordered")
    public ResponseEntity<List<InspectionTemplateQuestionChoiceResponse>> reorderChoices(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @Valid @RequestBody ReorderInspectionTemplateQuestionChoicesRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(choiceService.reorder(templateId, questionId, request));
    }
}
