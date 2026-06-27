package com.infratrack.completionreview;

import com.infratrack.completionreview.dto.CompletionReviewResponse;
import com.infratrack.completionreview.dto.RecordCompletionReviewRequest;
import com.infratrack.config.openapi.StandardApiResponses;
import com.infratrack.security.JwtAuthenticationToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maintenance-activities")
@CrossOrigin(origins = "http://localhost:3000")
@Tag(name = "Completion Reviews", description = "Manager completion review of maintenance (UC-010)")
@StandardApiResponses
@SecurityRequirement(name = "bearerAuth")
public class CompletionReviewController {

    private final CompletionReviewService completionReviewService;

    public CompletionReviewController(CompletionReviewService completionReviewService) {
        this.completionReviewService = completionReviewService;
    }

    @PostMapping("/{id}/completion-review")
    @Operation(
            summary = "Record completion review",
            description = "Records a manager completion review decision for a maintenance activity (UC-010).")
    @ApiResponse(responseCode = "201", description = "Completion review recorded")
    public ResponseEntity<CompletionReviewResponse> recordCompletionReview(
            @PathVariable Long id,
            @Valid @RequestBody RecordCompletionReviewRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        CompletionReviewResponse response = completionReviewService.recordCompletionReview(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
