package com.infratrack.inspection;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionAnswerResponse;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.inspection.dto.InspectionSummaryResponse;
import com.infratrack.inspection.dto.SaveInspectionAnswersRequest;
import com.infratrack.inspection.dto.SaveInspectionProgressRequest;
import com.infratrack.inspectiontemplate.DecisionRuleEvaluationService;
import com.infratrack.inspectiontemplate.dto.DecisionRuleEvaluationResult;
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
@RequestMapping("/api/inspections")
@Tag(name = "Inspections", description = "Inspection assignment and completion (UC-003, UC-004)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class InspectionController {

    private final InspectionService inspectionService;
    private final DecisionRuleEvaluationService decisionRuleEvaluationService;

    public InspectionController(
            InspectionService inspectionService,
            DecisionRuleEvaluationService decisionRuleEvaluationService) {
        this.inspectionService = inspectionService;
        this.decisionRuleEvaluationService = decisionRuleEvaluationService;
    }

    @GetMapping
    @Operation(summary = "List inspections", description = "Returns paginated inspection summaries ordered by creation time.")
    @ApiResponse(responseCode = "200", description = "Paginated inspection summaries")
    public ResponseEntity<Page<InspectionSummaryResponse>> listInspections(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "When true, returns only inspections eligible for issue recording (UC-005)")
            @RequestParam(required = false) Boolean eligibleForIssueRecording,
            Authentication authentication) {
        if (Boolean.TRUE.equals(eligibleForIssueRecording)) {
            Long userId = ((JwtAuthenticationToken) authentication).getUserId();
            Pageable pageable = PaginationSupport.pageable(
                    page,
                    size,
                    Sort.by(Sort.Direction.DESC, "completedAt"));
            return ResponseEntity.ok(inspectionService.listEligibleForIssueRecordingPage(userId, pageable));
        }
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(inspectionService.listPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get inspection by ID")
    @ApiResponse(responseCode = "200", description = "Inspection details")
    public ResponseEntity<InspectionResponse> getInspection(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(inspectionService.getById(id, userId));
    }

    @GetMapping("/{id}/rule-evaluation")
    @Operation(
            summary = "Evaluate decision rules for an inspection",
            description = "Returns in-memory rule evaluation results. No persistence or workflow side effects (A3.2).")
    @ApiResponse(responseCode = "200", description = "Rule evaluation results")
    public ResponseEntity<List<DecisionRuleEvaluationResult>> evaluateRules(
            @PathVariable Long id,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(decisionRuleEvaluationService.evaluateInspection(id, userId));
    }

    @PostMapping
    @Operation(
            summary = "Assign inspection",
            description = "Assigns an inspection to a field employee or contractor (UC-003). Requires Operational Coordinator.")
    @ApiResponse(responseCode = "201", description = "Inspection assigned")
    public ResponseEntity<InspectionResponse> assignInspection(
            @Valid @RequestBody AssignInspectionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        InspectionResponse response = inspectionService.assignInspection(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/answers")
    @Operation(
            summary = "Save inspection answers progressively",
            description = "Upserts structured checklist answers on an assigned inspection without completing it.")
    @ApiResponse(responseCode = "200", description = "Current saved answers")
    public ResponseEntity<List<InspectionAnswerResponse>> saveInspectionAnswers(
            @PathVariable Long id,
            @Valid @RequestBody SaveInspectionAnswersRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        List<InspectionAnswerResponse> answers = inspectionService.saveInspectionAnswers(id, request, userId);
        return ResponseEntity.ok(answers);
    }

    @PutMapping("/{id}/progress")
    @Operation(
            summary = "Save inspection progress",
            description = "Saves draft inspection summary fields and/or checklist answers without completing the inspection.")
    @ApiResponse(responseCode = "200", description = "Saved inspection progress")
    public ResponseEntity<InspectionResponse> saveInspectionProgress(
            @PathVariable Long id,
            @Valid @RequestBody SaveInspectionProgressRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(inspectionService.saveInspectionProgress(id, request, userId));
    }

    @PostMapping("/{id}/complete")
    @Operation(
            summary = "Complete inspection",
            description = "Records field inspection results including physical condition and issue identification (UC-004).")
    @ApiResponse(responseCode = "200", description = "Inspection completed")
    public ResponseEntity<InspectionResponse> completeInspection(
            @PathVariable Long id,
            @Valid @RequestBody CompleteInspectionRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        InspectionResponse response = inspectionService.completeInspection(id, request, userId);
        return ResponseEntity.ok(response);
    }
}
