package com.infratrack.operationaldecision;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.operationaldecision.dto.CreateOperationalDecisionRequest;
import com.infratrack.operationaldecision.dto.OperationalDecisionResponse;
import com.infratrack.model.User;
import com.infratrack.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OperationalDecisionService {

    private final OperationalDecisionRepository operationalDecisionRepository;
    private final IssueRepository issueRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;

    public OperationalDecisionService(
            OperationalDecisionRepository operationalDecisionRepository,
            IssueRepository issueRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService) {
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.issueRepository = issueRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public List<OperationalDecisionResponse> listAll() {
        return operationalDecisionRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(OperationalDecisionResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public OperationalDecisionResponse getById(Long id) {
        return OperationalDecisionResponse.from(findDecisionOrThrow(id));
    }

    @Transactional
    public OperationalDecisionResponse makeOperationalDecision(CreateOperationalDecisionRequest request, Long userId) {
        User manager = requireManager(userId);
        Issue issue = findIssueOrThrow(request.getIssueId());
        requireIssueFromCompletedInspection(issue);
        requireNoExistingDecision(issue.getId());

        OperationalDecisionOutcome outcome = validateOutcome(request.getOutcome());
        String rationale = normalizeRationale(request.getRationale());
        LocalDateTime decidedAt = validateDecidedAt(request.getDecidedAt(), issue);

        Asset asset = issue.getAsset();
        OperationalDecision decision = operationalDecisionRepository.save(new OperationalDecision(
                issue,
                asset,
                outcome,
                rationale,
                manager.getId(),
                decidedAt
        ));

        assetHistoryEventRepository.save(new AssetHistoryEvent(
                asset,
                AssetHistoryEventType.OPERATIONAL_DECISION_MADE,
                manager.getId(),
                decidedAt.toLocalDate()
        ));

        return OperationalDecisionResponse.from(decision);
    }

    private User requireManager(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only managers can make operational decisions");
        }
        return user;
    }

    private OperationalDecision findDecisionOrThrow(Long id) {
        return operationalDecisionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Operational decision not found"));
    }

    private Issue findIssueOrThrow(Long issueId) {
        if (issueId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue is required");
        }
        return issueRepository.findById(issueId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Issue not found"));
    }

    private void requireIssueFromCompletedInspection(Issue issue) {
        if (issue.getInspection().getStatus() != InspectionStatus.COMPLETED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Operational decisions can only be made for issues from completed inspections");
        }
    }

    private void requireNoExistingDecision(Long issueId) {
        if (operationalDecisionRepository.existsByIssueId(issueId)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An operational decision has already been made for this issue");
        }
    }

    private OperationalDecisionOutcome validateOutcome(OperationalDecisionOutcome outcome) {
        if (outcome == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision outcome is required");
        }
        return outcome;
    }

    private String normalizeRationale(String rationale) {
        if (rationale == null || rationale.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision rationale is required");
        }
        return rationale.trim();
    }

    private LocalDateTime validateDecidedAt(LocalDateTime decidedAt, Issue issue) {
        if (decidedAt == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Decision date and time are required");
        }
        if (decidedAt.isBefore(issue.getRecordedAt())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Decision date and time cannot be before the issue was recorded");
        }
        if (decidedAt.isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Decision date and time cannot be in the future");
        }
        return decidedAt;
    }
}
