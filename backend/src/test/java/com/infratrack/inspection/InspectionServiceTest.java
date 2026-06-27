package com.infratrack.inspection;

import com.infratrack.exception.BusinessValidationException;
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
import com.infratrack.inspection.dto.InspectionSummaryResponse;
import com.infratrack.notification.OperationalEventNotificationService;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InspectionServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private BusinessTriggerRepository businessTriggerRepository;

    @Mock
    private AssetHistoryEventRepository assetHistoryEventRepository;

    @Mock
    private UserService userService;

    @Mock
    private UserNameLookup userNameLookup;

    @Mock
    private OperationalEventNotificationService operationalEventNotificationService;

    private InspectionService inspectionService;

    @BeforeEach
    void setUp() {
        InspectionAuthorizationService authorizationService = new InspectionAuthorizationService(userService);
        InspectionHistoryRecorder historyRecorder = new InspectionHistoryRecorder(assetHistoryEventRepository);
        inspectionService = new InspectionService(
                inspectionRepository,
                businessTriggerRepository,
                authorizationService,
                historyRecorder,
                userService,
                userNameLookup,
                operationalEventNotificationService);
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
        verify(inspectionRepository).save(argThat(saved -> saved.isIssueIdentified()));
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
    void completeInspection_shouldRejectMissingCompletedAt() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setCompletedAt(null);
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void completeInspection_shouldRejectFutureCompletedAt() {
        CompleteInspectionRequest request = validCompleteRequest();
        request.setCompletedAt(LocalDateTime.now().plusDays(1));
        Inspection inspection = assignedInspection(100L, 20L);
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);

        when(inspectionRepository.findById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> inspectionService.completeInspection(100L, request, 20L))
                .isInstanceOf(BusinessValidationException.class);
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
