package com.infratrack.completionreview;

import com.infratrack.completionreview.dto.CompletionReviewResponse;
import com.infratrack.completionreview.dto.RecordCompletionReviewRequest;
import com.infratrack.security.JwtAuthenticationToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/maintenance-activities")
@CrossOrigin(origins = "http://localhost:3000")
public class CompletionReviewController {

    private final CompletionReviewService completionReviewService;

    public CompletionReviewController(CompletionReviewService completionReviewService) {
        this.completionReviewService = completionReviewService;
    }

    @PostMapping("/{id}/completion-review")
    public ResponseEntity<CompletionReviewResponse> recordCompletionReview(
            @PathVariable Long id,
            @RequestBody RecordCompletionReviewRequest request,
            Authentication authentication) {
        Long userId = ((JwtAuthenticationToken) authentication).getUserId();
        CompletionReviewResponse response = completionReviewService.recordCompletionReview(id, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
