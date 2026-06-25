package com.infratrack.issue;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.dto.CreateIssueRequest;
import com.infratrack.issue.dto.IssueResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class IssueService {

    private final IssueRepository issueRepository;
    private final InspectionRepository inspectionRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;

    public IssueService(
            IssueRepository issueRepository,
            InspectionRepository inspectionRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService) {
        this.issueRepository = issueRepository;
        this.inspectionRepository = inspectionRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<IssueResponse> listAll() {
        return issueRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(IssueResponse::from)
                .toList();
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
        Issue issue = issueRepository.save(new Issue(
                inspection,
                asset,
                description,
                severity,
                recorder.getId(),
                recordedAt
        ));

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.ISSUE_RECORDED,
                recorder.getId(),
                recordedAt.toLocalDate()
        ));

        return IssueResponse.from(issue);
    }

    private Issue findIssueOrThrow(Long id) {
        return issueRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Issue not found"));
    }

    private Inspection findInspectionOrThrow(Long inspectionId) {
        if (inspectionId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inspection is required");
        }
        return inspectionRepository.findById(inspectionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Inspection not found"));
    }

    private User requireIssueRecorder(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only field employees and contractors can record issues");
        }
        return user;
    }

    private void requireCompletedWithIssueIdentified(Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Issues can only be recorded for completed inspections");
        }
        if (!inspection.isIssueIdentified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This inspection did not identify an issue");
        }
    }

    private void requireInspectionCompleter(User recorder, Inspection inspection) {
        if (inspection.getCompletedByUserId() == null
                || !inspection.getCompletedByUserId().equals(recorder.getId())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the user who completed the inspection can record the issue");
        }
    }

    private void requireNoExistingIssue(Long inspectionId) {
        if (issueRepository.existsByInspectionId(inspectionId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An issue has already been recorded for this inspection");
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue description is required");
        }
        return description.trim();
    }

    private IssueSeverity validateSeverity(IssueSeverity severity) {
        if (severity == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue severity is required");
        }
        return severity;
    }

    private LocalDateTime validateRecordedAt(LocalDateTime recordedAt, Inspection inspection) {
        if (recordedAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Recorded date and time are required");
        }
        if (inspection.getCompletedAt() != null && recordedAt.isBefore(inspection.getCompletedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recorded date and time cannot be before the inspection was completed");
        }
        if (recordedAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Recorded date and time cannot be in the future");
        }
        return recordedAt;
    }
}
