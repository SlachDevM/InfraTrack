package com.infratrack.operationaldecision;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.operationaldecision.dto.CreateOperationalDecisionRequest;
import com.infratrack.operationaldecision.dto.OperationalDecisionResponse;
import com.infratrack.organization.policy.approval.ApprovalPolicyService;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Records manager operational decisions on issues (UC-007).
 */
@Service
public class OperationalDecisionService {

    private final OperationalDecisionRepository operationalDecisionRepository;
    private final IssueRepository issueRepository;
    private final AssetHistoryEventRepository assetHistoryEventRepository;
    private final UserService userService;
    private final DelegatedAuthorityService delegatedAuthorityService;
    private final ApprovalPolicyService approvalPolicyService;

    public OperationalDecisionService(
            OperationalDecisionRepository operationalDecisionRepository,
            IssueRepository issueRepository,
            AssetHistoryEventRepository assetHistoryEventRepository,
            UserService userService,
            DelegatedAuthorityService delegatedAuthorityService,
            ApprovalPolicyService approvalPolicyService) {
        this.operationalDecisionRepository = operationalDecisionRepository;
        this.issueRepository = issueRepository;
        this.assetHistoryEventRepository = assetHistoryEventRepository;
        this.userService = userService;
        this.delegatedAuthorityService = delegatedAuthorityService;
        this.approvalPolicyService = approvalPolicyService;
    }

    @Transactional(readOnly = true)
    public Page<OperationalDecisionResponse> listPage(Pageable pageable) {
        return operationalDecisionRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(OperationalDecisionResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<OperationalDecisionResponse> listEligibleForWorkOrderCreationPage(Long userId, Pageable pageable) {
        User coordinator = requireOperationalCoordinator(userId);
        Long coordinatorDepartmentId = coordinator.getDepartment() != null
                ? coordinator.getDepartment().getId()
                : null;
        return operationalDecisionRepository.findEligibleForWorkOrderCreation(
                        coordinatorDepartmentId,
                        pageable)
                .map(OperationalDecisionResponse::from);
    }

    @Transactional(readOnly = true)
    public OperationalDecisionResponse getById(Long id) {
        return OperationalDecisionResponse.from(findDecisionOrThrow(id));
    }

    @Transactional
    public OperationalDecisionResponse makeOperationalDecision(CreateOperationalDecisionRequest request, Long userId) {
        requireManagerOperationalDecisionEnabled();
        User manager = requireManager(userId);
        Issue issue = findIssueOrThrow(request.getIssueId());
        requireIssueFromCompletedInspection(issue);
        requireNoExistingDecision(issue.getId());

        OperationalDecisionOutcome outcome = validateOutcome(request.getOutcome());
        String rationale = normalizeRationale(request.getRationale());
        LocalDateTime decidedAt = validateDecidedAt(request.getDecidedAt(), issue);

        Asset asset = issue.getAsset();
        Long delegatedAuthorityId = delegatedAuthorityService.resolveOperationalDecisionDelegationId(
                manager, asset, decidedAt);

        OperationalDecision decision = new OperationalDecision(
                issue,
                asset,
                outcome,
                rationale,
                manager.getId(),
                decidedAt
        );
        decision.setDelegatedAuthorityId(delegatedAuthorityId);
        decision = operationalDecisionRepository.save(decision);

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
            throw new ForbiddenOperationException(
                    "Only managers can make operational decisions");
        }
        return user;
    }

    private User requireOperationalCoordinator(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isOperationalCoordinator()) {
            throw new ForbiddenOperationException(
                    "Only operational coordinators can create work orders");
        }
        return user;
    }

    private OperationalDecision findDecisionOrThrow(Long id) {
        return operationalDecisionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Operational decision not found"));
    }

    private Issue findIssueOrThrow(Long issueId) {
        if (issueId == null) {
            throw new BusinessValidationException("Issue is required");
        }
        return issueRepository.findDetailedById(issueId)
                .orElseThrow(() -> new BusinessValidationException("Issue not found"));
    }

    private void requireIssueFromCompletedInspection(Issue issue) {
        if (issue.getInspection().getStatus() != InspectionStatus.COMPLETED) {
            throw new BusinessValidationException(
                    "Operational decisions can only be made for issues from completed inspections");
        }
    }

    private void requireNoExistingDecision(Long issueId) {
        if (operationalDecisionRepository.existsByIssueId(issueId)) {
            throw new ConflictException(
                    "An operational decision has already been made for this issue");
        }
    }

    private OperationalDecisionOutcome validateOutcome(OperationalDecisionOutcome outcome) {
        if (outcome == null) {
            throw new BusinessValidationException("Decision outcome is required");
        }
        return outcome;
    }

    private String normalizeRationale(String rationale) {
        if (rationale == null || rationale.isBlank()) {
            throw new BusinessValidationException("Decision rationale is required");
        }
        return rationale.trim();
    }

    private LocalDateTime validateDecidedAt(LocalDateTime decidedAt, Issue issue) {
        if (decidedAt == null) {
            throw new BusinessValidationException("Decision date and time are required");
        }
        if (decidedAt.isBefore(issue.getRecordedAt())) {
            throw new BusinessValidationException(
                    "Decision date and time cannot be before the issue was recorded");
        }
        if (decidedAt.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException(
                    "Decision date and time cannot be in the future");
        }
        return decidedAt;
    }

    private void requireManagerOperationalDecisionEnabled() {
        if (!approvalPolicyService.getPolicy().requiresManagerOperationalDecision()) {
            throw new BusinessValidationException(
                    "Manager operational decisions are not enabled by the current approval policy");
        }
    }
}
