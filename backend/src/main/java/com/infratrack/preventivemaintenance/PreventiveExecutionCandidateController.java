package com.infratrack.preventivemaintenance;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.preventivemaintenance.dto.ApprovePreventiveCandidateRequest;
import com.infratrack.preventivemaintenance.dto.ApprovePreventiveCandidateResponse;
import com.infratrack.preventivemaintenance.dto.DismissPreventiveCandidateRequest;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationResultResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveExecutionCandidateResponse;
import com.infratrack.preventivemaintenance.dto.RejectPreventiveCandidateRequest;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/preventive-execution-candidates")
@Tag(name = "Preventive Execution Candidates", description = "Review queue for eligible preventive plans (V2 Phase B)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class PreventiveExecutionCandidateController {

    private final PreventiveExecutionCandidateService candidateService;
    private final PreventiveExecutionCandidateAuthorizationService authorizationService;
    private final PreventiveDecisionAssistantService decisionAssistantService;

    public PreventiveExecutionCandidateController(
            PreventiveExecutionCandidateService candidateService,
            PreventiveExecutionCandidateAuthorizationService authorizationService,
            PreventiveDecisionAssistantService decisionAssistantService) {
        this.candidateService = candidateService;
        this.authorizationService = authorizationService;
        this.decisionAssistantService = decisionAssistantService;
    }

    @PostMapping("/generate")
    @Operation(summary = "Generate execution candidates for all active eligible plans")
    @ApiResponse(responseCode = "200", description = "Generation results per active plan")
    public ResponseEntity<List<ExecutionCandidateGenerationResultResponse>> generateCandidates(
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanGenerateCandidates(userId);
        return ResponseEntity.ok(candidateService.generateCandidates());
    }

    @GetMapping
    @Operation(summary = "List preventive execution candidates")
    @ApiResponse(responseCode = "200", description = "Paginated candidate list")
    public ResponseEntity<Page<PreventiveExecutionCandidateResponse>> listCandidates(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Filter by candidate status") @RequestParam(required = false) ExecutionCandidateStatus status,
            @Parameter(description = "Filter by asset ID") @RequestParam(required = false) Long assetId,
            @Parameter(description = "Filter by preventive plan ID") @RequestParam(required = false) Long planId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewCandidates(userId);
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "evaluatedAt"));
        return ResponseEntity.ok(candidateService.listCandidates(status, assetId, planId, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get preventive execution candidate by ID")
    @ApiResponse(responseCode = "200", description = "Candidate detail")
    public ResponseEntity<PreventiveExecutionCandidateResponse> getCandidate(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewCandidates(userId);
        return ResponseEntity.ok(candidateService.getCandidate(id));
    }

    @PostMapping("/{id}/approve")
    @Operation(summary = "Approve preventive execution candidate and create Inspection")
    @ApiResponse(responseCode = "200", description = "Approved candidate with created inspection")
    public ResponseEntity<ApprovePreventiveCandidateResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody ApprovePreventiveCandidateRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionAssistantService.approve(id, request, userId));
    }

    @PostMapping("/{id}/reject")
    @Operation(summary = "Reject preventive execution candidate")
    @ApiResponse(responseCode = "200", description = "Rejected candidate")
    public ResponseEntity<PreventiveExecutionCandidateResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectPreventiveCandidateRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionAssistantService.reject(id, request, userId));
    }

    @PostMapping("/{id}/dismiss")
    @Operation(summary = "Dismiss preventive execution candidate")
    @ApiResponse(responseCode = "200", description = "Dismissed candidate")
    public ResponseEntity<PreventiveExecutionCandidateResponse> dismiss(
            @PathVariable Long id,
            @Valid @RequestBody DismissPreventiveCandidateRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionAssistantService.dismiss(id, request, userId));
    }
}
