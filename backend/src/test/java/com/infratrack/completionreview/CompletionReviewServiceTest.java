package com.infratrack.completionreview;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.exception.ConflictException;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.completionreview.dto.RecordCompletionReviewRequest;
import com.infratrack.department.Department;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.issue.IssueType;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompletionReviewServiceTest {

    @Mock
    private CompletionReviewRepository completionReviewRepository;

    @Mock
    private MaintenanceActivityRepository maintenanceActivityRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private CompletionReviewAuthorizationService authorizationService;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private OperationalEventNotificationService operationalEventNotificationService;

    @InjectMocks
    private CompletionReviewService completionReviewService;

    @Test
    void recordCompletionReview_shouldAllowManagerToRecordApprovedReview() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> {
            CompletionReview review = invocation.getArgument(0);
            review.setId(7000L);
            return review;
        });

        var response = completionReviewService.recordCompletionReview(5000L, request, 30L);

        assertThat(response.getDecision()).isEqualTo(CompletionReviewDecision.APPROVED);
        assertThat(response.getMaintenanceActivityId()).isEqualTo(5000L);
        assertThat(response.getWorkOrderStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
        assertThat(response.getReworkIssueId()).isNull();
        verify(issueRepository, never()).save(any());
    }

    @Test
    void recordCompletionReview_shouldAllowManagerToRecordReworkRequiredReview() {
        RecordCompletionReviewRequest request = reworkRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> {
            CompletionReview review = invocation.getArgument(0);
            review.setId(7001L);
            return review;
        });
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(8001L);
            return issue;
        });

        var response = completionReviewService.recordCompletionReview(5000L, request, 30L);

        assertThat(response.getDecision()).isEqualTo(CompletionReviewDecision.REWORK_REQUIRED);
        assertThat(response.getReworkIssueId()).isEqualTo(8001L);
    }

    @Test
    void recordCompletionReview_shouldLinkCompletionReviewToMaintenanceActivityAndAsset() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        ArgumentCaptor<CompletionReview> reviewCaptor = ArgumentCaptor.forClass(CompletionReview.class);
        verify(completionReviewRepository).save(reviewCaptor.capture());
        CompletionReview saved = reviewCaptor.getValue();
        assertThat(saved.getMaintenanceActivity().getId()).isEqualTo(5000L);
        assertThat(saved.getAsset().getId()).isEqualTo(5L);
        assertThat(saved.getReviewNotes()).isEqualTo("Work completed to standard");
    }

    @Test
    void recordCompletionReview_shouldCreateAssetHistoryEvent() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType())
                .isEqualTo(AssetHistoryEventType.COMPLETION_REVIEW_RECORDED);
        assertThat(historyCaptor.getValue().getAsset().getId()).isEqualTo(5L);
    }

    @Test
    void recordCompletionReview_shouldKeepWorkOrderCompleted() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        assertThat(maintenanceActivity.getWorkOrder().getStatus()).isEqualTo(WorkOrderStatus.COMPLETED);
    }

    @Test
    void recordCompletionReview_shouldNotModifyMaintenanceActivity() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        String originalNotes = maintenanceActivity.getCompletionNotes();
        LocalDateTime originalCompletedAt = maintenanceActivity.getCompletedAt();
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        verify(maintenanceActivityRepository, never()).save(any());
        assertThat(maintenanceActivity.getCompletionNotes()).isEqualTo(originalNotes);
        assertThat(maintenanceActivity.getCompletedAt()).isEqualTo(originalCompletedAt);
    }

    @Test
    void recordCompletionReview_shouldNotChangeAssetStatus() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        AssetStatus originalStatus = maintenanceActivity.getAsset().getStatus();
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        assertThat(maintenanceActivity.getAsset().getStatus()).isEqualTo(originalStatus);
    }

    @Test
    void recordCompletionReview_shouldRejectDuplicateCompletionReview() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(true);

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(ConflictException.class);

        verify(completionReviewRepository, never()).save(any());
    }

    @Test
    void recordCompletionReview_shouldRejectMissingMaintenanceActivity() {
        RecordCompletionReviewRequest request = approvedRequest();
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectInvalidMaintenanceActivityWhenWorkOrderNotCompleted() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = assignedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectWorkOrderNotCompleted() {
        RecordCompletionReviewRequest request = approvedRequest();
        WorkOrder workOrder = workOrder(1000L, WorkOrderStatus.ASSIGNED);
        MaintenanceActivity maintenanceActivity = maintenanceActivity(5000L, workOrder);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectMissingDecision() {
        RecordCompletionReviewRequest request = approvedRequest();
        request.setDecision(null);
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectBlankReviewNotes() {
        RecordCompletionReviewRequest request = approvedRequest();
        request.setReviewNotes("   ");
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectMissingReviewedAt() {
        RecordCompletionReviewRequest request = approvedRequest();
        request.setReviewedAt(null);
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectReviewedAtBeforeMaintenanceCompletion() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        request.setReviewedAt(maintenanceActivity.getCompletedAt().minusMinutes(1));
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectFutureReviewedAt() {
        RecordCompletionReviewRequest request = approvedRequest();
        request.setReviewedAt(LocalDateTime.now().plusHours(1));
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordCompletionReview_shouldRejectAdministrator() {
        assertRejectedRole(UserRole.ADMINISTRATOR, 1L);
    }

    @Test
    void recordCompletionReview_shouldRejectOperationalCoordinator() {
        assertRejectedRole(UserRole.OPERATIONAL_COORDINATOR, 2L);
    }

    @Test
    void recordCompletionReview_shouldRejectFieldEmployee() {
        assertRejectedRole(UserRole.FIELD_EMPLOYEE, 20L);
    }

    @Test
    void recordCompletionReview_shouldRejectContractor() {
        assertRejectedRole(UserRole.CONTRACTOR, 25L);
    }

    @Test
    void recordCompletionReview_shouldNotCreateNewWorkOrder() {
        recordCompletionReview_shouldOnlyPersistCompletionReviewAndAssetHistory();
    }

    @Test
    void recordCompletionReview_shouldNotCreateOperationalDecision() {
        recordCompletionReview_shouldOnlyPersistCompletionReviewAndAssetHistory();
    }

    private void recordCompletionReview_shouldOnlyPersistCompletionReviewAndAssetHistory() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        verify(completionReviewRepository).save(any(CompletionReview.class));
        verify(assetHistoryEventRepository).save(any(AssetHistoryEvent.class));
        verify(maintenanceActivityRepository, never()).save(any());
        verify(issueRepository, never()).save(any());
        verify(operationalEventNotificationService, never())
                .notifyReworkIssueRequiresOperationalDecision(any(), anyLong());
    }

    @Test
    void recordCompletionReview_shouldCreateReworkIssueWhenReworkRequired() {
        RecordCompletionReviewRequest request = reworkRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> {
            CompletionReview review = invocation.getArgument(0);
            review.setId(7001L);
            return review;
        });
        when(issueRepository.existsBySourceCompletionReviewId(7001L)).thenReturn(false);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(8001L);
            return issue;
        });

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(issueRepository).save(issueCaptor.capture());
        Issue savedIssue = issueCaptor.getValue();
        assertThat(savedIssue.getAsset().getId()).isEqualTo(5L);
        assertThat(savedIssue.getSourceCompletionReview().getId()).isEqualTo(7001L);
        assertThat(savedIssue.getInspection()).isNull();
        assertThat(savedIssue.getIssueType()).isEqualTo(IssueType.REWORK);
        assertThat(savedIssue.getDescription())
                .isEqualTo("Rework required following Completion Review #7001: Chain still loose, rework required");
        assertThat(savedIssue.getSeverity()).isEqualTo(IssueSeverity.HIGH);
        assertThat(savedIssue.getRootCause()).isEqualTo("Missing lubrication");
        assertThat(savedIssue.getCorrectiveAction()).isEqualTo("Re-grease chain bearings");
        assertThat(savedIssue.getPreventiveAction()).isEqualTo("Monthly lubrication check");
        assertThat(savedIssue.getRecordedByUserId()).isEqualTo(30L);
    }

    @Test
    void recordCompletionReview_shouldCreateReworkIssueAssetHistoryEvent() {
        RecordCompletionReviewRequest request = reworkRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> {
            CompletionReview review = invocation.getArgument(0);
            review.setId(7001L);
            return review;
        });
        when(issueRepository.existsBySourceCompletionReviewId(7001L)).thenReturn(false);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(8001L);
            return issue;
        });

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository, times(2)).save(historyCaptor.capture());
        assertThat(historyCaptor.getAllValues())
                .extracting(AssetHistoryEvent::getEventType)
                .containsExactly(
                        AssetHistoryEventType.COMPLETION_REVIEW_RECORDED,
                        AssetHistoryEventType.REWORK_ISSUE_CREATED);
        AssetHistoryEvent reworkHistoryEvent = historyCaptor.getAllValues().get(1);
        assertThat(reworkHistoryEvent.getDetails())
                .isEqualTo("Issue type: REWORK | Severity: HIGH | Root cause: Missing lubrication");
    }

    @Test
    void recordCompletionReview_shouldRejectReworkRequiredWithoutSeverity() {
        RecordCompletionReviewRequest request = reworkRequest();
        request.setReworkSeverity(null);
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Rework severity is required when rework is required");

        verify(completionReviewRepository, never()).save(any());
        verify(issueRepository, never()).save(any());
    }

    @Test
    void buildReworkIssueHistoryDetails_shouldOmitRootCauseWhenNotProvided() {
        assertThat(CompletionReviewService.buildReworkIssueHistoryDetails(IssueSeverity.LOW, null))
                .isEqualTo("Issue type: REWORK | Severity: LOW");
    }

    @Test
    void recordCompletionReview_shouldNotifyManagersWhenReworkIssueCreated() {
        RecordCompletionReviewRequest request = reworkRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> {
            CompletionReview review = invocation.getArgument(0);
            review.setId(7001L);
            return review;
        });
        when(issueRepository.existsBySourceCompletionReviewId(7001L)).thenReturn(false);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(8001L);
            return issue;
        });

        completionReviewService.recordCompletionReview(5000L, request, 30L);

        verify(operationalEventNotificationService).notifyReworkIssueRequiresOperationalDecision(
                eq(maintenanceActivity.getAsset().getDepartment()),
                eq(8001L));
    }

    @Test
    void recordCompletionReview_shouldNotCreateReworkIssueWhenApproved() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = user(30L, UserRole.MANAGER);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> {
            CompletionReview review = invocation.getArgument(0);
            review.setId(7000L);
            return review;
        });

        var response = completionReviewService.recordCompletionReview(5000L, request, 30L);

        assertThat(response.getReworkIssueId()).isNull();
        verify(issueRepository, never()).save(any());
        verify(operationalEventNotificationService, never())
                .notifyReworkIssueRequiresOperationalDecision(any(), anyLong());
    }

    @Test
    void recordCompletionReview_shouldRejectManagerFromAnotherDepartment() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = managerInDepartment(30L, 2L);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doThrow(new ForbiddenOperationException(
                "You may only record completion reviews for assets in your own department."))
                .when(authorizationService)
                .requireManagerAuthorizedForAsset(
                        eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, 30L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(completionReviewRepository, never()).save(any());
    }

    @Test
    void recordCompletionReview_shouldAllowManagerWithActiveDelegation() {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User manager = managerInDepartment(30L, 2L);

        when(authorizationService.requireManager(30L)).thenReturn(manager);
        when(maintenanceActivityRepository.findById(5000L)).thenReturn(Optional.of(maintenanceActivity));
        when(completionReviewRepository.existsByMaintenanceActivityId(5000L)).thenReturn(false);
        doNothing().when(authorizationService).requireManagerAuthorizedForAsset(
                eq(manager), eq(maintenanceActivity.getAsset()), any(LocalDateTime.class));
        when(completionReviewRepository.save(any(CompletionReview.class))).thenAnswer(invocation -> {
            CompletionReview review = invocation.getArgument(0);
            review.setId(7000L);
            return review;
        });

        var response = completionReviewService.recordCompletionReview(5000L, request, 30L);

        assertThat(response.getDecision()).isEqualTo(CompletionReviewDecision.APPROVED);
        verify(completionReviewRepository).save(any(CompletionReview.class));
    }

    private void assertRejectedRole(UserRole role, Long userId) {
        RecordCompletionReviewRequest request = approvedRequest();
        MaintenanceActivity maintenanceActivity = completedMaintenanceActivity(5000L, 1000L);
        User actor = user(userId, role);

        when(authorizationService.requireManager(userId))
                .thenThrow(new ForbiddenOperationException("Only managers can record completion reviews"));

        assertThatThrownBy(() -> completionReviewService.recordCompletionReview(5000L, request, userId))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(completionReviewRepository, never()).save(any());
        verify(maintenanceActivityRepository, never()).findById(any());
    }

    private RecordCompletionReviewRequest approvedRequest() {
        RecordCompletionReviewRequest request = new RecordCompletionReviewRequest();
        request.setDecision(CompletionReviewDecision.APPROVED);
        request.setReviewNotes("Work completed to standard");
        request.setReviewedAt(LocalDateTime.now().minusMinutes(1));
        return request;
    }

    private RecordCompletionReviewRequest reworkRequest() {
        RecordCompletionReviewRequest request = approvedRequest();
        request.setDecision(CompletionReviewDecision.REWORK_REQUIRED);
        request.setReviewNotes("Chain still loose, rework required");
        request.setReworkSeverity(IssueSeverity.HIGH);
        request.setRootCause("Missing lubrication");
        request.setCorrectiveAction("Re-grease chain bearings");
        request.setPreventiveAction("Monthly lubrication check");
        return request;
    }

    private MaintenanceActivity completedMaintenanceActivity(Long id, Long workOrderId) {
        WorkOrder workOrder = workOrder(workOrderId, WorkOrderStatus.COMPLETED);
        return maintenanceActivity(id, workOrder);
    }

    private MaintenanceActivity assignedMaintenanceActivity(Long id, Long workOrderId) {
        WorkOrder workOrder = workOrder(workOrderId, WorkOrderStatus.ASSIGNED);
        return maintenanceActivity(id, workOrder);
    }

    private MaintenanceActivity maintenanceActivity(Long id, WorkOrder workOrder) {
        MaintenanceActivity maintenanceActivity = new MaintenanceActivity(
                workOrder,
                workOrder.getAsset(),
                20L,
                "Replaced damaged swing chain",
                LocalDateTime.now().minusHours(2)
        );
        maintenanceActivity.setId(id);
        return maintenanceActivity;
    }

    private WorkOrder workOrder(Long id, WorkOrderStatus status) {
        Asset asset = asset();
        OperationalDecision decision = new OperationalDecision(
                null,
                asset,
                OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                "Decision rationale",
                30L,
                LocalDateTime.now().minusDays(2)
        );
        decision.setId(900L);
        WorkOrder workOrder = new WorkOrder(
                decision,
                asset,
                WorkType.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusDays(1)
        );
        workOrder.setId(id);
        if (status == WorkOrderStatus.COMPLETED) {
            workOrder.assign(20L, 40L, LocalDateTime.now().minusHours(3));
            workOrder.complete();
        } else if (status == WorkOrderStatus.ASSIGNED) {
            workOrder.assign(20L, 40L, LocalDateTime.now().minusHours(3));
        }
        return workOrder;
    }

    private Asset asset() {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Park Swing",
                department,
                category,
                "Central Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L
        );
        asset.setId(5L);
        return asset;
    }

    private User managerInDepartment(Long id, Long departmentId) {
        User manager = user(id, UserRole.MANAGER);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        manager.setDepartment(department);
        return manager;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@example.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
