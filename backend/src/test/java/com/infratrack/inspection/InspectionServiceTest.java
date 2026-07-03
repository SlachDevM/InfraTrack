package com.infratrack.inspection;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetHistoryEvent;
import com.infratrack.asset.AssetHistoryEventRepository;
import com.infratrack.asset.AssetHistoryEventType;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionAnswerRequest;
import com.infratrack.inspection.dto.InspectionSummaryResponse;
import com.infratrack.inspection.dto.SaveInspectionAnswersRequest;
import com.infratrack.inspection.dto.SaveInspectionProgressRequest;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.inspectiontemplate.InspectionTemplateRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.organization.policy.notification.NotificationPolicyService;
import com.infratrack.organization.policy.visibility.InspectionVisibilityPolicyService;
import com.infratrack.time.WorkflowClock;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-02T10:00:00Z");
    private static final LocalDateTime FIXED_NOW =
            LocalDateTime.ofInstant(FIXED_INSTANT, ZoneId.systemDefault());

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private BusinessTriggerRepository businessTriggerRepository;

    @Mock
    private InspectionTemplateRepository inspectionTemplateRepository;

    @Mock
    private InspectionAnswerRepository inspectionAnswerRepository;

    @Mock
    private com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository inspectionTemplateQuestionRepository;

    @Mock
    private com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository inspectionTemplateQuestionChoiceRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserNameLookup userNameLookup;

    @Mock
    private OperationalEventNotificationService operationalEventNotificationService;

    @Mock
    private com.infratrack.ruleevaluation.RuleEvaluationReportService ruleEvaluationReportService;

    private InspectionService inspectionService;
    private WorkflowClock workflowClock;

    @BeforeEach
    void setUp() {
        workflowClock = new WorkflowClock(Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault()));
        InspectionAuthorizationService authorizationService = new InspectionAuthorizationService(
                userService,
                new InspectionVisibilityPolicyService("DEPARTMENT"));
        InspectionHistoryRecorder historyRecorder = new InspectionHistoryRecorder(assetHistoryEventRepository);
        InspectionAnswerService inspectionAnswerService = new InspectionAnswerService(
                inspectionAnswerRepository,
                inspectionTemplateQuestionRepository,
                inspectionTemplateQuestionChoiceRepository);
        inspectionService = new InspectionService(
                inspectionRepository,
                businessTriggerRepository,
                inspectionTemplateRepository,
                authorizationService,
                historyRecorder,
                inspectionAnswerService,
                userService,
                userNameLookup,
                operationalEventNotificationService,
                new NotificationPolicyService("DEFAULT"),
                ruleEvaluationReportService,
                workflowClock);
    }

    @Test
    void assignInspection_shouldCreateInspectionAndHistoryEvent_whenValid() {
        AssignInspectionRequest request = validRequest();
        BusinessTrigger trigger = businessTrigger(1L, false);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> {
            Inspection inspection = invocation.getArgument(0);
            inspection.setId(100L);
            return inspection;
        });

        var response = inspectionService.assignInspection(request, 10L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getAssetId()).isEqualTo(5L);
        assertThat(response.getAssetName()).isEqualTo("Central Playground");
        assertThat(response.getBusinessTriggerId()).isEqualTo(1L);
        assertThat(response.getAssignedToUserId()).isEqualTo(20L);
        assertThat(response.getAssignedByUserId()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo(InspectionStatus.ASSIGNED);
        assertThat(response.getPriority()).isEqualTo(InspectionPriority.NORMAL);

        ArgumentCaptor<Inspection> inspectionCaptor = ArgumentCaptor.forClass(Inspection.class);
        verify(inspectionRepository).save(inspectionCaptor.capture());
        assertThat(inspectionCaptor.getValue().getAsset().getId()).isEqualTo(5L);
        assertThat(inspectionCaptor.getValue().getBusinessTrigger().getId()).isEqualTo(1L);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.INSPECTION_ASSIGNED);
        assertThat(historyCaptor.getValue().getAsset().getId()).isEqualTo(5L);
        assertThat(historyCaptor.getValue().getPerformedByUserId()).isEqualTo(10L);
        assertThat(historyCaptor.getValue().getEventDate()).isEqualTo(LocalDate.now());
        verify(operationalEventNotificationService).notifyInspectionAssigned(20L);
    }

    @Test
    void assignInspection_shouldNotCreateNotificationHistoryEvent() {
        AssignInspectionRequest request = validRequest();
        BusinessTrigger trigger = businessTrigger(1L, false);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> {
            Inspection inspection = invocation.getArgument(0);
            inspection.setId(100L);
            return inspection;
        });

        inspectionService.assignInspection(request, 10L);

        verify(assetHistoryEventRepository, times(1)).save(any(AssetHistoryEvent.class));
        verify(assetHistoryEventRepository).save(argThat(event ->
                event.getEventType() == AssetHistoryEventType.INSPECTION_ASSIGNED));
    }

    @Test
    void assignInspection_shouldDefaultPriorityToUrgent_whenTriggerIsUrgent() {
        AssignInspectionRequest request = validRequest();
        request.setPriority(null);
        BusinessTrigger trigger = businessTrigger(1L, true);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> {
            Inspection inspection = invocation.getArgument(0);
            inspection.setId(100L);
            return inspection;
        });

        var response = inspectionService.assignInspection(request, 10L);

        assertThat(response.getPriority()).isEqualTo(InspectionPriority.URGENT);
        verify(inspectionRepository).save(argThat(inspection ->
                inspection.getPriority() == InspectionPriority.URGENT));
    }

    @Test
    void assignInspection_shouldRejectMissingBusinessTriggerId() {
        AssignInspectionRequest request = validRequest();
        request.setBusinessTriggerId(null);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(10L)).thenReturn(coordinator);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignInspection_shouldRejectInvalidBusinessTrigger() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignInspection_shouldRejectMissingAssignedUser() {
        AssignInspectionRequest request = validRequest();
        request.setAssignedToUserId(null);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void assignInspection_shouldRejectInvalidAssignedUser() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    void assignInspection_shouldRejectAssignedUserWithInvalidRole() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User manager = user(20L, UserRole.MANAGER);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(manager);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Assigned user is not a Field Employee.");

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void assignInspection_shouldRejectDisabledFieldEmployee() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        fieldEmployee.setEnabled(false);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Assigned worker is disabled.");

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void assignInspection_shouldRejectFieldEmployeeFromOtherDepartment() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Department otherDepartment = new Department("Roads");
        otherDepartment.setId(99L);
        fieldEmployee.setDepartment(otherDepartment);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Assigned worker must belong to your department.");

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void assignInspection_shouldRejectContractorAssignee() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User contractor = user(20L, UserRole.CONTRACTOR);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(contractor);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Assigned user is not a Field Employee.");

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void assignInspection_shouldRejectOperationalCoordinatorAssignee() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User otherCoordinator = user(20L, UserRole.OPERATIONAL_COORDINATOR);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(otherCoordinator);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Assigned user is not a Field Employee.");

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void assignInspection_shouldRejectOtherDepartmentBusinessTrigger() {
        AssignInspectionRequest request = validRequest();
        BusinessTrigger trigger = businessTrigger(1L, false);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        Department otherDepartment = new Department("Roads");
        otherDepartment.setId(99L);
        coordinator.setDepartment(otherDepartment);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("You may only assign inspections for assets in your own department.");
    }

    @Test
    void assignInspection_shouldRejectManager() {
        AssignInspectionRequest request = validRequest();
        User manager = user(10L, UserRole.MANAGER);
        when(userService.getById(10L)).thenReturn(manager);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(inspectionRepository, never()).save(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void assignInspection_shouldRejectAdministrator() {
        AssignInspectionRequest request = validRequest();
        User administrator = user(10L, UserRole.ADMINISTRATOR);
        when(userService.getById(10L)).thenReturn(administrator);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void assignInspection_shouldRejectFieldEmployee() {
        AssignInspectionRequest request = validRequest();
        User fieldEmployee = user(10L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(10L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void assignInspection_shouldRejectContractor() {
        AssignInspectionRequest request = validRequest();
        User contractor = user(10L, UserRole.CONTRACTOR);
        when(userService.getById(10L)).thenReturn(contractor);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void assignInspection_shouldRejectDuplicateActiveAssignment() {
        AssignInspectionRequest request = validRequest();
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(true);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class);

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void assignInspection_shouldRejectExpectedCompletionDateInPast() {
        AssignInspectionRequest request = validRequest();
        request.setExpectedCompletionDate(LocalDate.now().minusDays(1));
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        BusinessTrigger trigger = businessTrigger(1L, false);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class);

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void completeInspection_shouldCompleteInspectionAndHistoryEvent_whenAssignedFieldEmployee() {
        CompleteInspectionRequest request = validCompleteRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inspectionService.completeInspection(100L, request, 20L);

        assertThat(response.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
        assertThat(response.getObservedCondition()).isEqualTo(PhysicalCondition.GOOD);
        assertThat(response.getObservations()).isEqualTo("Equipment inspected and operating normally");
        assertThat(response.isIssueIdentified()).isFalse();
        assertThat(response.getCompletedByUserId()).isEqualTo(20L);
        assertThat(response.getCompletedAt()).isEqualTo(FIXED_NOW);

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getEventType()).isEqualTo(AssetHistoryEventType.INSPECTION_COMPLETED);
        assertThat(historyCaptor.getValue().getAsset().getId()).isEqualTo(5L);
        assertThat(historyCaptor.getValue().getPerformedByUserId()).isEqualTo(20L);
    }

    @Test
    void completeInspection_shouldAllowAssignedContractor() {
        CompleteInspectionRequest request = validCompleteRequest();
        Inspection inspection = assignedInspection(100L, 30L);
        User contractor = user(30L, UserRole.CONTRACTOR);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(30L)).thenReturn(contractor);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inspectionService.completeInspection(100L, request, 30L);

        assertThat(response.getStatus()).isEqualTo(InspectionStatus.COMPLETED);
    }

    @Test
    void completeInspection_shouldRecordIssueIdentifiedWithoutCreatingIssue() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setIssueIdentified(true);
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inspectionService.completeInspection(100L, request, 20L);

        assertThat(response.isIssueIdentified()).isTrue();
        verify(inspectionRepository, times(2)).save(argThat(saved ->
                saved.getStatus() == InspectionStatus.COMPLETED && saved.isIssueIdentified()));
    }

    @Test
    void completeInspection_shouldRejectNonAssignedUser() {
        CompleteInspectionRequest request = validCompleteRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        User otherFieldEmployee = user(99L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(99L)).thenReturn(otherFieldEmployee);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 99L))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(inspectionRepository, never()).save(any());
    }

    @Test
    void completeInspection_shouldRejectManager() {
        CompleteInspectionRequest request = validCompleteRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        User manager = user(20L, UserRole.MANAGER);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(manager);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void completeInspection_shouldRejectOperationalCoordinator() {
        CompleteInspectionRequest request = validCompleteRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        User coordinator = user(20L, UserRole.OPERATIONAL_COORDINATOR);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(coordinator);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void completeInspection_shouldRejectAdministrator() {
        CompleteInspectionRequest request = validCompleteRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        User administrator = user(20L, UserRole.ADMINISTRATOR);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(administrator);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void completeInspection_shouldRejectWhenNotAssignedStatus() {
        CompleteInspectionRequest request = validCompleteRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        inspection.complete(
                PhysicalCondition.GOOD,
                "Already done",
                false,
                LocalDateTime.now().minusHours(1),
                20L
        );
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void completeInspection_shouldRejectMissingObservations() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setObservations("  ");
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void completeInspection_shouldRejectMissingObservedCondition() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setObservedCondition(null);
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void completeInspection_shouldGenerateCompletedAtFromServer_whenClientOmitsTimestamp() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setCompletedAt(null);
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inspectionService.completeInspection(100L, request, 20L);

        assertThat(response.getCompletedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void completeInspection_shouldIgnoreClientProvidedCompletedAt() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setCompletedAt(LocalDateTime.now().minusDays(30));
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = inspectionService.completeInspection(100L, request, 20L);

        assertThat(response.getCompletedAt()).isEqualTo(FIXED_NOW);
    }

    @Test
    void completeInspection_shouldRejectWhenInspectionNotFound() {
        CompleteInspectionRequest request = validCompleteRequest();
        when(inspectionRepository.findById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listEligibleForIssueRecordingPage_shouldReturnCompletedInspectionWithIssueIdentified() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection inspection = completedInspectionWithIssue(100L, 20L);
        Pageable pageable = PageRequest.of(0, 20);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findEligibleForIssueRecording(
                InspectionStatus.COMPLETED, 20L, 1L, pageable))
                .thenReturn(new PageImpl<>(List.of(inspection), pageable, 1));
        when(userNameLookup.resolveNames(List.of(20L))).thenReturn(java.util.Map.of(20L, "Field Employee"));

        Page<InspectionSummaryResponse> page =
                inspectionService.listEligibleForIssueRecordingPage(20L, pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(100L);
        assertThat(page.getContent().get(0).isIssueIdentified()).isTrue();
        assertThat(page.getContent().get(0).getCompletedByUserId()).isEqualTo(20L);
    }

    @Test
    void listEligibleForIssueRecordingPage_shouldRejectUnauthorizedRole() {
        User manager = user(30L, UserRole.MANAGER);
        when(userService.getById(30L)).thenReturn(manager);

        assertThatThrownBy(() -> inspectionService.listEligibleForIssueRecordingPage(
                30L, PageRequest.of(0, 20)))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void listEligibleForIssueRecordingPage_shouldReturnEmptyWhenUserHasNoDepartment() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        fieldEmployee.setDepartment(null);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        Page<InspectionSummaryResponse> page =
                inspectionService.listEligibleForIssueRecordingPage(20L, PageRequest.of(0, 20));

        assertThat(page.getContent()).isEmpty();
        verify(inspectionRepository, never()).findEligibleForIssueRecording(
                any(), any(), any(), any());
    }

    @Test
    void assignInspection_withPublishedTemplate_shouldLinkTemplate() {
        AssignInspectionRequest request = validRequest();
        request.setInspectionTemplateId(50L);
        BusinessTrigger trigger = businessTrigger(1L, false);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        InspectionTemplate template = publishedTemplate(50L, trigger.getAsset().getAssetCategory().getId());

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionTemplateRepository.findDetailedById(50L)).thenReturn(Optional.of(template));
        when(inspectionRepository.save(any(Inspection.class))).thenAnswer(invocation -> {
            Inspection inspection = invocation.getArgument(0);
            inspection.setId(100L);
            return inspection;
        });

        var response = inspectionService.assignInspection(request, 10L);

        assertThat(response.getInspectionTemplateId()).isEqualTo(50L);
        assertThat(response.getInspectionTemplateName()).isEqualTo("Pump Inspection");
    }

    @Test
    void assignInspection_shouldRejectDraftTemplate() {
        AssignInspectionRequest request = validRequest();
        request.setInspectionTemplateId(50L);
        BusinessTrigger trigger = businessTrigger(1L, false);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        InspectionTemplate template = draftTemplate(50L, trigger.getAsset().getAssetCategory().getId());

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionTemplateRepository.findDetailedById(50L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Only published inspection templates can be used for inspections");
    }

    @Test
    void assignInspection_shouldRejectArchivedTemplate() {
        AssignInspectionRequest request = validRequest();
        request.setInspectionTemplateId(50L);
        BusinessTrigger trigger = businessTrigger(1L, false);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        InspectionTemplate template = archivedTemplate(50L, trigger.getAsset().getAssetCategory().getId());

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionTemplateRepository.findDetailedById(50L)).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Only published inspection templates can be used for inspections");
    }

    @Test
    void assignInspection_shouldRejectTemplateFromDifferentCategory() {
        AssignInspectionRequest request = validRequest();
        request.setInspectionTemplateId(50L);
        BusinessTrigger trigger = businessTrigger(1L, false);
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(userService.getById(10L)).thenReturn(coordinator);
        when(businessTriggerRepository.findById(1L)).thenReturn(Optional.of(trigger));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.existsByBusinessTriggerIdAndStatus(1L, InspectionStatus.ASSIGNED))
                .thenReturn(false);
        when(inspectionTemplateRepository.findDetailedById(50L))
                .thenReturn(Optional.of(publishedTemplate(50L, 999L)));

        assertThatThrownBy(() -> inspectionService.assignInspection(request, 10L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Inspection template must belong to the asset category");
    }

    @Test
    void completeInspection_withTemplateAnswers_shouldPersistSnapshots() {
        CompleteInspectionRequest request = validCompleteRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion question = templateQuestion(1L, inspection.getInspectionTemplate());
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> {
            InspectionAnswer answer = invocation.getArgument(0);
            answer.setId(500L);
            return answer;
        });
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenAnswer(invocation -> {
                    InspectionAnswer answer = new InspectionAnswer(
                            inspection,
                            question,
                            "LEAK",
                            "Is there a visible leak?",
                            InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            1);
                    answer.setId(500L);
                    return List.of(answer);
                });

        inspectionService.completeInspection(100L, request, 20L);

        ArgumentCaptor<InspectionAnswer> answerCaptor = ArgumentCaptor.forClass(InspectionAnswer.class);
        verify(inspectionAnswerRepository).save(answerCaptor.capture());
        assertThat(answerCaptor.getValue().getQuestionCodeSnapshot()).isEqualTo("LEAK");
        assertThat(answerCaptor.getValue().getBooleanValue()).isTrue();

        ArgumentCaptor<AssetHistoryEvent> historyCaptor = ArgumentCaptor.forClass(AssetHistoryEvent.class);
        verify(assetHistoryEventRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getDetails()).isEqualTo("Structured answers recorded: 1");
    }

    @Test
    void completeInspection_shouldRejectAnswersWithoutTemplate() {
        CompleteInspectionRequest request = validCompleteRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Structured answers are only supported for templated inspections");
    }

    @Test
    void completeInspection_shouldRejectMissingRequiredAnswer() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setAnswers(List.of());

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion requiredQuestion = templateQuestion(1L, inspection.getInspectionTemplate());
        requiredQuestion.setRequired(true);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(requiredQuestion));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Required checklist question 'LEAK' must be answered");
    }

    @Test
    void completeInspection_shouldRejectUnsupportedPhotoAnswer() {
        CompleteInspectionRequest request = validCompleteRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(2L);
        answerRequest.setTextValue("photo");
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion photoQuestion = templateQuestion(2L, inspection.getInspectionTemplate());
        photoQuestion.setQuestionType(InspectionTemplateQuestionType.PHOTO);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(photoQuestion));

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Answers for question type PHOTO are not supported yet");
    }

    @Test
    void completeInspection_shouldSucceedWithPreviouslySavedAnswers() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setAnswers(List.of());

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion requiredQuestion = templateQuestion(1L, inspection.getInspectionTemplate());
        requiredQuestion.setRequired(true);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        InspectionAnswer savedAnswer = new InspectionAnswer(
                inspection,
                requiredQuestion,
                "LEAK",
                "Is there a visible leak?",
                InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                1);
        savedAnswer.setId(500L);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(requiredQuestion));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(savedAnswer));

        inspectionService.completeInspection(100L, request, 20L);

        verify(inspectionAnswerRepository, never()).save(any(InspectionAnswer.class));
        verify(ruleEvaluationReportService).createReportIfApplicable(100L);
    }

    @Test
    void completeInspection_shouldTriggerDecisionEngineOnlyOnce() {
        CompleteInspectionRequest request = validCompleteRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion question = templateQuestion(1L, inspection.getInspectionTemplate());
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenAnswer(invocation -> {
                    InspectionAnswer answer = new InspectionAnswer(
                            inspection,
                            question,
                            "LEAK",
                            "Is there a visible leak?",
                            InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            1);
                    return List.of(answer);
                });

        inspectionService.completeInspection(100L, request, 20L);

        verify(ruleEvaluationReportService, times(1)).createReportIfApplicable(100L);
    }

    @Test
    void saveInspectionAnswers_shouldPersistFirstAnswer() {
        SaveInspectionAnswersRequest request = new SaveInspectionAnswersRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion question = templateQuestion(1L, inspection.getInspectionTemplate());
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> {
            InspectionAnswer answer = invocation.getArgument(0);
            answer.setId(500L);
            return answer;
        });
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenAnswer(invocation -> {
                    InspectionAnswer answer = new InspectionAnswer(
                            inspection,
                            question,
                            "LEAK",
                            "Is there a visible leak?",
                            InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                            true,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            null,
                            1);
                    answer.setId(500L);
                    return List.of(answer);
                });

        var responses = inspectionService.saveInspectionAnswers(100L, request, 20L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getQuestionId()).isEqualTo(1L);
        assertThat(responses.get(0).getBooleanValue()).isTrue();
        verify(ruleEvaluationReportService, never()).createReportIfApplicable(any());
    }

    @Test
    void saveInspectionAnswers_shouldRejectCompletedInspection() {
        SaveInspectionAnswersRequest request = new SaveInspectionAnswersRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        inspection.complete(
                PhysicalCondition.GOOD,
                "Done",
                false,
                LocalDateTime.now().minusHours(1),
                20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.saveInspectionAnswers(100L, request, 20L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Inspection answers cannot be modified after completion");
    }

    @Test
    void saveInspectionAnswers_shouldRejectUnassignedUser() {
        SaveInspectionAnswersRequest request = new SaveInspectionAnswersRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 99L, 50L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.saveInspectionAnswers(100L, request, 20L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Only the assigned user can save inspection answers");
    }

    @Test
    void saveInspectionProgress_shouldSaveSummaryOnly_withoutCompletingOrTriggeringSideEffects() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setObservedCondition(PhysicalCondition.GOOD);
        request.setObservations("Draft notes");
        request.setIssueIdentified(true);

        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getId()).isEqualTo(100L);
        assertThat(response.getStatus()).isEqualTo(InspectionStatus.ASSIGNED);
        assertThat(response.getObservedCondition()).isEqualTo(PhysicalCondition.GOOD);
        assertThat(response.getObservations()).isEqualTo("Draft notes");
        assertThat(response.isIssueIdentified()).isTrue();

        verify(ruleEvaluationReportService, never()).createReportIfApplicable(any());
        verify(assetHistoryEventRepository, never()).save(any());
    }

    @Test
    void saveInspectionProgress_shouldRejectCompletedInspection() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setObservations("Draft");

        Inspection inspection = assignedInspection(100L, 20L);
        inspection.complete(
                PhysicalCondition.GOOD,
                "Done",
                false,
                LocalDateTime.now().minusHours(1),
                20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.saveInspectionProgress(100L, request, 20L))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Inspection progress cannot be modified after completion");
    }

    @Test
    void completeInspection_shouldSucceed_whenAnswersWerePreviouslySavedProgressively() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setAnswers(null);

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion question = templateQuestion(1L, inspection.getInspectionTemplate());
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenAnswer(invocation -> List.of(new InspectionAnswer(
                        inspection,
                        question,
                        "LEAK",
                        "Is there a visible leak?",
                        InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                        true,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1)));

        SaveInspectionAnswersRequest saveRequest = new SaveInspectionAnswersRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        saveRequest.setAnswers(List.of(answerRequest));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        inspectionService.saveInspectionAnswers(100L, saveRequest, 20L);
        inspectionService.completeInspection(100L, request, 20L);

        verify(ruleEvaluationReportService, times(1)).createReportIfApplicable(100L);
    }

    @Test
    void saveInspectionProgress_emptyBody_shouldSucceed() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getStatus()).isEqualTo(InspectionStatus.ASSIGNED);
        verify(inspectionRepository).save(inspection);
    }

    @Test
    void saveInspectionProgress_emptyAnswersArray_shouldSucceed() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setAnswers(List.of());

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getStatus()).isEqualTo(InspectionStatus.ASSIGNED);
        verify(inspectionAnswerRepository, never()).save(any(InspectionAnswer.class));
    }

    @Test
    void saveInspectionProgress_observedConditionOnly_shouldSucceed() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setObservedCondition(PhysicalCondition.GOOD);

        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getObservedCondition()).isEqualTo(PhysicalCondition.GOOD);
        assertThat(response.getObservations()).isNull();
    }

    @Test
    void saveInspectionProgress_observationsOnly_shouldSucceed() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setObservations("Partial observation.");

        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getObservations()).isEqualTo("Partial observation.");
    }

    @Test
    void saveInspectionProgress_issueIdentifiedOnly_shouldSucceed() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setIssueIdentified(true);

        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.isIssueIdentified()).isTrue();
    }

    @Test
    void saveInspectionProgress_blankObservations_shouldClearStoredValue() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setObservations("   ");

        Inspection inspection = assignedInspection(100L, 20L);
        inspection.saveProgress(null, "Previous notes", true, null);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getObservations()).isNull();
    }

    @Test
    void saveInspectionProgress_emptyObservations_shouldClearStoredValue() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setObservations("");

        Inspection inspection = assignedInspection(100L, 20L);
        inspection.saveProgress(null, "Previous notes", true, null);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getObservations()).isNull();
    }

    @Test
    void saveInspectionProgress_observations_shouldTrimWhitespace() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        request.setObservations("  Partial observation.  ");

        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getObservations()).isEqualTo("Partial observation.");
    }

    @Test
    void saveInspectionProgress_answerWithQuestionIdOnly_shouldBeIgnored() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getStatus()).isEqualTo(InspectionStatus.ASSIGNED);
        verify(inspectionAnswerRepository, never()).save(any(InspectionAnswer.class));
    }

    @Test
    void saveInspectionProgress_answerWithNullBooleanValue_shouldBeIgnored() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(null);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        inspectionService.saveInspectionProgress(100L, request, 20L);

        verify(inspectionAnswerRepository, never()).save(any(InspectionAnswer.class));
    }

    @Test
    void saveInspectionProgress_invalidQuestionId_shouldStillReject() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(99L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion question = templateQuestion(1L, inspection.getInspectionTemplate());
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(question));

        assertThatThrownBy(() -> inspectionService.saveInspectionProgress(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Checklist question does not belong to this inspection template");
    }

    @Test
    void saveInspectionProgress_shouldNotRequireMandatoryQuestions() {
        SaveInspectionProgressRequest request = new SaveInspectionProgressRequest();
        InspectionAnswerRequest answerRequest = new InspectionAnswerRequest();
        answerRequest.setQuestionId(1L);
        answerRequest.setBooleanValue(true);
        request.setAnswers(List.of(answerRequest));

        Inspection inspection = templatedAssignedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion requiredQuestion = templateQuestion(1L, inspection.getInspectionTemplate());
        requiredQuestion.setRequired(true);
        InspectionTemplateQuestion unansweredRequired = templateQuestion(2L, inspection.getInspectionTemplate());
        unansweredRequired.setRequired(true);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionTemplateQuestionRepository.findByInspectionTemplateIdOrderByDisplayOrderAsc(50L))
                .thenReturn(List.of(requiredQuestion, unansweredRequired));
        when(inspectionAnswerRepository.findByInspectionIdAndQuestionId(100L, 1L)).thenReturn(Optional.empty());
        when(inspectionAnswerRepository.save(any(InspectionAnswer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenAnswer(invocation -> List.of(new InspectionAnswer(
                        inspection,
                        requiredQuestion,
                        "LEAK",
                        "Is there a visible leak?",
                        InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                        true,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        1)));

        var response = inspectionService.saveInspectionProgress(100L, request, 20L);

        assertThat(response.getStatus()).isEqualTo(InspectionStatus.ASSIGNED);
    }

    private CompleteInspectionRequest validCompleteRequest() {
        CompleteInspectionRequest request = new CompleteInspectionRequest();
        request.setObservedCondition(PhysicalCondition.GOOD);
        request.setObservations("Equipment inspected and operating normally");
        request.setIssueIdentified(false);
        request.setCompletedAt(LocalDateTime.now().minusMinutes(5));
        return request;
    }

    private Inspection assignedInspection(Long id, Long assignedToUserId) {
        BusinessTrigger trigger = businessTrigger(1L, false);
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

    private Inspection templatedAssignedInspection(Long id, Long assignedToUserId, Long templateId) {
        Inspection inspection = assignedInspection(id, assignedToUserId);
        inspection.setInspectionTemplate(publishedTemplate(templateId, 2L));
        return inspection;
    }

    private InspectionTemplate publishedTemplate(Long id, Long categoryId) {
        AssetCategory category = new AssetCategory("Playground");
        category.setId(categoryId);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection",
                null,
                category,
                1,
                InspectionTemplateStatus.PUBLISHED
        );
        template.setId(id);
        return template;
    }

    private InspectionTemplate draftTemplate(Long id, Long categoryId) {
        AssetCategory category = new AssetCategory("Playground");
        category.setId(categoryId);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection Draft",
                null,
                category,
                1,
                InspectionTemplateStatus.DRAFT
        );
        template.setId(id);
        return template;
    }

    private InspectionTemplate archivedTemplate(Long id, Long categoryId) {
        AssetCategory category = new AssetCategory("Playground");
        category.setId(categoryId);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection Archived",
                null,
                category,
                1,
                InspectionTemplateStatus.ARCHIVED
        );
        template.setId(id);
        return template;
    }

    private InspectionTemplateQuestion templateQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Is there a visible leak?",
                "LEAK",
                null,
                InspectionTemplateQuestionType.BOOLEAN,
                true,
                1
        );
        question.setId(id);
        return question;
    }

    private Inspection completedInspectionWithIssue(Long id, Long completedByUserId) {
        Inspection inspection = assignedInspection(id, completedByUserId);
        inspection.complete(
                PhysicalCondition.POOR,
                "Damaged swing chain observed",
                true,
                LocalDateTime.now().minusHours(1),
                completedByUserId
        );
        return inspection;
    }

    private AssignInspectionRequest validRequest() {
        AssignInspectionRequest request = new AssignInspectionRequest();
        request.setBusinessTriggerId(1L);
        request.setAssignedToUserId(20L);
        request.setPriority(InspectionPriority.NORMAL);
        request.setExpectedCompletionDate(LocalDate.now().plusDays(7));
        return request;
    }

    private BusinessTrigger businessTrigger(Long id, boolean urgent) {
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
                urgent,
                10L
        );
        trigger.setId(id);
        return trigger;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        Department department = new Department("Parks");
        department.setId(1L);
        user.setDepartment(department);
        return user;
    }
}
