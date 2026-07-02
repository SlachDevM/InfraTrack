package com.infratrack.suggestedaction;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.issue.IssueService;
import com.infratrack.issue.dto.IssueResponse;
import com.infratrack.suggestedaction.dto.ApproveSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.ApproveSuggestedActionResponse;
import com.infratrack.suggestedaction.dto.DismissSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.RejectSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.SuggestedActionDetailResponse;
import com.infratrack.time.WorkflowClock;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DecisionAssistantService {

    private final SuggestedActionRepository suggestedActionRepository;
    private final IssueService issueService;
    private final UserService userService;
    private final WorkflowClock workflowClock;

    public DecisionAssistantService(
            SuggestedActionRepository suggestedActionRepository,
            IssueService issueService,
            UserService userService,
            WorkflowClock workflowClock) {
        this.suggestedActionRepository = suggestedActionRepository;
        this.issueService = issueService;
        this.userService = userService;
        this.workflowClock = workflowClock;
    }

    @Transactional(readOnly = true)
    public SuggestedActionDetailResponse getSuggestedAction(Long suggestedActionId, Long userId) {
        SuggestedAction action = findAuthorizedAction(suggestedActionId, userId);
        return SuggestedActionDetailResponse.from(action);
    }

    @Transactional
    public ApproveSuggestedActionResponse approve(
            Long suggestedActionId,
            ApproveSuggestedActionRequest request,
            Long userId) {
        SuggestedAction action = findAuthorizedAction(suggestedActionId, userId);
        action.requirePending();
        requireIssueApprovalActionType(action.getActionType());

        IssueResponse issue = issueService.recordIssueFromApprovedSuggestion(action, request, userId);
        long decidedAt = workflowClock.nowMillis();
        action.markAccepted(userId, issue.getId(), decidedAt);
        suggestedActionRepository.save(action);

        return new ApproveSuggestedActionResponse(
                SuggestedActionDetailResponse.from(action),
                issue);
    }

    @Transactional
    public SuggestedActionDetailResponse reject(
            Long suggestedActionId,
            RejectSuggestedActionRequest request,
            Long userId) {
        SuggestedAction action = findAuthorizedAction(suggestedActionId, userId);
        action.markRejected(userId, normalizeOptionalText(request.getReason()), workflowClock.nowMillis());
        return SuggestedActionDetailResponse.from(suggestedActionRepository.save(action));
    }

    @Transactional
    public SuggestedActionDetailResponse dismiss(
            Long suggestedActionId,
            DismissSuggestedActionRequest request,
            Long userId) {
        SuggestedAction action = findAuthorizedAction(suggestedActionId, userId);
        action.markDismissed(userId, normalizeOptionalText(request.getComment()), workflowClock.nowMillis());
        return SuggestedActionDetailResponse.from(suggestedActionRepository.save(action));
    }

    private SuggestedAction findAuthorizedAction(Long suggestedActionId, Long userId) {
        User manager = requireManager(userId);
        SuggestedAction action = suggestedActionRepository.findDetailedById(suggestedActionId)
                .orElseThrow(() -> new NotFoundException("Suggested action not found"));
        requireManagerAuthorizedForAsset(manager, action.getInspection().getAsset());
        return action;
    }

    private User requireManager(Long userId) {
        User user = userService.getById(userId);
        if (!user.getRole().isManager()) {
            throw new ForbiddenOperationException("Only managers can review suggested actions");
        }
        return user;
    }

    private void requireManagerAuthorizedForAsset(User manager, com.infratrack.asset.Asset asset) {
        Long managerDepartmentId = manager.getDepartment() != null
                ? manager.getDepartment().getId()
                : null;
        Long assetDepartmentId = asset.getDepartment().getId();
        if (managerDepartmentId == null || !managerDepartmentId.equals(assetDepartmentId)) {
            throw new ForbiddenOperationException(
                    "You may only review suggested actions for assets in your own department.");
        }
    }

    private static void requireIssueApprovalActionType(DecisionRuleActionType actionType) {
        if (actionType != DecisionRuleActionType.SUGGEST_ISSUE
                && actionType != DecisionRuleActionType.SUGGEST_SEVERITY) {
            throw new BusinessValidationException(
                    "Only issue-related suggested actions can be approved into an Issue");
        }
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
