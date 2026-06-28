package com.infratrack.issue;

import com.infratrack.config.PaginationSupport;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.issue.dto.CreateIssueRequest;
import com.infratrack.issue.dto.IssueResponse;
import com.infratrack.issue.dto.UpdateIssueCapaRequest;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/issues")
@Tag(name = "Issues", description = "Issue recording from completed inspections (UC-005)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class IssueController {

    private final IssueService issueService;

    public IssueController(IssueService issueService) {
        this.issueService = issueService;
    }

    @GetMapping
    @Operation(summary = "List issues", description = "Returns paginated recorded issues ordered by creation time.")
    @ApiResponse(responseCode = "200", description = "Paginated issue list")
    public ResponseEntity<Page<IssueResponse>> listIssues(
            @Parameter(description = "Zero-based page index") @RequestParam(required = false) Integer page,
            @Parameter(description = "Page size (max 100)") @RequestParam(required = false) Integer size,
            @Parameter(description = "When true, returns only issues eligible for operational decision (UC-007)")
            @RequestParam(required = false) Boolean eligibleForOperationalDecision,
            Authentication authentication) {
        Pageable pageable = PaginationSupport.pageable(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt"));
        if (Boolean.TRUE.equals(eligibleForOperationalDecision)) {
            Long userId = ((JwtAuthenticationToken) authentication).getUserId();
            return ResponseEntity.ok(issueService.listEligibleForOperationalDecisionPage(userId, pageable));
        }
        return ResponseEntity.ok(issueService.listPage(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get issue by ID")
    @ApiResponse(responseCode = "200", description = "Issue details")
    public ResponseEntity<IssueResponse> getIssue(@PathVariable Long id) {
        return ResponseEntity.ok(issueService.getById(id));
    }

    @PostMapping
    @Operation(
            summary = "Record issue",
            description = "Records an issue from a completed inspection (UC-005). At most one issue per inspection.")
    @ApiResponse(responseCode = "201", description = "Issue recorded")
    @ApiResponse(responseCode = "409", description = "Issue already recorded for this inspection")
    public ResponseEntity<IssueResponse> recordIssue(
            @Valid @RequestBody CreateIssueRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        IssueResponse response = issueService.recordIssue(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PatchMapping("/{id}/capa")
    @Operation(
            summary = "Update issue CAPA metadata",
            description = "Updates optional root cause, corrective action, preventive action, and lessons learned.")
    @ApiResponse(responseCode = "200", description = "Issue CAPA metadata updated")
    public ResponseEntity<IssueResponse> updateIssueCapa(
            @PathVariable Long id,
            @Valid @RequestBody UpdateIssueCapaRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        return ResponseEntity.ok(issueService.updateCapa(id, request, userId));
    }
}
