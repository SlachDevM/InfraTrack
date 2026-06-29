package com.infratrack.inspectiontemplate;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.inspectiontemplate.dto.DeactivateInspectionTemplateQuestionRuleRequest;
import com.infratrack.inspectiontemplate.dto.CreateInspectionTemplateQuestionRuleRequest;
import com.infratrack.inspectiontemplate.dto.InspectionTemplateQuestionRuleResponse;
import com.infratrack.inspectiontemplate.dto.UpdateInspectionTemplateQuestionRuleRequest;
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
@RequestMapping("/api/inspection-templates/{templateId}/questions/{questionId}/rules")
@Tag(name = "Inspection Template Question Rules", description = "Decision rule definitions on checklist questions (V2 Domain Engine)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class InspectionTemplateQuestionRuleController {

    private final InspectionTemplateQuestionRuleService ruleService;
    private final InspectionTemplateAuthorizationService authorizationService;

    public InspectionTemplateQuestionRuleController(
            InspectionTemplateQuestionRuleService ruleService,
            InspectionTemplateAuthorizationService authorizationService) {
        this.ruleService = ruleService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @Operation(summary = "List decision rules for a checklist question")
    @ApiResponse(responseCode = "200", description = "Rule list")
    public ResponseEntity<List<InspectionTemplateQuestionRuleResponse>> listRules(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewTemplates(userId);
        return ResponseEntity.ok(ruleService.listByQuestionId(templateId, questionId));
    }

    @PostMapping
    @Operation(summary = "Create decision rule", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "201", description = "Rule created")
    public ResponseEntity<InspectionTemplateQuestionRuleResponse> createRule(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @Valid @RequestBody CreateInspectionTemplateQuestionRuleRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ruleService.create(templateId, questionId, request));
    }

    @PutMapping("/{ruleId}")
    @Operation(summary = "Update decision rule", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Rule updated")
    public ResponseEntity<InspectionTemplateQuestionRuleResponse> updateRule(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @PathVariable Long ruleId,
            @Valid @RequestBody UpdateInspectionTemplateQuestionRuleRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(ruleService.update(templateId, questionId, ruleId, request));
    }

    @PostMapping("/{ruleId}/deactivate")
    @Operation(summary = "Deactivate decision rule", description = "Administrator only. Template must be DRAFT.")
    @ApiResponse(responseCode = "200", description = "Rule deactivated")
    public ResponseEntity<InspectionTemplateQuestionRuleResponse> deactivateRule(
            @PathVariable Long templateId,
            @PathVariable Long questionId,
            @PathVariable Long ruleId,
            @RequestBody(required = false) DeactivateInspectionTemplateQuestionRuleRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        String disabledReason = request == null ? null : request.getDisabledReason();
        return ResponseEntity.ok(ruleService.deactivate(templateId, questionId, ruleId, disabledReason));
    }
}
