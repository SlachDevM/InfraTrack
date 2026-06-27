package com.infratrack.issue;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.dto.CreateIssueRequest;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IssueServiceTest {

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private IssueService issueService;

    @Test
    void recordIssue_shouldCreateIssueAndHistoryEvent_whenValid() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(issueRepository.existsByInspectionId(100L)).thenReturn(false);
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(500L);
            return issue;
        });

        var response = issueService.recordIssue(request, 20L);

        assertThat(response.getId()).isEqualTo(500L);
        assertThat(response.getInspectionId()).isEqualTo(100L);
        assertThat(response.getAssetId()).isEqualTo(5L);
        assertThat(response.getAssetName()).isEqualTo("Central Playground");
        assertThat(response.getDescription()).isEqualTo("Broken swing chain requires replacement");
        assertThat(response.getSeverity()).isEqualTo(IssueSeverity.HIGH);
        assertThat(response.getRecordedByUserId()).isEqualTo(20L);

        ArgumentCaptor<Issue> issueCaptor = ArgumentCaptor.forClass(Issue.class);
        verify(issueRepository).save(issueCaptor.capture());
        assertThat(issueCaptor.getValue().getInspection().getId()).isEqualTo(100L);
        assertThat(issueCaptor.getValue().getAsset().getId()).isEqualTo(5L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.ISSUE_RECORDED);
        assertThat(historyCaptor.getValue().getAsset().getId()).isEqualTo(5L);
        assertThat(historyCaptor.getValue().getPerformedByUserId()).isEqualTo(20L);
    }

    @Test
    void recordIssue_shouldAllowAssignedContractorWhoCompletedInspection() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = completedInspectionWithIssue(100L, 30L);
        User contractor = user(30L, UserRole.CONTRACTOR);

        when(userService.getById(30L)).thenReturn(contractor);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(issueRepository.save(any(Issue.class))).thenAnswer(invocation -> {
            Issue issue = invocation.getArgument(0);
            issue.setId(500L);
            return issue;
        });

        var response = issueService.recordIssue(request, 30L);

        assertThat(response.getRecordedByUserId()).isEqualTo(30L);
    }

    @Test
    void recordIssue_shouldRejectMissingInspectionId() {
        CreateIssueRequest request = validRequest();
        request.setInspectionId(null);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectInvalidInspection() {
        CreateIssueRequest request = validRequest();
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectInspectionNotCompleted() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);

        verify(issueRepository, never()).save(any());
    }

    @Test
    void recordIssue_shouldRejectInspectionWithoutIssueIdentified() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        inspection.complete(
                PhysicalCondition.GOOD,
                "No issues found",
                false,
                LocalDateTime.now().minusHours(2),
                20L
        );
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectNonCompletingUser() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User otherFieldEmployee = user(99L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(99L)).thenReturn(otherFieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 99L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(issueRepository, never()).save(any());
    }

    @Test
    void recordIssue_shouldRejectManager() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User manager = user(20L, UserRole.MANAGER);

        when(userService.getById(20L)).thenReturn(manager);

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void recordIssue_shouldRejectOperationalCoordinator() {
        CreateIssueRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(10L)).thenReturn(coordinator);

        assertThatThrownBy(() -> issueService.recordIssue(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void recordIssue_shouldRejectAdministrator() {
        CreateIssueRequest request = validRequest();
        User administrator = user(10L, UserRole.ADMINISTRATOR);
        when(userService.getById(10L)).thenReturn(administrator);

        assertThatThrownBy(() -> issueService.recordIssue(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void recordIssue_shouldRejectBlankDescription() {
        CreateIssueRequest request = validRequest();
        request.setDescription("  ");
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectMissingSeverity() {
        CreateIssueRequest request = validRequest();
        request.setSeverity(null);
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectMissingRecordedAt() {
        CreateIssueRequest request = validRequest();
        request.setRecordedAt(null);
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectRecordedAtBeforeInspectionCompletion() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        request.setRecordedAt(inspection.getCompletedAt().minusMinutes(30));
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectFutureRecordedAt() {
        CreateIssueRequest request = validRequest();
        request.setRecordedAt(LocalDateTime.now().plusDays(1));
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void recordIssue_shouldRejectDuplicateIssueForSameInspection() {
        CreateIssueRequest request = validRequest();
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(issueRepository.existsByInspectionId(100L)).thenReturn(true);

        assertThatThrownBy(() -> issueService.recordIssue(request, 20L))
                .isInstanceOf(ConflictException.class);

        verify(issueRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    private CreateIssueRequest validRequest() {
        CreateIssueRequest request = new CreateIssueRequest();
        request.setInspectionId(100L);
        request.setDescription("Broken swing chain requires replacement");
        request.setSeverity(IssueSeverity.HIGH);
        request.setRecordedAt(LocalDateTime.now().minusMinutes(10));
        return request;
    }

    private Inspection assignedInspection(Long id, Long assignedToUserId) {
        BusinessTrigger trigger = businessTrigger();
        Inspection inspection = new Inspection(
                trigger.getAsset(),
                trigger,
                assignedToUserId,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7)
        );
        inspection.setId(id);
        return inspection;
    }

    private Inspection completedInspectionWithIssue(Long id, Long completedByUserId) {
        Inspection inspection = assignedInspection(id, completedByUserId);
        LocalDateTime completedAt = LocalDateTime.now().minusHours(1);
        inspection.complete(
                PhysicalCondition.POOR,
                "Damaged swing chain observed",
                true,
                completedAt,
                completedByUserId
        );
        return inspection;
    }

    private BusinessTrigger businessTrigger() {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L
        );
        asset.setId(5L);
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
                false,
                10L
        );
        trigger.setId(1L);
        return trigger;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        return user;
    }
}
