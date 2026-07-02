package com.infratrack.suggestedaction;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.issue.IssueService;
import com.infratrack.issue.dto.IssueResponse;
import com.infratrack.ruleevaluation.RuleEngineVersion;
import com.infratrack.ruleevaluation.RuleEvaluationReport;
import com.infratrack.ruleevaluation.RuleEvaluationResult;
import com.infratrack.ruleevaluation.RuleEvaluationStatus;
import com.infratrack.suggestedaction.dto.ApproveSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.ApproveSuggestedActionResponse;
import com.infratrack.suggestedaction.dto.DismissSuggestedActionRequest;
import com.infratrack.suggestedaction.dto.RejectSuggestedActionRequest;
import com.infratrack.time.WorkflowClock;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DecisionAssistantServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-02T10:00:00Z");
    private static final long FIXED_MILLIS = FIXED_INSTANT.toEpochMilli();

    @Mock
    private SuggestedActionRepository suggestedActionRepository;

    @Mock
    private IssueService issueService;

    @Mock
    private UserService userService;

    private DecisionAssistantService decisionAssistantService;
    private WorkflowClock workflowClock;

    @BeforeEach
    void setUp() {
        workflowClock = new WorkflowClock(Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault()));
        decisionAssistantService = new DecisionAssistantService(
                suggestedActionRepository,
                issueService,
                userService,
                workflowClock);
    }

    @Test
    void approve_shouldCreateIssueAndMarkSuggestionAccepted() {
        SuggestedAction action = pendingAction();
        User manager = manager(30L);
        ApproveSuggestedActionRequest request = approveRequest();
        Issue issue = new Issue(
                action.getInspection(),
                action.getInspection().getAsset(),
                "Temperature exceeds safe operating range.",
                IssueSeverity.HIGH,
                30L,
                LocalDateTime.now());
        issue.setId(99L);
        IssueResponse issueResponse = IssueResponse.from(issue);
        when(userService.getById(30L)).thenReturn(manager);
        when(suggestedActionRepository.findDetailedById(7L)).thenReturn(Optional.of(action));
        when(issueService.recordIssueFromApprovedSuggestion(eq(action), eq(request), eq(30L)))
                .thenReturn(issueResponse);
        when(suggestedActionRepository.save(action)).thenAnswer(invocation -> invocation.getArgument(0));

        ApproveSuggestedActionResponse response = decisionAssistantService.approve(7L, request, 30L);

        assertThat(action.getStatus()).isEqualTo(SuggestedActionStatus.ACCEPTED);
        assertThat(action.getCreatedIssueId()).isEqualTo(99L);
        assertThat(action.getDecidedAt()).isEqualTo(FIXED_MILLIS);
        verify(issueService).recordIssueFromApprovedSuggestion(action, request, 30L);
    }

    @Test
    void reject_shouldSetDecidedAtFromServer() {
        SuggestedAction action = pendingAction();
        User manager = manager(30L);
        when(userService.getById(30L)).thenReturn(manager);
        when(suggestedActionRepository.findDetailedById(7L)).thenReturn(Optional.of(action));
        when(suggestedActionRepository.save(action)).thenAnswer(invocation -> invocation.getArgument(0));

        decisionAssistantService.reject(7L, new RejectSuggestedActionRequest(), 30L);

        assertThat(action.getDecidedAt()).isEqualTo(FIXED_MILLIS);
    }

    @Test
    void reject_shouldUpdateStatusWithoutCreatingIssue() {
        SuggestedAction action = pendingAction();
        User manager = manager(30L);
        when(userService.getById(30L)).thenReturn(manager);
        when(suggestedActionRepository.findDetailedById(7L)).thenReturn(Optional.of(action));
        when(suggestedActionRepository.save(action)).thenAnswer(invocation -> invocation.getArgument(0));

        var response = decisionAssistantService.reject(
                7L, new RejectSuggestedActionRequest(), 30L);

        assertThat(response.getStatus()).isEqualTo(SuggestedActionStatus.REJECTED);
        verify(issueService, never()).recordIssueFromApprovedSuggestion(any(), any(), eq(30L));
    }

    @Test
    void dismiss_shouldUpdateStatusWithoutCreatingIssue() {
        SuggestedAction action = pendingAction();
        User manager = manager(30L);
        when(userService.getById(30L)).thenReturn(manager);
        when(suggestedActionRepository.findDetailedById(7L)).thenReturn(Optional.of(action));
        when(suggestedActionRepository.save(action)).thenAnswer(invocation -> invocation.getArgument(0));

        var response = decisionAssistantService.dismiss(
                7L, new DismissSuggestedActionRequest(), 30L);

        assertThat(response.getStatus()).isEqualTo(SuggestedActionStatus.DISMISSED);
        verify(issueService, never()).recordIssueFromApprovedSuggestion(any(), any(), eq(30L));
    }

    @Test
    void approve_shouldRejectAlreadyRejectedSuggestion() {
        SuggestedAction action = pendingAction();
        action.markRejected(30L, "Not valid", FIXED_MILLIS);
        User manager = manager(30L);
        when(userService.getById(30L)).thenReturn(manager);
        when(suggestedActionRepository.findDetailedById(7L)).thenReturn(Optional.of(action));

        assertThatThrownBy(() -> decisionAssistantService.approve(7L, approveRequest(), 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getSuggestedAction_shouldRejectCrossDepartmentManager() {
        SuggestedAction action = pendingAction();
        User otherDepartmentManager = manager(40L);
        Department other = new Department("Water");
        other.setId(99L);
        otherDepartmentManager.setDepartment(other);
        when(userService.getById(40L)).thenReturn(otherDepartmentManager);
        when(suggestedActionRepository.findDetailedById(7L)).thenReturn(Optional.of(action));

        assertThatThrownBy(() -> decisionAssistantService.getSuggestedAction(7L, 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getSuggestedAction_shouldIncludeExplanationFromSnapshot() {
        SuggestedAction action = pendingAction();
        User manager = manager(30L);
        when(userService.getById(30L)).thenReturn(manager);
        when(suggestedActionRepository.findDetailedById(7L)).thenReturn(Optional.of(action));

        var response = decisionAssistantService.getSuggestedAction(7L, 30L);

        assertThat(response.getExplanation()).isNotNull();
        assertThat(response.getExplanation().getMatchedRuleCode()).isEqualTo("HIGH_TEMP");
        assertThat(response.getConfidence()).isEqualTo(SuggestionConfidence.LOW);
    }

    private ApproveSuggestedActionRequest approveRequest() {
        ApproveSuggestedActionRequest request = new ApproveSuggestedActionRequest();
        request.setTitle("High temperature detected");
        request.setDescription("Temperature exceeds safe operating range.");
        request.setSeverity(IssueSeverity.HIGH);
        request.setRecordedAt(LocalDateTime.now());
        return request;
    }

    private SuggestedAction pendingAction() {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Pump");
        category.setId(2L);
        Asset asset = new Asset("Pump", department, category, "Site", AssetStatus.ACTIVE, null, 1L);
        asset.setId(5L);
        InspectionTemplate template = new InspectionTemplate(
                "Pump", null, category, 1, InspectionTemplateStatus.PUBLISHED);
        Inspection inspection = new Inspection(asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        inspection.setId(100L);
        inspection.setInspectionTemplate(template);
        inspection.complete(
                com.infratrack.inspection.PhysicalCondition.FAIR,
                "Observed",
                false,
                LocalDateTime.now().minusHours(1),
                20L);
        RuleEvaluationReport report = new RuleEvaluationReport(
                inspection, 1L, RuleEngineVersion.CURRENT, 1L, 1, 1);
        report.setId(10L);
        report.setEvaluationStatus(RuleEvaluationStatus.SUCCESS);
        report.setTemplateVersionSnapshot(1);
        RuleEvaluationResult result = new RuleEvaluationResult(
                1L, "HIGH_TEMP", "High temperature",
                com.infratrack.inspectiontemplate.DecisionRuleConditionType.NUMBER,
                com.infratrack.inspectiontemplate.DecisionRuleOperator.GREATER_THAN,
                "90", "95", true,
                DecisionRuleActionType.SUGGEST_ISSUE,
                "{\"severity\":\"HIGH\"}", 10, 1L, 1L);
        SuggestedAction action = new SuggestedAction(
                inspection, report, result,
                DecisionRuleActionType.SUGGEST_ISSUE,
                "High temperature detected",
                "Temperature exceeds safe operating range.",
                "HIGH", "{}", 1, "HIGH_TEMP", SuggestionConfidence.LOW);
        action.setId(7L);
        return action;
    }

    private User manager(Long id) {
        User user = new User("mgr@test.com", "password", "Manager", UserRole.MANAGER);
        user.setId(id);
        user.setEnabled(true);
        Department department = new Department("Parks");
        department.setId(1L);
        user.setDepartment(department);
        return user;
    }
}
