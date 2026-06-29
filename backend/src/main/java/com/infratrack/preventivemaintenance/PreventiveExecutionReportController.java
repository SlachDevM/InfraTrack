package com.infratrack.preventivemaintenance;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.PreventiveExecutionReportResponse;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/preventive-execution-reports")
@Tag(name = "Preventive Execution Reports", description = "Audit reports for preventive execution candidates (V2 Phase B)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class PreventiveExecutionReportController {

    private final PreventiveExecutionReportService reportService;
    private final PreventiveExecutionCandidateAuthorizationService authorizationService;
    private final PreventiveExecutionReportRepository reportRepository;

    public PreventiveExecutionReportController(
            PreventiveExecutionReportService reportService,
            PreventiveExecutionCandidateAuthorizationService authorizationService,
            PreventiveExecutionReportRepository reportRepository) {
        this.reportService = reportService;
        this.authorizationService = authorizationService;
        this.reportRepository = reportRepository;
    }

    @GetMapping
    @Operation(summary = "List preventive execution audit reports")
    @ApiResponse(responseCode = "200", description = "Paginated report list")
    public ResponseEntity<Page<PreventiveExecutionReportResponse>> listReports(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "Filter by report status") @RequestParam(required = false) ExecutionReportStatus status,
            @Parameter(description = "Filter by asset ID") @RequestParam(required = false) Long assetId,
            @Parameter(description = "Filter by preventive plan ID") @RequestParam(required = false) Long planId,
            @Parameter(description = "Filter by decision source") @RequestParam(required = false) DecisionSource decisionSource,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewCandidates(userId);
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "generatedAt"));
        return ResponseEntity.ok(reportService.listReports(status, assetId, planId, decisionSource, pageable));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get preventive execution audit report by ID")
    @ApiResponse(responseCode = "200", description = "Report detail")
    public ResponseEntity<PreventiveExecutionReportResponse> getReport(
            @PathVariable Long reportId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        authorizationService.requireCanViewCandidates(userId);
        PreventiveExecutionReport report = reportRepository.findDetailedById(reportId)
                .orElseThrow(() -> new NotFoundException("Preventive execution report not found"));
        authorizationService.requireAuthorizedToViewReportAsset(userId, report.getCandidate().getAsset());
        return ResponseEntity.ok(PreventiveExecutionReportResponse.from(report));
    }
}
