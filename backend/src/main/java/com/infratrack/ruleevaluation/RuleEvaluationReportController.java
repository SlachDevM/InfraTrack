package com.infratrack.ruleevaluation;

import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.ruleevaluation.dto.RuleEvaluationReportResponse;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/inspections/{inspectionId}/rule-evaluation/reports")
@Tag(name = "Rule Evaluation Reports", description = "Persisted decision rule evaluation reports (V2 Domain Engine A3.3)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class RuleEvaluationReportController {

    private final RuleEvaluationReportService reportService;

    public RuleEvaluationReportController(RuleEvaluationReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    @Operation(summary = "List rule evaluation reports for an inspection")
    @ApiResponse(responseCode = "200", description = "Report summaries ordered by evaluation time")
    public ResponseEntity<List<RuleEvaluationReportResponse>> listReports(
            @PathVariable Long inspectionId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(reportService.listReports(inspectionId, userId));
    }

    @GetMapping("/latest")
    @Operation(summary = "Get latest rule evaluation report for an inspection")
    @ApiResponse(responseCode = "200", description = "Latest report with results")
    public ResponseEntity<RuleEvaluationReportResponse> getLatestReport(
            @PathVariable Long inspectionId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(reportService.getLatestReport(inspectionId, userId));
    }

    @GetMapping("/{reportId}")
    @Operation(summary = "Get rule evaluation report by ID")
    @ApiResponse(responseCode = "200", description = "Report with results")
    public ResponseEntity<RuleEvaluationReportResponse> getReport(
            @PathVariable Long inspectionId,
            @PathVariable Long reportId,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(reportService.getReport(inspectionId, reportId, userId));
    }
}
