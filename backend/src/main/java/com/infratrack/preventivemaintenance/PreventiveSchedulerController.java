package com.infratrack.preventivemaintenance;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResultResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerStatusResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preventive-scheduler")
@Tag(name = "Preventive Scheduler", description = "Controlled preventive candidate generation (V2 Phase B)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class PreventiveSchedulerController {

    private final PreventiveSchedulerService schedulerService;
    private final PreventiveSchedulerAuthorizationService authorizationService;
    private final PreventiveSchedulerProperties properties;

    public PreventiveSchedulerController(
            PreventiveSchedulerService schedulerService,
            PreventiveSchedulerAuthorizationService authorizationService,
            PreventiveSchedulerProperties properties) {
        this.schedulerService = schedulerService;
        this.authorizationService = authorizationService;
        this.properties = properties;
    }

    @GetMapping("/status")
    @Operation(summary = "Get preventive scheduler enabled status")
    @ApiResponse(responseCode = "200", description = "Scheduler configuration status")
    public ResponseEntity<PreventiveSchedulerStatusResponse> getStatus(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewSchedulerRuns(userId);
        return ResponseEntity.ok(new PreventiveSchedulerStatusResponse(properties.isEnabled()));
    }

    @PostMapping("/run")
    @Operation(summary = "Run preventive scheduler manually")
    @ApiResponse(responseCode = "200", description = "Scheduler run result with counts")
    public ResponseEntity<PreventiveSchedulerRunResultResponse> runManually(Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanRunScheduler(userId);
        Long departmentId = authorizationService.resolveDepartmentScopeForManualRun(userId);
        return ResponseEntity.ok(schedulerService.runManual(userId, departmentId));
    }

    @GetMapping("/runs")
    @Operation(summary = "List preventive scheduler runs")
    @ApiResponse(responseCode = "200", description = "Paginated scheduler run history")
    public ResponseEntity<Page<PreventiveSchedulerRunResponse>> listRuns(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewSchedulerRuns(userId);
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "startedAt"));
        return ResponseEntity.ok(schedulerService.listRuns(pageable));
    }

    @GetMapping("/runs/{id}")
    @Operation(summary = "Get preventive scheduler run by ID")
    @ApiResponse(responseCode = "200", description = "Scheduler run detail")
    public ResponseEntity<PreventiveSchedulerRunResponse> getRun(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewSchedulerRuns(userId);
        return ResponseEntity.ok(schedulerService.getRun(id));
    }
}
