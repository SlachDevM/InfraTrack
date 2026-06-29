package com.infratrack.suggestedaction;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import com.infratrack.suggestedaction.dto.ApproveSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.ApproveSuggestedActionResponse;
import com.infratrack.suggestedaction.dto.DismissSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.RejectSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.SuggestedActionDetailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suggested-actions")
@Tag(name = "Decision Assistant", description = "Manager review of suggested actions (V2 Domain Engine A3.5)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class DecisionAssistantController {

    private final DecisionAssistantService decisionAssistantService;

    public DecisionAssistantController(DecisionAssistantService decisionAssistantService) {
        this.decisionAssistantService = decisionAssistantService;
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get suggested action detail with explanation")
    @ApiResponse(responseCode = "200", description = "Suggested action detail")
    public ResponseEntity<SuggestedActionDetailResponse> getSuggestedAction(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionAssistantService.getSuggestedAction(id, userId));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve suggested action and create Issue")
    @ApiResponse(responseCode = "200", description = "Approved suggestion and created issue")
    public ResponseEntity<ApproveSuggestedActionResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApproveSuggestedActionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionAssistantService.approve(id, request, userId));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject suggested action")
    @ApiResponse(responseCode = "200", description = "Rejected suggestion")
    public ResponseEntity<SuggestedActionDetailResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectSuggestedActionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionAssistantService.reject(id, request, userId));
    }

    @PostMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss suggested action")
    @ApiResponse(responseCode = "200", description = "Dismissed suggestion")
    public ResponseEntity<SuggestedActionDetailResponse> dismiss(
            @PathVariable Long id,
            @Valid @RequestBody DismissSuggestedActionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionAssistantService.dismiss(id, request, userId));
    }
}
