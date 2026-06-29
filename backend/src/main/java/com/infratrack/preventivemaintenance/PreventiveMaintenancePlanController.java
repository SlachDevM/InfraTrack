package com.infratrack.preventivemaintenance;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.preventivemaintenance.dto.CreatePreventiveMaintenancePlanRequest;
import com.infratrack.preventivemaintenance.dto.PreventiveMaintenancePlanResponse;
import com.infratrack.preventivemaintenance.dto.TriggerEvaluationResultResponse;
import com.infratrack.preventivemaintenance.dto.UpdatePreventiveMaintenancePlanRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/preventive-maintenance-plans")
@Tag(name = "Preventive Maintenance Plans", description = "Preventive maintenance plan definitions (V2 Phase B)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class PreventiveMaintenancePlanController {

    private final PreventiveMaintenancePlanService planService;
    private final TriggerEvaluationService triggerEvaluationService;
    private final PreventiveMaintenancePlanAuthorizationService authorizationService;

    public PreventiveMaintenancePlanController(
            PreventiveMaintenancePlanService planService,
            TriggerEvaluationService triggerEvaluationService,
            PreventiveMaintenancePlanAuthorizationService authorizationService) {
        this.planService = planService;
        this.triggerEvaluationService = triggerEvaluationService;
        this.authorizationService = authorizationService;
    }

    @GetMapping
    @Operation(summary = "List preventive maintenance plans")
    @ApiResponse(responseCode = "200", description = "Paginated plan list")
    public ResponseEntity<Page<PreventiveMaintenancePlanResponse>> listPlans(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Filter by asset ID") @RequestParam(required = false) Long assetId,
            @Parameter(description = "Filter by plan status") @RequestParam(required = false) PreventiveMaintenancePlanStatus status,
            @Parameter(description = "Filter by trigger type") @RequestParam(required = false) PlanTriggerType triggerType,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewPlans(userId);
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(planService.listPage(assetId, status, triggerType, pageable));
    }

    @GetMapping("/evaluation")
    @Operation(summary = "Evaluate all active preventive maintenance plans")
    @ApiResponse(responseCode = "200", description = "In-memory eligibility results for active plans")
    public ResponseEntity<List<TriggerEvaluationResultResponse>> evaluateAllPlans(
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewPlans(userId);
        return ResponseEntity.ok(triggerEvaluationService.evaluateAllPlans());
    }

    @GetMapping("/{id}/evaluation")
    @Operation(summary = "Evaluate preventive maintenance plan eligibility")
    @ApiResponse(responseCode = "200", description = "In-memory eligibility result")
    public ResponseEntity<TriggerEvaluationResultResponse> evaluatePlan(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewPlans(userId);
        return ResponseEntity.ok(triggerEvaluationService.evaluatePlan(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get preventive maintenance plan by ID")
    @ApiResponse(responseCode = "200", description = "Plan details including business trigger")
    public ResponseEntity<PreventiveMaintenancePlanResponse> getPlan(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewPlans(userId);
        return ResponseEntity.ok(planService.getById(id));
    }

    @PostMapping
    @Operation(summary = "Create preventive maintenance plan", description = "Administrator only.")
    @ApiResponse(responseCode = "201", description = "Plan created with business trigger")
    public ResponseEntity<PreventiveMaintenancePlanResponse> createPlan(
            @Valid @RequestBody CreatePreventiveMaintenancePlanRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(planService.create(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update preventive maintenance plan", description = "Administrator only.")
    @ApiResponse(responseCode = "200", description = "Plan updated")
    public ResponseEntity<PreventiveMaintenancePlanResponse> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody UpdatePreventiveMaintenancePlanRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(planService.update(id, request));
    }

    @PostMapping("/{id}/archive")
    @Operation(summary = "Archive preventive maintenance plan", description = "Administrator only.")
    @ApiResponse(responseCode = "200", description = "Plan archived")
    public ResponseEntity<PreventiveMaintenancePlanResponse> archivePlan(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireAdministrator(userId);
        return ResponseEntity.ok(planService.archive(id));
    }
}
