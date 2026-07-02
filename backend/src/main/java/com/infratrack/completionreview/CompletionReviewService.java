package com.infratrack.completionreview;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.completionreview.dto.CompletionReviewResponse;
import com.infratrack.completionreview.dto.RecordCompletionReviewRequest;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.issue.IssueType;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.time.WorkflowClock;
import com.infratrack.user.User;
import com.infratrack.workorder.WorkOrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Records manager completion reviews of maintenance activities (UC-010).
 * When the decision is {@link CompletionReviewDecision#REWORK_REQUIRED}, creates a traceable
 * rework Issue for the normal operational decision workflow (V2 Sprint A1.1 / A1.2).
 */
@Service
public class CompletionReviewService {

    private final CompletionReviewRepository completionReviewRepository;
    private final MaintenanceActivityRepository maintenanceActivityRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final CompletionReviewAuthorizationService authorizationService;
    private final IssueRepository issueRepository;
    private final OperationalEventNotificationService operationalEventNotificationService;
    private final WorkflowClock workflowClock;

    public CompletionReviewService(
            CompletionReviewRepository completionReviewRepository,
            MaintenanceActivityRepository maintenanceActivityRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            CompletionReviewAuthorizationService authorizationService,
            IssueRepository issueRepository,
            OperationalEventNotificationService operationalEventNotificationService,
            WorkflowClock workflowClock) {
        this.completionReviewRepository = completionReviewRepository;
        this.maintenanceActivityRepository = maintenanceActivityRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.authorizationService = authorizationService;
        this.issueRepository = issueRepository;
        this.operationalEventNotificationService = operationalEventNotificationService;
        this.workflowClock = workflowClock;
    }

    @Transactional
    public CompletionReviewResponse recordCompletionReview(
            Long maintenanceActivityId,
            RecordCompletionReviewRequest request,
            Long userId) {
        User manager = authorizationService.requireManager(userId);
        MaintenanceActivity maintenanceActivity = findMaintenanceActivityOrThrow(maintenanceActivityId);
        requireCompletedWorkOrder(maintenanceActivity);
        requireNoExistingCompletionReview(maintenanceActivityId);

        CompletionReviewDecision decision = validateDecision(request.getDecision());
        String reviewNotes = normalizeReviewNotes(request.getReviewNotes());
        LocalDateTime reviewedAt = workflowClock.now();
        IssueSeverity reworkSeverity = validateReworkSeverity(request, decision);
        String rootCause = normalizeOptionalText(request.getRootCause());
        String correctiveAction = normalizeOptionalText(request.getCorrectiveAction());
        String preventiveAction = normalizeOptionalText(request.getPreventiveAction());

        Asset asset = maintenanceActivity.getAsset();
        authorizationService.requireManagerAuthorizedForAsset(manager, asset, reviewedAt);
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

        Long reworkIssueId = null;
        if (decision == CompletionReviewDecision.REWORK_REQUIRED) {
            reworkIssueId = createReworkIssue(
                    completionReview,
                    asset,
                    reviewNotes,
                    manager,
                    reviewedAt,
                    reworkSeverity,
                    rootCause,
                    correctiveAction,
                    preventiveAction);
        }

        return CompletionReviewResponse.from(completionReview, reworkIssueId);
    }

    private Long createReworkIssue(
            CompletionReview completionReview,
            Asset asset,
            String reviewNotes,
            User manager,
            LocalDateTime reviewedAt,
            IssueSeverity severity,
            String rootCause,
            String correctiveAction,
            String preventiveAction) {
        if (issueRepository.existsBySourceCompletionReviewId(completionReview.getId())) {
            throw new ConflictException("A rework issue has already been created for this completion review");
        }

        String description = buildReworkIssueDescription(completionReview.getId(), reviewNotes);
        Issue reworkIssue = issueRepository.save(Issue.forRework(
                asset,
                completionReview,
                description,
                severity,
                manager.getId(),
                reviewedAt,
                rootCause,
                correctiveAction,
                preventiveAction
        ));

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.REWORK_ISSUE_CREATED,
                manager.getId(),
                reviewedAt.toLocalDate(),
                buildReworkIssueHistoryDetails(severity, rootCause)
        ));

        operationalEventNotificationService.notifyReworkIssueRequiresOperationalDecision(
                asset.getDepartment(),
                reworkIssue.getId());

        return reworkIssue.getId();
    }

    static String buildReworkIssueDescription(Long completionReviewId, String reviewNotes) {
        return "Rework required following Completion Review #%d: %s".formatted(completionReviewId, reviewNotes);
    }

    static String buildReworkIssueHistoryDetails(IssueSeverity severity, String rootCause) {
        StringBuilder details = new StringBuilder("Issue type: ")
                .append(IssueType.REWORK.name())
                .append(" | Severity: ")
                .append(severity.name());
        if (rootCause != null) {
            details.append(" | Root cause: ").append(rootCause);
        }
        return details.toString();
    }

    private MaintenanceActivity findMaintenanceActivityOrThrow(Long maintenanceActivityId) {
        return maintenanceActivityRepository.findById(maintenanceActivityId)
                .orElseThrow(() -> new NotFoundException("Maintenance activity not found"));
    }

    private void requireCompletedWorkOrder(MaintenanceActivity maintenanceActivity) {
        if (maintenanceActivity.getWorkOrder().getStatus() != WorkOrderStatus.COMPLETED) {
            throw new ConflictException(
                    "Completion review requires a completed work order");
        }
    }

    private void requireNoExistingCompletionReview(Long maintenanceActivityId) {
        if (completionReviewRepository.existsByMaintenanceActivityId(maintenanceActivityId)) {
            throw new ConflictException(
                    "A completion review has already been recorded for this maintenance activity");
        }
    }

    private CompletionReviewDecision validateDecision(CompletionReviewDecision decision) {
        if (decision == null) {
            throw new BusinessValidationException("Review decision is required");
        }
        return decision;
    }

    private IssueSeverity validateReworkSeverity(
            RecordCompletionReviewRequest request,
            CompletionReviewDecision decision) {
        if (decision != CompletionReviewDecision.REWORK_REQUIRED) {
            return null;
        }
        if (request.getReworkSeverity() == null) {
            throw new BusinessValidationException("Rework severity is required when rework is required");
        }
        return request.getReworkSeverity();
    }

    private String normalizeReviewNotes(String reviewNotes) {
        if (reviewNotes == null || reviewNotes.isBlank()) {
            throw new BusinessValidationException("Review notes are required");
        }
        return reviewNotes.trim();
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

