package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.preventivemaintenance.dto.ApprovePreventiveCandidateRequest;
import com.infratrack.preventivemaintenance.dto.ApprovePreventiveCandidateResponse;
import com.infratrack.preventivemaintenance.dto.DismissPreventiveCandidateRequest;
import com.infratrack.preventivemaintenance.dto.RejectPreventiveCandidateRequest;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveDecisionAssistantServiceTest {

    @Mock
    private PreventiveExecutionCandidateRepository candidateRepository;

    @Mock
    private UserService userService;

    @Mock
    private InspectionService inspectionService;

    private PreventiveExecutionCandidateAuthorizationService authorizationService;
    private PreventiveDecisionAssistantService decisionAssistantService;

    @BeforeEach
    void setUp() {
        authorizationService = new PreventiveExecutionCandidateAuthorizationService(userService);
        decisionAssistantService = new PreventiveDecisionAssistantService(
                candidateRepository,
                authorizationService,
                inspectionService);
    }

    @Test
    void approve_shouldCreateInspectionAndMarkCandidateApproved() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        User manager = manager(30L, department(10L));
        ApprovePreventiveCandidateRequest request = approveRequest();
        InspectionResponse inspectionResponse = inspectionResponse(900L);
        when(userService.getById(30L)).thenReturn(manager);
        when(candidateRepository.findDetailedById(500L)).thenReturn(Optional.of(candidate));
        when(inspectionService.createInspectionFromApprovedPreventiveCandidate(
                eq(candidate), eq(request), eq(30L))).thenReturn(inspectionResponse);
        when(candidateRepository.save(candidate)).thenAnswer(invocation -> invocation.getArgument(0));

        ApprovePreventiveCandidateResponse response = decisionAssistantService.approve(500L, request, 30L);

        assertThat(candidate.getCandidateStatus()).isEqualTo(ExecutionCandidateStatus.APPROVED);
        assertThat(candidate.getCreatedInspectionId()).isEqualTo(900L);
        assertThat(candidate.getDecidedByUserId()).isEqualTo(30L);
        assertThat(response.getInspection().getId()).isEqualTo(900L);
        verify(inspectionService).createInspectionFromApprovedPreventiveCandidate(candidate, request, 30L);
    }

    @Test
    void reject_shouldUpdateStatusWithoutCreatingInspection() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        User manager = manager(30L, department(10L));
        when(userService.getById(30L)).thenReturn(manager);
        when(candidateRepository.findDetailedById(500L)).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(candidate)).thenAnswer(invocation -> invocation.getArgument(0));

        var response = decisionAssistantService.reject(
                500L, rejectRequest("Already inspected"), 30L);

        assertThat(response.getCandidateStatus()).isEqualTo(ExecutionCandidateStatus.REJECTED);
        assertThat(response.getRejectionReason()).isEqualTo("Already inspected");
        verify(inspectionService, never()).createInspectionFromApprovedPreventiveCandidate(any(), any(), eq(30L));
    }

    @Test
    void dismiss_shouldUpdateStatusWithoutCreatingInspection() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        User manager = manager(30L, department(10L));
        when(userService.getById(30L)).thenReturn(manager);
        when(candidateRepository.findDetailedById(500L)).thenReturn(Optional.of(candidate));
        when(candidateRepository.save(candidate)).thenAnswer(invocation -> invocation.getArgument(0));

        var response = decisionAssistantService.dismiss(
                500L, dismissRequest("Not relevant"), 30L);

        assertThat(response.getCandidateStatus()).isEqualTo(ExecutionCandidateStatus.DISMISSED);
        assertThat(response.getDismissComment()).isEqualTo("Not relevant");
        verify(inspectionService, never()).createInspectionFromApprovedPreventiveCandidate(any(), any(), eq(30L));
    }

    @Test
    void approve_shouldRejectAlreadyApprovedCandidate() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        candidate.markApproved(30L, 900L, null);
        User manager = manager(30L, department(10L));
        when(userService.getById(30L)).thenReturn(manager);
        when(candidateRepository.findDetailedById(500L)).thenReturn(Optional.of(candidate));

        assertThatThrownBy(() -> decisionAssistantService.approve(500L, approveRequest(), 30L))
                .isInstanceOf(BusinessValidationException.class);
        verify(inspectionService, never()).createInspectionFromApprovedPreventiveCandidate(any(), any(), eq(30L));
    }

    @Test
    void approve_shouldRejectUnsupportedTargetAction() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        PreventiveExecutionCandidate workOrderCandidate = new PreventiveExecutionCandidate(
                candidate.getPreventiveMaintenancePlan(),
                candidate.getAsset(),
                candidate.getTriggerType(),
                candidate.getEligibilityReason(),
                candidate.getEvaluatedAt(),
                null,
                candidate.getPlanCodeSnapshot(),
                candidate.getPlanVersionSnapshot(),
                candidate.getPlanNameSnapshot(),
                PlanTargetAction.CREATE_WORK_ORDER,
                candidate.getTriggerSummaryTitleSnapshot(),
                candidate.getTriggerSummaryDescriptionSnapshot());
        workOrderCandidate.setId(501L);
        User manager = manager(30L, department(10L));
        when(userService.getById(30L)).thenReturn(manager);
        when(candidateRepository.findDetailedById(501L)).thenReturn(Optional.of(workOrderCandidate));

        assertThatThrownBy(() -> decisionAssistantService.approve(501L, approveRequest(), 30L))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Target action not supported yet.");
    }

    @Test
    void approve_shouldRejectCrossDepartmentManager() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        Department other = department(99L);
        User manager = manager(40L, other);
        when(userService.getById(40L)).thenReturn(manager);
        when(candidateRepository.findDetailedById(500L)).thenReturn(Optional.of(candidate));

        assertThatThrownBy(() -> decisionAssistantService.approve(500L, approveRequest(), 40L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void approve_shouldRejectCoordinator() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        User coordinator = new User("coord@test.com", "password", "Coordinator", UserRole.OPERATIONAL_COORDINATOR);
        coordinator.setId(50L);
        coordinator.setEnabled(true);
        coordinator.setDepartment(department(10L));
        when(userService.getById(50L)).thenReturn(coordinator);

        assertThatThrownBy(() -> decisionAssistantService.approve(500L, approveRequest(), 50L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    private ApprovePreventiveCandidateRequest approveRequest() {
        ApprovePreventiveCandidateRequest request = new ApprovePreventiveCandidateRequest();
        request.setAssigneeId(60L);
        request.setNotes("Monthly preventive inspection");
        return request;
    }

    private RejectPreventiveCandidateRequest rejectRequest(String reason) {
        RejectPreventiveCandidateRequest request = new RejectPreventiveCandidateRequest();
        request.setReason(reason);
        return request;
    }

    private DismissPreventiveCandidateRequest dismissRequest(String comment) {
        DismissPreventiveCandidateRequest request = new DismissPreventiveCandidateRequest();
        request.setComment(comment);
        return request;
    }

    private InspectionResponse inspectionResponse(Long id) {
        Asset asset = asset(5L, department(10L));
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.SCHEDULED_INSPECTION,
                "Preventive maintenance",
                false,
                30L);
        trigger.setId(700L);
        Inspection inspection = new Inspection(
                asset,
                trigger,
                60L,
                30L,
                InspectionPriority.NORMAL,
                null);
        inspection.setId(id);
        return InspectionResponse.from(inspection);
    }

    private PreventiveExecutionCandidate pendingCandidate() {
        Asset asset = asset(5L, department(10L));
        PreventiveMaintenancePlan plan = new PreventiveMaintenancePlan(
                asset,
                "PUMP_MONTHLY",
                "Monthly Pump Inspection",
                null,
                1,
                PreventiveMaintenancePlanStatus.ACTIVE,
                PreventiveMaintenancePlanPriority.MEDIUM,
                PlanTargetAction.CREATE_INSPECTION,
                null);
        plan.setId(100L);
        PreventiveExecutionCandidate candidate = new PreventiveExecutionCandidate(
                plan,
                asset,
                PlanTriggerType.TIME,
                "One full month has elapsed.",
                1710000000000L,
                null,
                "PUMP_MONTHLY",
                1,
                "Monthly Pump Inspection",
                PlanTargetAction.CREATE_INSPECTION,
                "Every month",
                "Eligible once every full month from plan creation.");
        candidate.setId(500L);
        return candidate;
    }

    private Asset asset(Long id, Department department) {
        Asset asset = new Asset(
                "Pump A",
                department,
                new AssetCategory("Pumps"),
                "Location",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 1, 1),
                1L);
        asset.setId(id);
        return asset;
    }

    private Department department(Long id) {
        Department department = new Department("Water");
        department.setId(id);
        return department;
    }

    private User manager(Long id, Department department) {
        User user = new User("manager@test.com", "password", "Manager", UserRole.MANAGER);
        user.setId(id);
        user.setEnabled(true);
        user.setDepartment(department);
        return user;
    }
}
