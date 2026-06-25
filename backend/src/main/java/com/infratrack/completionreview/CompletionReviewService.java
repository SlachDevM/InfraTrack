package com.infratrack.completionreview;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.completionreview.dto.CompletionReviewResponse;
import com.infratrack.completionreview.dto.RecordCompletionReviewRequest;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrderStatus;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class CompletionReviewService {

    private final CompletionReviewRepository completionReviewRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;

    public CompletionReviewService(
            CompletionReviewRepository completionReviewRepository,
            MaintenanceActivityRepository maintenanceActivityRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService) {
        this.completionReviewRepository = completionReviewRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
    }

    @Transactional
    public CompletionReviewResponse recordCompletionReview(
            Long maintenanceActivityId,
            RecordCompletionReviewRequest request,
            Long userId) {
        User manager = requireManager(userId);
        MaintenanceActivity maintenanceActivity = findMaintenanceActivityOrThrow(maintenanceActivityId);
        requireCompletedWorkOrder(maintenanceActivity);
        requireNoExistingCompletionReview(maintenanceActivityId);

        CompletionReviewDecision decision = validateDecision(request.getDecision());
        String reviewNotes = normalizeReviewNotes(request.getReviewNotes());
        LocalDateTime reviewedAt = validateReviewedAt(request.getReviewedAt(), maintenanceActivity);

        Asset asset = maintenanceActivity.getAsset();
        CompletionReview completionReview = completionReviewRepository.save(new CompletionReview(
                maintenanceActivity,
                asset,
                decision,
                reviewNotes,
                manager.getId(),
                reviewedAt
        ));

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.COMPLETION_REVIEW_RECORDED,
                manager.getId(),
                reviewedAt.toLocalDate()
        ));

        return CompletionReviewResponse.from(completionReview);
    }

    private User requireManager(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers can record completion reviews");
        }
        return user;
    }

    private MaintenanceActivity findMaintenanceActivityOrThrow(Long maintenanceActivityId) {
        return maintenanceActivityRepository.findById(maintenanceActivityId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Maintenance activity not found"));
    }

    private void requireCompletedWorkOrder(MaintenanceActivity maintenanceActivity) {
        if (maintenanceActivity.getWorkOrder().getStatus() != WorkOrderStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Completion review requires a completed work order");
        }
    }

    private void requireNoExistingCompletionReview(Long maintenanceActivityId) {
        if (completionReviewRepository.existsByMaintenanceActivityId(maintenanceActivityId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A completion review has already been recorded for this maintenance activity");
        }
    }

    private CompletionReviewDecision validateDecision(CompletionReviewDecision decision) {
        if (decision == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review decision is required");
        }
        return decision;
    }

    private String normalizeReviewNotes(String reviewNotes) {
        if (reviewNotes == null || reviewNotes.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review notes are required");
        }
        return reviewNotes.trim();
    }

    private LocalDateTime validateReviewedAt(LocalDateTime reviewedAt, MaintenanceActivity maintenanceActivity) {
        if (reviewedAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Review date and time are required");
        }
        if (reviewedAt.isBefore(maintenanceActivity.getCompletedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review date and time cannot be before maintenance was completed");
        }
        if (reviewedAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Review date and time cannot be in the future");
        }
        return reviewedAt;
    }
}
