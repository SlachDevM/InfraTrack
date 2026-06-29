package com.infratrack.suggestedaction;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.suggestedaction.dto.SuggestedActionResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inspections/{inspectionId}/suggested-actions")
@Tag(name = "Suggested Actions", description = "Read-only suggestions from rule evaluation (V2 Domain Engine A3.4)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class SuggestedActionController {

    private final SuggestedActionService suggestedActionService;

    public SuggestedActionController(SuggestedActionService suggestedActionService) {
        this.suggestedActionService = suggestedActionService;
    }

    @GetMapping
    @Operation(summary = "List suggested actions for an inspection")
    @ApiResponse(responseCode = "200", description = "Suggested actions ordered by creation time")
    public ResponseEntity<List<SuggestedActionResponse>> listSuggestedActions(
            @PathVariable Long inspectionId,
            @RequestParam(required = false) SuggestedActionStatus status,
            @RequestParam(required = false) DecisionRuleActionType actionType,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(suggestedActionService.listSuggestedActions(
                inspectionId, userId, status, actionType));
    }

    @GetMapping("/{suggestedActionId}")
    @Operation(summary = "Get suggested action by ID")
    @ApiResponse(responseCode = "200", description = "Suggested action detail")
    public ResponseEntity<SuggestedActionResponse> getSuggestedAction(
            @PathVariable Long inspectionId,
            @PathVariable Long suggestedActionId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(suggestedActionService.getSuggestedAction(
                inspectionId, suggestedActionId, userId));
    }
}
