package com.infratrack.issue;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.dto.CreateIssueRequest;
import com.infratrack.suggestedaction.SuggestedAction;
import com.infratrack.suggestedaction.dto.ApproveSuggestedActionRequest;
import com.infratrack.issue.dto.IssueResponse;
import com.infratrack.issue.dto.UpdateIssueCapaRequest;
import com.infratrack.time.WorkflowClock;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Records issues identified from completed inspections (UC-005).
 */
@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final InspectionRepository inspectionRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;
    private final WorkflowClock workflowClock;

    public IssueService(
            IssueRepository issueRepository,
            InspectionRepository inspectionRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService,
            WorkflowClock workflowClock) {
        this.issueRepository = issueRepository;
        this.inspectionRepository = inspectionRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
        this.workflowClock = workflowClock;
    }

    @Transactional(readOnly = true)
    public Page<IssueResponse> listPage(Pageable pageable) {
        return issueRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(IssueResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<IssueResponse> listEligibleForOperationalDecisionPage(Long userId, Pageable pageable) {
        User manager = requireManager(userId);
        Long managerDepartmentId = manager.getDepartment() != null
                ? manager.getDepartment().getId()
                : null;
        return issueRepository.findEligibleForOperationalDecision(
                        manager.getId(),
                        managerDepartmentId,
                        LocalDateTime.now(),
                        pageable)
                .map(IssueResponse::from);
    }

    @Transactional(readOnly = true)
    public IssueResponse getById(Long id) {
        return IssueResponse.from(findIssueOrThrow(id));
    }

    @Transactional
    public IssueResponse recordIssue(CreateIssueRequest request, Long userId) {
        User recorder = requireIssueRecorder(userId);
        Inspection inspection = findInspectionOrThrow(request.getInspectionId());
        requireCompletedWithIssueIdentified(inspection);
        requireInspectionCompleter(recorder, inspection);
        requireNoExistingIssue(inspection.getId());

        String description = normalizeDescription(request.getDescription());
        IssueSeverity severity = validateSeverity(request.getSeverity());
        LocalDateTime recordedAt = validateRecordedAt(request.getRecordedAt(), inspection);

        Asset asset = inspection.getAsset();
        Issue issue = new Issue(
                inspection,
                asset,
                description,
                severity,
                recorder.getId(),
                recordedAt
        );
        issue.applyCapaMetadata(
                normalizeOptionalText(request.getRootCause()),
                normalizeOptionalText(request.getCorrectiveAction()),
                normalizeOptionalText(request.getPreventiveAction()),
                normalizeOptionalText(request.getLessonsLearned()));
        issue = issueRepository.save(issue);

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.ISSUE_RECORDED,
                recorder.getId(),
                recordedAt.toLocalDate()
        ));

        return IssueResponse.from(issue);
    }

    @Transactional
    public IssueResponse recordIssueFromApprovedSuggestion(
            SuggestedAction suggestedAction,
            ApproveSuggestedActionRequest request,
            Long managerUserId) {
        User manager = requireManager(managerUserId);
        Inspection inspection = suggestedAction.getInspection();
        requireManagerAuthorizedForAsset(manager, inspection.getAsset());
        requireCompletedInspection(inspection);
        requireNoExistingIssue(inspection.getId());

        String description = buildIssueDescription(request.getTitle(), request.getDescription());
        IssueSeverity severity = validateSeverity(request.getSeverity());
        LocalDateTime recordedAt = workflowClock.now();

        Asset asset = inspection.getAsset();
        Issue issue = new Issue(
                inspection,
                asset,
                description,
                severity,
                manager.getId(),
                recordedAt
        );
        issue.applyCapaMetadata(
                normalizeOptionalText(request.getRootCause()),
                normalizeOptionalText(request.getCorrectiveAction()),
                normalizeOptionalText(request.getPreventiveAction()),
                null);
        issue.linkToDecisionAssistant(suggestedAction, suggestedAction.getReport());
        issue = issueRepository.save(issue);

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.ISSUE_RECORDED,
                manager.getId(),
                recordedAt.toLocalDate()
        ));
        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.SUGGESTED_ACTION_APPROVED,
                manager.getId(),
                recordedAt.toLocalDate(),
                "Suggested action " + suggestedAction.getId() + " approved; issue " + issue.getId() + " created."
        ));

        return IssueResponse.from(issue);
    }

    private void requireManagerAuthorizedForAsset(User manager, Asset asset) {
        Long managerDepartmentId = manager.getDepartment() != null
                ? manager.getDepartment().getId()
                : null;
        Long assetDepartmentId = asset.getDepartment().getId();
        if (managerDepartmentId == null || !managerDepartmentId.equals(assetDepartmentId)) {
            throw new ForbiddenOperationException(
                    "You may only review suggested actions for assets in your own department.");
        }
    }

    private void requireCompletedInspection(Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.COMPLETED) {
            throw new BusinessValidationException(
                    "Issues can only be created from completed inspections");
        }
    }

    private static String buildIssueDescription(String title, String description) {
        String normalizedTitle = title == null ? "" : title.trim();
        String normalizedDescription = description == null ? "" : description.trim();
        if (normalizedTitle.isEmpty()) {
            return normalizedDescription;
        }
        if (normalizedDescription.isEmpty()) {
            return normalizedTitle;
        }
        if (normalizedDescription.startsWith(normalizedTitle)) {
            return normalizedDescription;
        }
        return normalizedTitle + "\n\n" + normalizedDescription;
    }

    @Transactional
    public IssueResponse updateCapa(Long issueId, UpdateIssueCapaRequest request, Long userId) {
        User manager = requireManager(userId);
        Issue issue = findIssueOrThrow(issueId);
        requireManagerAuthorizedForIssueAsset(manager, issue);

        issue.applyCapaMetadata(
                normalizeOptionalText(request.getRootCause()),
                normalizeOptionalText(request.getCorrectiveAction()),
                normalizeOptionalText(request.getPreventiveAction()),
                normalizeOptionalText(request.getLessonsLearned()));

        return IssueResponse.from(issueRepository.save(issue));
    }

    private void requireManagerAuthorizedForIssueAsset(User manager, Issue issue) {
        Long managerDepartmentId = manager.getDepartment() != null
                ? manager.getDepartment().getId()
                : null;
        Long assetDepartmentId = issue.getAsset().getDepartment().getId();
        if (managerDepartmentId == null || !managerDepartmentId.equals(assetDepartmentId)) {
            throw new ForbiddenOperationException(
                    "You may only update issue CAPA metadata for assets in your own department.");
        }
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private Issue findIssueOrThrow(Long id) {
        return issueRepository.findDetailedById(id)
                .orElseThrow(() -> new NotFoundException("Issue not found"));
    }

    private User requireManager(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager()) {
            throw new ForbiddenOperationException("Only managers can make operational decisions");
        }
        return user;
    }

    private Inspection findInspectionOrThrow(Long inspectionId) {
        if (inspectionId == null) {
            throw new BusinessValidationException("Inspection is required");
        }
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new BusinessValidationException("Inspection not found"));
    }

    private User requireIssueRecorder(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            throw new ForbiddenOperationException(
                    "Only field employees and contractors can record issues");
        }
        return user;
    }

    private void requireCompletedWithIssueIdentified(Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.COMPLETED) {
            throw new BusinessValidationException(
                    "Issues can only be recorded for completed inspections");
        }
        if (!inspection.isIssueIdentified()) {
            throw new BusinessValidationException(
                    "This inspection did not identify an issue");
        }
    }

    private void requireInspectionCompleter(User recorder, Inspection inspection) {
        if (inspection.getCompletedByUserId() == null
                || !inspection.getCompletedByUserId().equals(recorder.getId())) {
            throw new ForbiddenOperationException(
                    "Only the user who completed the inspection can record the issue");
        }
    }

    private void requireNoExistingIssue(Long inspectionId) {
        if (issueRepository.existsByInspectionId(inspectionId)) {
            throw new ConflictException(
                    "An issue has already been recorded for this inspection");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new BusinessValidationException("Issue description is required");
        }
        return description.trim();
    }

    private IssueSeverity validateSeverity(IssueSeverity severity) {
        if (severity == null) {
            throw new BusinessValidationException("Issue severity is required");
        }
        return severity;
    }

    private LocalDateTime validateRecordedAt(LocalDateTime recordedAt, Inspection inspection) {
        if (recordedAt == null) {
            throw new BusinessValidationException("Recorded date and time are required");
        }
        if (inspection.getCompletedAt() != null && recordedAt.isBefore(inspection.getCompletedAt())) {
            throw new BusinessValidationException(
                    "Recorded date and time cannot be before the inspection was completed");
        }
        if (recordedAt.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException(
                    "Recorded date and time cannot be in the future");
        }
        return recordedAt;
    }
}
