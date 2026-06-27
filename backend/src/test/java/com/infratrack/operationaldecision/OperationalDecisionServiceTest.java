package com.infratrack.operationaldecision;

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
import com.infratrack.delegatedauthority.DelegatedAuthority;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.operationaldecision.dto.CreateOperationalDecisionRequest;
import com.infratrack.operationaldecision.dto.OperationalDecisionResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationalDecisionServiceTest {

    @Mock
    private OperationalDecisionRepository operationalDecisionRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

    @InjectMocks
    private OperationalDecisionService operationalDecisionService;

    @Test
    void makeOperationalDecision_shouldCreateDecisionAndHistoryEvent_whenValid() {
        CreateOperationalDecisionRequest request = validRequest();
        Issue issue = issue(500L);
        User manager = managerInDepartment(30L, 1L);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);
        when(delegatedAuthorityService.isManagerOfDepartment(manager, issue.getAsset().getDepartment())).thenReturn(true);
        when(operationalDecisionRepository.save(any(OperationalDecision.class))).thenAnswer(invocation -> {
            OperationalDecision decision = invocation.getArgument(0);
            decision.setId(900L);
            return decision;
        });

        var response = operationalDecisionService.makeOperationalDecision(request, 30L);

        assertThat(response.getId()).isEqualTo(900L);
        assertThat(response.getIssueId()).isEqualTo(500L);
        assertThat(response.getAssetId()).isEqualTo(5L);
        assertThat(response.getAssetName()).isEqualTo("Central Playground");
        assertThat(response.getOutcome()).isEqualTo(OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        assertThat(response.getRationale()).isEqualTo("Replace damaged swing chain through internal parks crew");
        assertThat(response.getDecidedByUserId()).isEqualTo(30L);

        ArgumentCaptor<OperationalDecision> decisionCaptor = ArgumentCaptor.forClass(OperationalDecision.class);
        verify(operationalDecisionRepository).save(decisionCaptor.capture());
        assertThat(decisionCaptor.getValue().getIssue().getId()).isEqualTo(500L);
        assertThat(decisionCaptor.getValue().getAsset().getId()).isEqualTo(5L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.OPERATIONAL_DECISION_MADE);
        assertThat(historyCaptor.getValue().getAsset().getId()).isEqualTo(5L);
    }

    @Test
    void makeOperationalDecision_shouldNotChangeAssetStatus() {
        CreateOperationalDecisionRequest request = validRequest();
        request.setOutcome(OperationalDecisionOutcome.DECOMMISSION_RECOMMENDATION);
        Issue issue = issue(500L);
        AssetStatus statusBefore = issue.getAsset().getStatus();
        User manager = managerInDepartment(30L, 1L);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);
        when(delegatedAuthorityService.isManagerOfDepartment(manager, issue.getAsset().getDepartment())).thenReturn(true);
        when(operationalDecisionRepository.save(any(OperationalDecision.class))).thenAnswer(invocation -> {
            OperationalDecision decision = invocation.getArgument(0);
            decision.setId(900L);
            return decision;
        });

        operationalDecisionService.makeOperationalDecision(request, 30L);

        assertThat(issue.getAsset().getStatus()).isEqualTo(statusBefore);
        assertThat(issue.getAsset().getStatus()).isEqualTo(AssetStatus.ACTIVE);
    }

    @Test
    void makeOperationalDecision_shouldRejectMissingIssueId() {
        CreateOperationalDecisionRequest request = validRequest();
        request.setIssueId(null);
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectInvalidIssue() {
        CreateOperationalDecisionRequest request = validRequest();
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectDuplicateDecisionForSameIssue() {
        CreateOperationalDecisionRequest request = validRequest();
        Issue issue = issue(500L);
        User manager = user(30L, UserRole.MANAGER);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(true);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(ConflictException.class);

        verify(operationalDecisionRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void makeOperationalDecision_shouldRejectMissingOutcome() {
        CreateOperationalDecisionRequest request = validRequest();
        request.setOutcome(null);
        Issue issue = issue(500L);
        User manager = user(30L, UserRole.MANAGER);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectBlankRationale() {
        CreateOperationalDecisionRequest request = validRequest();
        request.setRationale("  ");
        Issue issue = issue(500L);
        User manager = user(30L, UserRole.MANAGER);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectMissingDecidedAt() {
        CreateOperationalDecisionRequest request = validRequest();
        request.setDecidedAt(null);
        Issue issue = issue(500L);
        User manager = user(30L, UserRole.MANAGER);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectDecidedAtBeforeIssueRecordedAt() {
        CreateOperationalDecisionRequest request = validRequest();
        Issue issue = issue(500L);
        request.setDecidedAt(issue.getRecordedAt().minusMinutes(30));
        User manager = user(30L, UserRole.MANAGER);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectFutureDecidedAt() {
        CreateOperationalDecisionRequest request = validRequest();
        request.setDecidedAt(LocalDateTime.now().plusDays(1));
        Issue issue = issue(500L);
        User manager = user(30L, UserRole.MANAGER);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void makeOperationalDecision_shouldAllowManager() {
        CreateOperationalDecisionRequest request = validRequest();
        Issue issue = issue(500L);
        User manager = managerInDepartment(30L, 1L);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);
        when(delegatedAuthorityService.isManagerOfDepartment(manager, issue.getAsset().getDepartment())).thenReturn(true);
        when(operationalDecisionRepository.save(any(OperationalDecision.class))).thenAnswer(invocation -> {
            OperationalDecision decision = invocation.getArgument(0);
            decision.setId(900L);
            return decision;
        });

        assertThatCode(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .doesNotThrowAnyException();
    }

    @Test
    void makeOperationalDecision_shouldRejectAdministrator() {
        CreateOperationalDecisionRequest request = validRequest();
        User administrator = user(30L, UserRole.ADMINISTRATOR);
        when(userService.getById(30L)).thenReturn(administrator);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectOperationalCoordinator() {
        CreateOperationalDecisionRequest request = validRequest();
        User coordinator = user(30L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(30L)).thenReturn(coordinator);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectFieldEmployee() {
        CreateOperationalDecisionRequest request = validRequest();
        User fieldEmployee = user(30L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(30L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectContractor() {
        CreateOperationalDecisionRequest request = validRequest();
        User contractor = user(30L, UserRole.CONTRACTOR);
        when(userService.getById(30L)).thenReturn(contractor);

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void makeOperationalDecision_shouldRejectManagerOutsideAssetDepartmentWithoutDelegation() {
        CreateOperationalDecisionRequest request = validRequest();
        Issue issue = issue(500L);
        User manager = managerInDepartment(30L, 2L);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);
        when(delegatedAuthorityService.isManagerOfDepartment(manager, issue.getAsset().getDepartment())).thenReturn(false);
        when(delegatedAuthorityService.findActiveDelegation(30L, 1L, request.getDecidedAt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(operationalDecisionRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void makeOperationalDecision_shouldAllowManagerWithActiveDelegation() {
        CreateOperationalDecisionRequest request = validRequest();
        Issue issue = issue(500L);
        User manager = managerInDepartment(30L, 2L);
        DelegatedAuthority delegation = activeDelegation(700L);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);
        when(delegatedAuthorityService.isManagerOfDepartment(manager, issue.getAsset().getDepartment())).thenReturn(false);
        when(delegatedAuthorityService.findActiveDelegation(30L, 1L, request.getDecidedAt()))
                .thenReturn(Optional.of(delegation));
        when(operationalDecisionRepository.save(any(OperationalDecision.class))).thenAnswer(invocation -> {
            OperationalDecision decision = invocation.getArgument(0);
            decision.setId(900L);
            return decision;
        });

        var response = operationalDecisionService.makeOperationalDecision(request, 30L);

        assertThat(response.getDelegatedAuthorityId()).isEqualTo(700L);
        verify(assetHistoryEventRepository).save(any(AssetHistoryEvent.class));
    }

    @Test
    void makeOperationalDecision_shouldRejectExpiredDelegation() {
        CreateOperationalDecisionRequest request = validRequest();
        Issue issue = issue(500L);
        User manager = managerInDepartment(30L, 2L);

        when(userService.getById(30L)).thenReturn(manager);
        when(issueRepository.findById(500L)).thenReturn(Optional.of(issue));
        when(operationalDecisionRepository.existsByIssueId(500L)).thenReturn(false);
        when(delegatedAuthorityService.isManagerOfDepartment(manager, issue.getAsset().getDepartment())).thenReturn(false);
        when(delegatedAuthorityService.findActiveDelegation(30L, 1L, request.getDecidedAt())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> operationalDecisionService.makeOperationalDecision(request, 30L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void listPage_shouldReturnPagedDecisions() {
        OperationalDecision decision = operationalDecision(800L);
        Pageable pageable = PageRequest.of(0, 20);
        when(operationalDecisionRepository.findAllByOrderByCreatedAtDesc(pageable))
                .thenReturn(new PageImpl<>(List.of(decision), pageable, 21));

        Page<OperationalDecisionResponse> page = operationalDecisionService.listPage(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(800L);
        assertThat(page.getTotalPages()).isEqualTo(2);
    }

    private CreateOperationalDecisionRequest validRequest() {
        CreateOperationalDecisionRequest request = new CreateOperationalDecisionRequest();
        request.setIssueId(500L);
        request.setOutcome(OperationalDecisionOutcome.INTERNAL_MAINTENANCE);
        request.setRationale("Replace damaged swing chain through internal parks crew");
        request.setDecidedAt(LocalDateTime.now().minusMinutes(5));
        return request;
    }

    private Issue issue(Long id) {
        BusinessTrigger trigger = businessTrigger();
        Inspection inspection = new Inspection(
                trigger.getAsset(),
                trigger,
                20L,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7)
        );
        inspection.setId(100L);
        inspection.complete(
                PhysicalCondition.POOR,
                "Damaged swing chain observed",
                true,
                LocalDateTime.now().minusHours(2),
                20L
        );

        Issue issue = new Issue(
                inspection,
                trigger.getAsset(),
                "Broken swing chain requires replacement",
                IssueSeverity.HIGH,
                20L,
                LocalDateTime.now().minusHours(1)
        );
        issue.setId(id);
        return issue;
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

    private User managerInDepartment(Long id, Long departmentId) {
        User manager = user(id, UserRole.MANAGER);
        Department department = new Department("Department " + departmentId);
        department.setId(departmentId);
        manager.setDepartment(department);
        return manager;
    }

    private DelegatedAuthority activeDelegation(Long id) {
        Department source = new Department("Roads");
        source.setId(2L);
        Department target = new Department("Parks");
        target.setId(1L);
        DelegatedAuthority authority = new DelegatedAuthority(
                40L,
                30L,
                source,
                target,
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(7),
                "Cross-department cover");
        authority.setId(id);
        return authority;
    }

    private OperationalDecision operationalDecision(Long id) {
        Issue issue = issue(500L);
        OperationalDecision decision = new OperationalDecision(
                issue,
                issue.getAsset(),
                OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain through internal parks crew",
                30L,
                LocalDateTime.now().minusMinutes(5)
        );
        decision.setId(id);
        return decision;
    }
}
