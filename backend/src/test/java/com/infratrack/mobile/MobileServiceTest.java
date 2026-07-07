package com.infratrack.mobile;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetRepository;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerQuestionTypeSnapshot;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoice;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueRepository;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.maintenanceactivity.MaintenanceActivity;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.mobile.dto.AssetContextResponse;
import com.infratrack.mobile.dto.MobileDashboardResponse;
import com.infratrack.operationaldocument.OperationalDocument;
import com.infratrack.operationaldocument.OperationalDocumentOwnerType;
import com.infratrack.operationaldocument.OperationalDocumentService;
import com.infratrack.operationaldocument.OperationalDocumentType;
import com.infratrack.mobile.dto.MobileInspectionBundleResponse;
import com.infratrack.mobile.dto.MobileChoiceResponse;
import com.infratrack.mobile.dto.MobileQuestionResponse;
import com.infratrack.mobile.dto.MobileInspectionSummaryResponse;
import com.infratrack.mobile.dto.MobileIssueSummaryResponse;
import com.infratrack.mobile.dto.MobileMeResponse;
import com.infratrack.mobile.dto.MobileWorkOrderBundleResponse;
import com.infratrack.mobile.dto.MobileWorkOrderSummaryResponse;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.operationaldecision.OperationalDecisionRepository;
import com.infratrack.preventivemaintenance.PlanBusinessTrigger;
import com.infratrack.preventivemaintenance.PlanTargetAction;
import com.infratrack.preventivemaintenance.PlanTriggerType;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlan;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanPriority;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanRepository;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanStatus;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.organization.policy.visibility.InspectionVisibilityPolicyService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionAnswerRepository inspectionAnswerRepository;

    @Mock
    private MobileInspectionChecklistLoader checklistLoader;

    @Mock
    private WorkOrderRepository workOrderRepository;

    @Mock
    private MaintenanceActivityRepository maintenanceActivityRepository;

    @Mock
    private PreventiveMaintenancePlanRepository preventiveMaintenancePlanRepository;

    @Mock
    private AssetRepository assetRepository;

    @Mock
    private IssueRepository issueRepository;

    @Mock
    private OperationalDecisionRepository operationalDecisionRepository;

    @Mock
    private DelegatedAuthorityService delegatedAuthorityService;

    @Mock
    private OperationalDocumentService operationalDocumentService;

    @Mock
    private UserNameLookup userNameLookup;

    private MobileService mobileService;

    @BeforeEach
    void setUp() {
        InspectionAuthorizationService inspectionAuthorizationService =
                new InspectionAuthorizationService(userService, new InspectionVisibilityPolicyService("DEPARTMENT"));
        MobileAuthorizationService authorizationService = new MobileAuthorizationService(
                userService, inspectionAuthorizationService, delegatedAuthorityService);
        mobileService = new MobileService(
                authorizationService,
                userService,
                assetRepository,
                inspectionRepository,
                inspectionAnswerRepository,
                checklistLoader,
                issueRepository,
                operationalDecisionRepository,
                workOrderRepository,
                maintenanceActivityRepository,
                preventiveMaintenancePlanRepository,
                operationalDocumentService,
                userNameLookup);
    }

    @Test
    void getMe_shouldReturnAuthenticatedUserSummary() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        MobileMeResponse response = mobileService.getMe(20L);

        assertThat(response.getId()).isEqualTo(20L);
        assertThat(response.getName()).isEqualTo("John Smith");
        assertThat(response.getEmail()).isEqualTo("john@test.com");
        assertThat(response.getRole()).isEqualTo("FIELD_EMPLOYEE");
        assertThat(response.getDepartmentId()).isEqualTo(1L);
        assertThat(response.getDepartmentName()).isEqualTo("Parks");
        assertThat(response.isEnabled()).isTrue();
    }

    @Test
    void getDashboard_shouldReturnOwnCountsForFieldUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.countByAssignedToUserIdAndStatus(20L, InspectionStatus.ASSIGNED)).thenReturn(3L);
        when(workOrderRepository.countByAssignedToUserIdAndStatus(20L, WorkOrderStatus.ASSIGNED)).thenReturn(2L);
        when(inspectionRepository.countOverdueByAssignedUser(eq(20L), eq(InspectionStatus.ASSIGNED), any()))
                .thenReturn(1L);
        when(inspectionRepository.countCompletedByUserBetween(eq(20L), any(), any())).thenReturn(1L);
        when(maintenanceActivityRepository.countCompletedByUserBetween(eq(20L), any(), any())).thenReturn(1L);

        MobileDashboardResponse response = mobileService.getDashboard(20L);

        assertThat(response.getAssignedInspections()).isEqualTo(3L);
        assertThat(response.getAssignedWorkOrders()).isEqualTo(2L);
        assertThat(response.getOverdueInspections()).isEqualTo(1L);
        assertThat(response.getOverdueWorkOrders()).isZero();
        assertThat(response.getCompletedToday()).isEqualTo(2L);
    }

    @Test
    void buildAssetContextsForSync_excludesUnauthorizedAssets() {
        Department employeeDepartment = department(1L, "Parks");
        Department assetDepartment = department(3L, "Roads");
        User fieldEmployee = userWithDepartment(20L, UserRole.FIELD_EMPLOYEE, employeeDepartment);
        Asset visibleAsset = assetInDepartment(employeeDepartment);
        visibleAsset.setId(50L);
        Asset hiddenAsset = assetInDepartment(assetDepartment);
        hiddenAsset.setId(99L);

        when(assetRepository.findByIdIn(List.of(50L, 99L))).thenReturn(List.of(visibleAsset, hiddenAsset));
        when(issueRepository.findAllByAsset_IdOrderByRecordedAtDesc(50L)).thenReturn(List.of());
        when(operationalDocumentService.listVisibleAssetOwnedDocuments(visibleAsset, 20L))
                .thenReturn(List.of());

        List<AssetContextResponse> contexts = mobileService.buildAssetContextsForSync(
                fieldEmployee, List.of(50L, 99L));

        assertThat(contexts).hasSize(1);
        assertThat(contexts.get(0).getAsset().getId()).isEqualTo(50L);
    }

    @Test
    void buildDashboard_matchesGetDashboardForSameUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.countByAssignedToUserIdAndStatus(20L, InspectionStatus.ASSIGNED)).thenReturn(3L);
        when(workOrderRepository.countByAssignedToUserIdAndStatus(20L, WorkOrderStatus.ASSIGNED)).thenReturn(2L);
        when(inspectionRepository.countOverdueByAssignedUser(eq(20L), eq(InspectionStatus.ASSIGNED), any()))
                .thenReturn(1L);
        when(inspectionRepository.countCompletedByUserBetween(eq(20L), any(), any())).thenReturn(1L);
        when(maintenanceActivityRepository.countCompletedByUserBetween(eq(20L), any(), any())).thenReturn(1L);

        MobileDashboardResponse fromGet = mobileService.getDashboard(20L);
        MobileDashboardResponse fromBuild = mobileService.buildDashboard(fieldEmployee);

        assertThat(fromBuild.getAssignedInspections()).isEqualTo(fromGet.getAssignedInspections());
        assertThat(fromBuild.getAssignedWorkOrders()).isEqualTo(fromGet.getAssignedWorkOrders());
        assertThat(fromBuild.getOverdueInspections()).isEqualTo(fromGet.getOverdueInspections());
        assertThat(fromBuild.getOverdueWorkOrders()).isEqualTo(fromGet.getOverdueWorkOrders());
        assertThat(fromBuild.getCompletedToday()).isEqualTo(fromGet.getCompletedToday());
    }

    @Test
    void getMyInspections_shouldReturnOnlyAssignedInspectionsForFieldUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection assigned = assignedInspection(100L, 20L);
        Inspection other = assignedInspection(101L, 99L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findByAssignedToUserId(20L)).thenReturn(List.of(assigned));

        List<MobileInspectionSummaryResponse> response = mobileService.getMyInspections(20L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getInspectionId()).isEqualTo(100L);
        assertThat(other.getAssignedToUserId()).isNotEqualTo(20L);
    }

    @Test
    void getMyInspections_shouldSortOverdueFirstThenDateThenPriority() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection overdue = assignedInspection(100L, 20L);
        overdue = withExpectedDate(overdue, LocalDate.now().minusDays(1));
        overdue = withPriority(overdue, InspectionPriority.NORMAL);

        Inspection dueSoon = assignedInspection(101L, 20L);
        dueSoon = withExpectedDate(dueSoon, LocalDate.now().plusDays(1));
        dueSoon = withPriority(dueSoon, InspectionPriority.URGENT);

        Inspection later = assignedInspection(102L, 20L);
        later = withExpectedDate(later, LocalDate.now().plusDays(5));
        later = withPriority(later, InspectionPriority.HIGH);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findByAssignedToUserId(20L))
                .thenReturn(List.of(later, dueSoon, overdue));

        List<MobileInspectionSummaryResponse> response = mobileService.getMyInspections(20L);

        assertThat(response).extracting(MobileInspectionSummaryResponse::getInspectionId)
                .containsExactly(100L, 101L, 102L);
    }

    @Test
    void getMyInspections_shouldMarkTemplatedInspectionAsHasChecklist() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection templated = templatedInspection(100L, 20L, 50L);
        Inspection legacy = assignedInspection(101L, 20L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findByAssignedToUserId(20L)).thenReturn(List.of(templated, legacy));

        List<MobileInspectionSummaryResponse> response = mobileService.getMyInspections(20L);

        assertThat(response.get(0).isHasChecklist()).isTrue();
        assertThat(response.get(0).getTemplateId()).isEqualTo(50L);
        assertThat(response.get(1).isHasChecklist()).isFalse();
        assertThat(response.get(1).getTemplateId()).isNull();
    }

    @Test
    void getInspectionBundle_shouldAllowAssignedFieldUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection inspection = templatedInspection(100L, 20L, 50L);
        inspection.saveProgress(com.infratrack.inspection.PhysicalCondition.GOOD, "Draft observations", true, true);
        InspectionTemplateQuestion question = templateQuestion(10L, inspection.getInspectionTemplate());
        InspectionTemplateQuestionChoice choice = templateChoice(1L, question, "YES", "Yes");
        InspectionAnswer answer = inspectionAnswer(inspection, question, true);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findMobileBundleById(100L)).thenReturn(Optional.of(inspection));
        when(checklistLoader.loadMobileQuestions(50L))
                .thenReturn(List.of(MobileQuestionResponse.from(question, List.of(MobileChoiceResponse.from(choice)))));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(answer));

        MobileInspectionBundleResponse response = mobileService.getInspectionBundle(20L, 100L);

        assertThat(response.getInspection().getId()).isEqualTo(100L);
        assertThat(response.getAsset().getName()).isEqualTo("Central Playground");
        assertThat(response.getAsset().getLocation()).isEqualTo("Memorial Park");
        assertThat(response.getInspection().getObservedCondition()).isEqualTo(com.infratrack.inspection.PhysicalCondition.GOOD);
        assertThat(response.getInspection().getObservations()).isEqualTo("Draft observations");
        assertThat(response.getInspection().isIssueIdentified()).isTrue();
        assertThat(response.getTemplate().getId()).isEqualTo(50L);
        assertThat(response.getQuestions()).hasSize(1);
        assertThat(response.getQuestions().get(0).getChoices()).hasSize(1);
        assertThat(response.getAnswers()).hasSize(1);
        assertThat(response.getAllowedActions().isCanComplete()).isTrue();
        assertThat(response.getAllowedActions().isCanViewAsset()).isTrue();
    }

    @Test
    void getInspectionBundle_shouldRejectUnassignedFieldUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection inspection = assignedInspection(100L, 99L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findMobileBundleById(100L)).thenReturn(Optional.of(inspection));

        assertThatThrownBy(() -> mobileService.getInspectionBundle(20L, 100L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("assigned to you");
    }

    @Test
    void getInspectionBundle_shouldAllowAdministrator() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Inspection inspection = assignedInspection(100L, 99L);

        when(userService.getById(1L)).thenReturn(admin);
        when(inspectionRepository.findMobileBundleById(100L)).thenReturn(Optional.of(inspection));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        MobileInspectionBundleResponse response = mobileService.getInspectionBundle(1L, 100L);

        assertThat(response.getInspection().getId()).isEqualTo(100L);
        assertThat(response.getQuestions()).isEmpty();
        assertThat(response.getAllowedActions().isCanComplete()).isFalse();
    }

    @Test
    void getInspectionBundle_shouldReturnProgressivelySavedAnswers() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection inspection = templatedInspection(100L, 20L, 50L);
        InspectionTemplateQuestion question = templateQuestion(10L, inspection.getInspectionTemplate());
        InspectionAnswer progressiveAnswer = inspectionAnswer(inspection, question, false);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findMobileBundleById(100L)).thenReturn(Optional.of(inspection));
        when(checklistLoader.loadMobileQuestions(50L))
                .thenReturn(List.of(MobileQuestionResponse.from(question, List.of())));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(progressiveAnswer));

        MobileInspectionBundleResponse response = mobileService.getInspectionBundle(20L, 100L);

        assertThat(response.getAnswers()).hasSize(1);
        assertThat(response.getAnswers().get(0).getBooleanValue()).isFalse();
    }

    @Test
    void getInspectionBundle_shouldReturnEmptyQuestionsForLegacyInspection() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        Inspection inspection = assignedInspection(100L, 20L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findMobileBundleById(100L)).thenReturn(Optional.of(inspection));
        when(inspectionAnswerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of());

        MobileInspectionBundleResponse response = mobileService.getInspectionBundle(20L, 100L);

        assertThat(response.getTemplate()).isNull();
        assertThat(response.getQuestions()).isEmpty();
        assertThat(response.getAnswers()).isEmpty();
    }

    @Test
    void getMyWorkOrders_shouldReturnOnlyAssignedWorkOrdersForFieldUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        WorkOrder assigned = assignedWorkOrder(200L, 20L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findByAssignedToUserId(20L)).thenReturn(List.of(assigned));

        List<MobileWorkOrderSummaryResponse> response = mobileService.getMyWorkOrders(20L);

        assertThat(response).hasSize(1);
        assertThat(response.get(0).getWorkOrderId()).isEqualTo(200L);
    }

    @Test
    void getWorkOrderBundle_shouldAllowAssignedFieldUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        WorkOrder workOrder = assignedWorkOrder(200L, 20L);
        MaintenanceActivity activity = new MaintenanceActivity(
                workOrder,
                workOrder.getAsset(),
                20L,
                "Completed repair",
                LocalDateTime.now().minusHours(1));

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findMobileBundleById(200L)).thenReturn(Optional.of(workOrder));
        when(maintenanceActivityRepository.findByWorkOrderId(200L)).thenReturn(Optional.of(activity));

        MobileWorkOrderBundleResponse response = mobileService.getWorkOrderBundle(20L, 200L);

        assertThat(response.getWorkOrder().getId()).isEqualTo(200L);
        assertThat(response.getAsset().getName()).isEqualTo("Central Playground");
        assertThat(response.getIssue().getIssueId()).isEqualTo(500L);
        assertThat(response.getDecision().getOperationalDecisionId()).isEqualTo(900L);
        assertThat(response.getMaintenanceActivity().getNotes()).isEqualTo("Completed repair");
        assertThat(response.getAllowedActions().isCanComplete()).isTrue();
    }

    @Test
    void getWorkOrderBundle_shouldRejectUnassignedFieldUser() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        WorkOrder workOrder = assignedWorkOrder(200L, 99L);

        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findMobileBundleById(200L)).thenReturn(Optional.of(workOrder));

        assertThatThrownBy(() -> mobileService.getWorkOrderBundle(20L, 200L))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessageContaining("assigned to you");
    }

    @Test
    void getMe_shouldRejectOperationalCoordinator() {
        User coordinator = user(10L, UserRole.OPERATIONAL_COORDINATOR);
        when(userService.getById(10L)).thenReturn(coordinator);

        assertThatThrownBy(() -> mobileService.getMe(10L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getInspectionBundle_shouldThrowNotFoundWhenMissing() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findMobileBundleById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mobileService.getInspectionBundle(20L, 999L))
                .isInstanceOf(NotFoundException.class);
    }

    // --- Mobile asset lookup / asset context (M4-BE1) ---

    @Test
    void getAssetContext_blankCode_throwsBusinessValidationException() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        when(userService.getById(1L)).thenReturn(admin);

        assertThatThrownBy(() -> mobileService.getAssetContext(1L, "   "))
                .isInstanceOf(BusinessValidationException.class);

        verify(assetRepository, never()).findByCodeIgnoreCase(any());
    }

    @Test
    void getAssetContext_nullCode_throwsBusinessValidationException() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        when(userService.getById(1L)).thenReturn(admin);

        assertThatThrownBy(() -> mobileService.getAssetContext(1L, null))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getAssetContext_unknownCode_throwsNotFoundException() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        when(userService.getById(1L)).thenReturn(admin);
        when(assetRepository.findByCodeIgnoreCase("UNKNOWN-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mobileService.getAssetContext(1L, "UNKNOWN-1"))
                .isInstanceOf(NotFoundException.class);

        verify(issueRepository, never()).findAllByAsset_IdOrderByRecordedAtDesc(any());
        verify(inspectionRepository, never()).findByAsset_IdAndStatus(any(), any());
        verify(workOrderRepository, never()).findByAsset_IdAndStatusIn(any(), any());
        verify(inspectionRepository, never())
                .findFirstByAsset_IdAndStatusOrderByCompletedAtDesc(any(), any());
        verify(maintenanceActivityRepository, never()).findFirstByAsset_IdOrderByCompletedAtDesc(any());
        verify(preventiveMaintenancePlanRepository, never())
                .findFirstByAsset_IdAndStatusOrderByCreatedAtDesc(any(), any());
        verify(operationalDocumentService, never()).listVisibleAssetOwnedDocuments(any(), any());
    }

    @Test
    void getAssetContext_administrator_canLookupAnyDepartmentAsset() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getAsset().getId()).isEqualTo(asset.getId());
    }

    @Test
    void getAssetContext_managerOwnDepartment_canLookup() {
        Department managerDepartment = department(1L, "Parks");
        User manager = userWithDepartment(2L, UserRole.MANAGER, managerDepartment);
        Asset asset = assetInDepartment(managerDepartment);
        stubAssetLookup(asset);
        when(userService.getById(2L)).thenReturn(manager);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(eq(manager), eq(managerDepartment), any()))
                .thenReturn(true);

        AssetContextResponse response = mobileService.getAssetContext(2L, asset.getCode());

        assertThat(response.getAsset().getId()).isEqualTo(asset.getId());
    }

    @Test
    void getAssetContext_managerDelegatedDepartment_canLookup() {
        Department managerDepartment = department(1L, "Parks");
        Department targetDepartment = department(3L, "Roads");
        User manager = userWithDepartment(2L, UserRole.MANAGER, managerDepartment);
        Asset asset = assetInDepartment(targetDepartment);
        stubAssetLookup(asset);
        when(userService.getById(2L)).thenReturn(manager);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(eq(manager), eq(targetDepartment), any()))
                .thenReturn(true);

        AssetContextResponse response = mobileService.getAssetContext(2L, asset.getCode());

        assertThat(response.getAsset().getId()).isEqualTo(asset.getId());
    }

    @Test
    void getAssetContext_managerCrossDepartmentWithoutDelegation_isForbidden() {
        Department managerDepartment = department(1L, "Parks");
        Department targetDepartment = department(3L, "Roads");
        User manager = userWithDepartment(2L, UserRole.MANAGER, managerDepartment);
        Asset asset = assetInDepartment(targetDepartment);
        stubAssetFound(asset);
        when(userService.getById(2L)).thenReturn(manager);
        when(delegatedAuthorityService.canManagerActForAssetDepartment(eq(manager), eq(targetDepartment), any()))
                .thenReturn(false);

        assertThatThrownBy(() -> mobileService.getAssetContext(2L, asset.getCode()))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(issueRepository, never()).findAllByAsset_IdOrderByRecordedAtDesc(any());
    }

    @Test
    void getAssetContext_operationalCoordinatorOwnDepartment_canLookup() {
        Department department = department(1L, "Parks");
        User coordinator = userWithDepartment(3L, UserRole.OPERATIONAL_COORDINATOR, department);
        Asset asset = assetInDepartment(department);
        stubAssetLookup(asset);
        when(userService.getById(3L)).thenReturn(coordinator);

        AssetContextResponse response = mobileService.getAssetContext(3L, asset.getCode());

        assertThat(response.getAsset().getId()).isEqualTo(asset.getId());
        assertThat(response.getAllowedActions().isCanCreateInspection()).isTrue();
    }

    @Test
    void getAssetContext_operationalCoordinatorCrossDepartment_isForbidden() {
        Department coordinatorDepartment = department(1L, "Parks");
        Department assetDepartment = department(3L, "Roads");
        User coordinator = userWithDepartment(3L, UserRole.OPERATIONAL_COORDINATOR, coordinatorDepartment);
        Asset asset = assetInDepartment(assetDepartment);
        stubAssetFound(asset);
        when(userService.getById(3L)).thenReturn(coordinator);

        assertThatThrownBy(() -> mobileService.getAssetContext(3L, asset.getCode()))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getAssetContext_fieldEmployeeOwnDepartment_canLookup() {
        Department department = department(1L, "Parks");
        User fieldEmployee = userWithDepartment(20L, UserRole.FIELD_EMPLOYEE, department);
        Asset asset = assetInDepartment(department);
        stubAssetLookup(asset);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        AssetContextResponse response = mobileService.getAssetContext(20L, asset.getCode());

        assertThat(response.getAsset().getId()).isEqualTo(asset.getId());
        assertThat(response.getAllowedActions().isCanCreateIssue()).isTrue();
        assertThat(response.getAllowedActions().isCanCreateInspection()).isFalse();
    }

    @Test
    void getAssetContext_contractorOwnDepartment_canLookup() {
        Department department = department(1L, "Parks");
        User contractor = userWithDepartment(21L, UserRole.CONTRACTOR, department);
        Asset asset = assetInDepartment(department);
        stubAssetLookup(asset);
        when(userService.getById(21L)).thenReturn(contractor);

        AssetContextResponse response = mobileService.getAssetContext(21L, asset.getCode());

        assertThat(response.getAsset().getId()).isEqualTo(asset.getId());
        assertThat(response.getAllowedActions().isCanCreateIssue()).isTrue();
    }

    @Test
    void getAssetContext_fieldEmployeeCrossDepartment_isForbiddenAndDoesNotLeakContext() {
        Department employeeDepartment = department(1L, "Parks");
        Department assetDepartment = department(3L, "Roads");
        User fieldEmployee = userWithDepartment(20L, UserRole.FIELD_EMPLOYEE, employeeDepartment);
        Asset asset = assetInDepartment(assetDepartment);
        stubAssetFound(asset);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> mobileService.getAssetContext(20L, asset.getCode()))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(issueRepository, never()).findAllByAsset_IdOrderByRecordedAtDesc(any());
        verify(inspectionRepository, never()).findByAsset_IdAndStatus(any(), any());
        verify(workOrderRepository, never()).findByAsset_IdAndStatusIn(any(), any());
        verify(inspectionRepository, never())
                .findFirstByAsset_IdAndStatusOrderByCompletedAtDesc(any(), any());
        verify(maintenanceActivityRepository, never()).findFirstByAsset_IdOrderByCompletedAtDesc(any());
        verify(preventiveMaintenancePlanRepository, never())
                .findFirstByAsset_IdAndStatusOrderByCreatedAtDesc(any(), any());
        verify(operationalDocumentService, never()).listVisibleAssetOwnedDocuments(any(), any());
    }

    @Test
    void getAssetContext_enrichmentSectionsNullWhenNoneExist() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getLastInspection()).isNull();
        assertThat(response.getLastMaintenance()).isNull();
        assertThat(response.getPreventivePlan()).isNull();
        assertThat(response.getDocuments()).isEmpty();
    }

    @Test
    void getAssetContext_includesVisibleAssetOwnedDocuments() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        OperationalDocument document = assetOwnedDocument(101L, asset);
        when(operationalDocumentService.listVisibleAssetOwnedDocuments(asset, 1L))
                .thenReturn(List.of(document));
        when(userNameLookup.resolveNames(List.of(30L))).thenReturn(Map.of(30L, "Maintenance Admin"));

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getDocuments()).hasSize(1);
        assertThat(response.getDocuments().getFirst().getId()).isEqualTo(101L);
        assertThat(response.getDocuments().getFirst().getFilename()).isEqualTo("street-light-manual.pdf");
        assertThat(response.getDocuments().getFirst().getContentType()).isEqualTo("application/pdf");
        assertThat(response.getDocuments().getFirst().getOwnerType()).isEqualTo(OperationalDocumentOwnerType.ASSET);
        assertThat(response.getDocuments().getFirst().getUploadedAt())
                .isEqualTo(LocalDateTime.of(2026, 7, 4, 9, 30));
        assertThat(response.getDocuments().getFirst().getUploadedBy()).isEqualTo("Maintenance Admin");
        assertThat(response.getDocuments().getFirst().getDownloadUrl())
                .isEqualTo("/api/operational-documents/101/download");
    }

    @Test
    void getAssetContext_documentSummariesDoNotExposeStoragePaths() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        OperationalDocument document = assetOwnedDocument(101L, asset);
        when(operationalDocumentService.listVisibleAssetOwnedDocuments(asset, 1L))
                .thenReturn(List.of(document));
        when(userNameLookup.resolveNames(List.of(30L))).thenReturn(Map.of(30L, "Maintenance Admin"));

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getDocuments().getFirst().getDownloadUrl())
                .doesNotContain("/tmp/")
                .doesNotContain("stored-file");
    }

    @Test
    void getAssetContext_fieldEmployeeSeesAssetOwnedDocuments() {
        Department department = department(1L, "Parks");
        User fieldEmployee = userWithDepartment(20L, UserRole.FIELD_EMPLOYEE, department);
        Asset asset = assetInDepartment(department);
        stubAssetLookup(asset);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        OperationalDocument document = assetOwnedDocument(101L, asset);
        when(operationalDocumentService.listVisibleAssetOwnedDocuments(asset, 20L))
                .thenReturn(List.of(document));
        when(userNameLookup.resolveNames(List.of(30L))).thenReturn(Map.of(30L, "Maintenance Admin"));

        AssetContextResponse response = mobileService.getAssetContext(20L, asset.getCode());

        assertThat(response.getDocuments()).hasSize(1);
        assertThat(response.getDocuments().getFirst().getId()).isEqualTo(101L);
        assertThat(response.getDocuments().getFirst().getOwnerType()).isEqualTo(OperationalDocumentOwnerType.ASSET);
    }

    @Test
    void getAssetContext_contractorSeesAssetOwnedDocuments() {
        Department department = department(1L, "Parks");
        User contractor = userWithDepartment(21L, UserRole.CONTRACTOR, department);
        Asset asset = assetInDepartment(department);
        stubAssetLookup(asset);
        when(userService.getById(21L)).thenReturn(contractor);

        OperationalDocument document = assetOwnedDocument(102L, asset);
        when(operationalDocumentService.listVisibleAssetOwnedDocuments(asset, 21L))
                .thenReturn(List.of(document));
        when(userNameLookup.resolveNames(List.of(30L))).thenReturn(Map.of(30L, "Maintenance Admin"));

        AssetContextResponse response = mobileService.getAssetContext(21L, asset.getCode());

        assertThat(response.getDocuments()).hasSize(1);
        assertThat(response.getDocuments().getFirst().getId()).isEqualTo(102L);
    }

    @Test
    void getAssetContext_crossDepartmentFieldEmployeeDoesNotLoadDocuments() {
        Department employeeDepartment = department(1L, "Parks");
        Department assetDepartment = department(3L, "Roads");
        User fieldEmployee = userWithDepartment(20L, UserRole.FIELD_EMPLOYEE, employeeDepartment);
        Asset asset = assetInDepartment(assetDepartment);
        stubAssetFound(asset);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> mobileService.getAssetContext(20L, asset.getCode()))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(operationalDocumentService, never()).listVisibleAssetOwnedDocuments(any(), any());
    }

    @Test
    void getAssetContext_includesLastCompletedInspection() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        Inspection completedInspection = completedInspection(901L);
        when(inspectionRepository.findFirstByAsset_IdAndStatusOrderByCompletedAtDesc(
                asset.getId(), InspectionStatus.COMPLETED))
                .thenReturn(Optional.of(completedInspection));

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getLastInspection()).isNotNull();
        assertThat(response.getLastInspection().getId()).isEqualTo(901L);
        assertThat(response.getLastInspection().getStatus()).isEqualTo(InspectionStatus.COMPLETED);
        assertThat(response.getLastInspection().getCompletedAt())
                .isEqualTo(LocalDateTime.of(2026, 7, 4, 10, 15));
        assertThat(response.getLastInspection().getObservedCondition()).isEqualTo(PhysicalCondition.GOOD);
        assertThat(response.getLastInspection().isIssueIdentified()).isFalse();
    }

    @Test
    void getAssetContext_includesLastMaintenanceWithPerformerName() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        MaintenanceActivity maintenanceActivity = maintenanceActivityForAsset(asset, 902L, 77L);
        when(maintenanceActivityRepository.findFirstByAsset_IdOrderByCompletedAtDesc(asset.getId()))
                .thenReturn(Optional.of(maintenanceActivity));
        User performer = user(20L, UserRole.FIELD_EMPLOYEE);
        when(userService.getById(20L)).thenReturn(performer);

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getLastMaintenance()).isNotNull();
        assertThat(response.getLastMaintenance().getId()).isEqualTo(902L);
        assertThat(response.getLastMaintenance().getWorkOrderId()).isEqualTo(77L);
        assertThat(response.getLastMaintenance().getCompletedAt())
                .isEqualTo(LocalDateTime.of(2026, 7, 3, 15, 30));
        assertThat(response.getLastMaintenance().getPerformedBy()).isEqualTo("John Smith");
    }

    @Test
    void getAssetContext_includesActivePreventivePlan() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        PreventiveMaintenancePlan plan = activePreventivePlan(88L, asset);
        when(preventiveMaintenancePlanRepository.findFirstByAsset_IdAndStatusOrderByCreatedAtDesc(
                asset.getId(), PreventiveMaintenancePlanStatus.ACTIVE))
                .thenReturn(Optional.of(plan));

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getPreventivePlan()).isNotNull();
        assertThat(response.getPreventivePlan().isExists()).isTrue();
        assertThat(response.getPreventivePlan().getId()).isEqualTo(88L);
        assertThat(response.getPreventivePlan().getName()).isEqualTo("Monthly lighting inspection");
        assertThat(response.getPreventivePlan().getTriggerType()).isEqualTo(PlanTriggerType.TIME);
        assertThat(response.getPreventivePlan().isActive()).isTrue();
    }

    @Test
    void getAssetContext_forbiddenLookupDoesNotQueryEnrichmentSections() {
        Department employeeDepartment = department(1L, "Parks");
        Department assetDepartment = department(3L, "Roads");
        User fieldEmployee = userWithDepartment(20L, UserRole.FIELD_EMPLOYEE, employeeDepartment);
        Asset asset = assetInDepartment(assetDepartment);
        stubAssetFound(asset);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        assertThatThrownBy(() -> mobileService.getAssetContext(20L, asset.getCode()))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(inspectionRepository, never())
                .findFirstByAsset_IdAndStatusOrderByCompletedAtDesc(any(), any());
        verify(maintenanceActivityRepository, never()).findFirstByAsset_IdOrderByCompletedAtDesc(any());
        verify(preventiveMaintenancePlanRepository, never())
                .findFirstByAsset_IdAndStatusOrderByCreatedAtDesc(any(), any());
        verify(operationalDocumentService, never()).listVisibleAssetOwnedDocuments(any(), any());
    }

    @Test
    void getAssetContext_returnsAssetSummaryFields() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getAsset().getId()).isEqualTo(asset.getId());
        assertThat(response.getAsset().getCode()).isEqualTo(asset.getCode());
        assertThat(response.getAsset().getName()).isEqualTo("Central Playground");
        assertThat(response.getAsset().getCategory()).isEqualTo("Playground");
        assertThat(response.getAsset().getDepartment()).isEqualTo("Parks");
        assertThat(response.getAsset().getLocation()).isEqualTo("Memorial Park");
        assertThat(response.getAsset().getStatus()).isEqualTo(AssetStatus.ACTIVE);
    }

    @Test
    void getAssetContext_includesOnlyUnresolvedIssues() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        Issue openIssue = issueForAsset(asset, 600L);
        Issue resolvedIssue = issueForAsset(asset, 601L);
        when(issueRepository.findAllByAsset_IdOrderByRecordedAtDesc(asset.getId()))
                .thenReturn(List.of(openIssue, resolvedIssue));
        when(operationalDecisionRepository.findResolvedIssueIds(List.of(600L, 601L)))
                .thenReturn(Set.of(601L));

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getOpenIssues()).extracting(MobileIssueSummaryResponse::getIssueId)
                .containsExactly(600L);
    }

    @Test
    void getAssetContext_includesOnlyActiveInspectionsAndWorkOrders() {
        User admin = user(1L, UserRole.ADMINISTRATOR);
        Asset asset = asset();
        stubAssetLookup(asset);
        when(userService.getById(1L)).thenReturn(admin);

        Inspection activeInspection = assignedInspection(700L, 20L);
        when(inspectionRepository.findByAsset_IdAndStatus(asset.getId(), InspectionStatus.ASSIGNED))
                .thenReturn(List.of(activeInspection));

        WorkOrder activeWorkOrder = createdWorkOrder(800L);
        when(workOrderRepository.findByAsset_IdAndStatusIn(
                asset.getId(), List.of(WorkOrderStatus.CREATED, WorkOrderStatus.ASSIGNED)))
                .thenReturn(List.of(activeWorkOrder));

        AssetContextResponse response = mobileService.getAssetContext(1L, asset.getCode());

        assertThat(response.getActiveInspections())
                .extracting(MobileInspectionSummaryResponse::getInspectionId)
                .containsExactly(700L);
        assertThat(response.getActiveWorkOrders())
                .extracting(MobileWorkOrderSummaryResponse::getWorkOrderId)
                .containsExactly(800L);
    }

    @Test
    void getAssetContext_allowedActionsAreConservativeForFieldEmployee() {
        Department department = department(1L, "Parks");
        User fieldEmployee = userWithDepartment(20L, UserRole.FIELD_EMPLOYEE, department);
        Asset asset = assetInDepartment(department);
        stubAssetLookup(asset);
        when(userService.getById(20L)).thenReturn(fieldEmployee);

        AssetContextResponse response = mobileService.getAssetContext(20L, asset.getCode());

        assertThat(response.getAllowedActions().isCanViewAsset()).isTrue();
        assertThat(response.getAllowedActions().isCanViewInspections()).isTrue();
        assertThat(response.getAllowedActions().isCanViewIssues()).isTrue();
        assertThat(response.getAllowedActions().isCanViewWorkOrders()).isTrue();
        assertThat(response.getAllowedActions().isCanCreateInspection()).isFalse();
        assertThat(response.getAllowedActions().isCanCreateIssue()).isTrue();
    }

    private void stubAssetLookup(Asset asset) {
        when(assetRepository.findByCodeIgnoreCase(asset.getCode())).thenReturn(Optional.of(asset));
        when(issueRepository.findAllByAsset_IdOrderByRecordedAtDesc(asset.getId())).thenReturn(List.of());
        when(inspectionRepository.findByAsset_IdAndStatus(asset.getId(), InspectionStatus.ASSIGNED))
                .thenReturn(List.of());
        when(workOrderRepository.findByAsset_IdAndStatusIn(
                asset.getId(), List.of(WorkOrderStatus.CREATED, WorkOrderStatus.ASSIGNED)))
                .thenReturn(List.of());
        when(inspectionRepository.findFirstByAsset_IdAndStatusOrderByCompletedAtDesc(
                asset.getId(), InspectionStatus.COMPLETED))
                .thenReturn(Optional.empty());
        when(maintenanceActivityRepository.findFirstByAsset_IdOrderByCompletedAtDesc(asset.getId()))
                .thenReturn(Optional.empty());
        when(preventiveMaintenancePlanRepository.findFirstByAsset_IdAndStatusOrderByCreatedAtDesc(
                asset.getId(), PreventiveMaintenancePlanStatus.ACTIVE))
                .thenReturn(Optional.empty());
        when(operationalDocumentService.listVisibleAssetOwnedDocuments(eq(asset), any()))
                .thenReturn(List.of());
    }

    private void stubAssetFound(Asset asset) {
        when(assetRepository.findByCodeIgnoreCase(asset.getCode())).thenReturn(Optional.of(asset));
    }

    private Issue issueForAsset(Asset asset, Long id) {
        Issue issue = new Issue(
                null,
                asset,
                "Sample issue",
                IssueSeverity.LOW,
                10L,
                LocalDateTime.now().minusDays(1));
        issue.setId(id);
        return issue;
    }

    private Department department(Long id, String name) {
        Department department = new Department(name);
        department.setId(id);
        return department;
    }

    private User userWithDepartment(Long id, UserRole role, Department department) {
        User user = new User("user" + id + "@test.com", "password", "Test User " + id, role);
        user.setId(id);
        user.setEnabled(true);
        user.setDepartment(department);
        return user;
    }

    private Asset assetInDepartment(Department department) {
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        Asset asset = new Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                AssetStatus.ACTIVE,
                LocalDate.of(2026, 6, 25),
                10L);
        asset.setId(5L);
        return asset;
    }

    private OperationalDocument assetOwnedDocument(Long id, Asset asset) {
        OperationalDocument document = new OperationalDocument(
                asset,
                OperationalDocumentOwnerType.ASSET,
                asset.getId(),
                OperationalDocumentType.REPORT,
                "street-light-manual.pdf",
                "stored-file-key",
                "application/pdf",
                1024L,
                "/tmp/internal-storage-path",
                null,
                30L,
                LocalDateTime.of(2026, 7, 4, 9, 30));
        document.setId(id);
        return document;
    }

    private Inspection completedInspection(Long id) {
        Inspection inspection = assignedInspection(id, 20L);
        inspection.complete(
                PhysicalCondition.GOOD,
                "All good",
                false,
                LocalDateTime.of(2026, 7, 4, 10, 15),
                20L);
        return inspection;
    }

    private MaintenanceActivity maintenanceActivityForAsset(Asset asset, Long id, Long workOrderId) {
        OperationalDecision decision = new OperationalDecision(
                null,
                asset,
                OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                "Proceed with maintenance",
                30L,
                LocalDateTime.now().minusDays(2));
        decision.setId(910L);
        WorkOrder workOrder = new WorkOrder(
                decision,
                asset,
                WorkType.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusDays(1));
        workOrder.setId(workOrderId);
        workOrder.assign(20L, 40L, LocalDateTime.now().minusHours(4));
        workOrder.complete();
        MaintenanceActivity maintenanceActivity = new MaintenanceActivity(
                workOrder,
                asset,
                20L,
                "Replaced damaged swing chain",
                LocalDateTime.of(2026, 7, 3, 15, 30));
        maintenanceActivity.setId(id);
        return maintenanceActivity;
    }

    private PreventiveMaintenancePlan activePreventivePlan(Long id, Asset asset) {
        PreventiveMaintenancePlan plan = new PreventiveMaintenancePlan(
                asset,
                "LIGHT-MONTHLY",
                "Monthly lighting inspection",
                null,
                1,
                PreventiveMaintenancePlanStatus.ACTIVE,
                PreventiveMaintenancePlanPriority.MEDIUM,
                PlanTargetAction.CREATE_INSPECTION,
                null);
        plan.setId(id);
        PlanBusinessTrigger trigger = new PlanBusinessTrigger(
                PlanTriggerType.TIME,
                "{\"intervalDays\":30}",
                true);
        plan.setBusinessTrigger(trigger);
        return plan;
    }

    private User user(Long id, UserRole role) {
        User user = new User("john@test.com", "password", "John Smith", role);
        user.setId(id);
        user.setEnabled(true);
        Department department = new Department("Parks");
        department.setId(1L);
        user.setDepartment(department);
        return user;
    }

    private Inspection assignedInspection(Long id, Long assignedToUserId) {
        BusinessTrigger trigger = businessTrigger();
        Inspection inspection = new Inspection(
                trigger.getAsset(),
                trigger,
                assignedToUserId,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7));
        inspection.setId(id);
        return inspection;
    }

    private Inspection templatedInspection(Long id, Long assignedToUserId, Long templateId) {
        Inspection inspection = assignedInspection(id, assignedToUserId);
        inspection.setInspectionTemplate(publishedTemplate(templateId));
        return inspection;
    }

    private Inspection withExpectedDate(Inspection inspection, LocalDate date) {
        try {
            var field = Inspection.class.getDeclaredField("expectedCompletionDate");
            field.setAccessible(true);
            field.set(inspection, date);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        return inspection;
    }

    private Inspection withPriority(Inspection inspection, InspectionPriority priority) {
        try {
            var field = Inspection.class.getDeclaredField("priority");
            field.setAccessible(true);
            field.set(inspection, priority);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
        return inspection;
    }

    private InspectionTemplate publishedTemplate(Long id) {
        AssetCategory category = new AssetCategory("Playground");
        category.setId(2L);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Inspection",
                null,
                category,
                1,
                InspectionTemplateStatus.PUBLISHED);
        template.setId(id);
        return template;
    }

    private InspectionTemplateQuestion templateQuestion(Long id, InspectionTemplate template) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                template,
                "Is equipment safe?",
                "SAFE",
                "Check overall safety",
                InspectionTemplateQuestionType.BOOLEAN,
                true,
                1);
        question.setId(id);
        return question;
    }

    private InspectionTemplateQuestionChoice templateChoice(
            Long id,
            InspectionTemplateQuestion question,
            String code,
            String label) {
        InspectionTemplateQuestionChoice choice = new InspectionTemplateQuestionChoice(
                question, code, label, 1);
        choice.setId(id);
        return choice;
    }

    private InspectionAnswer inspectionAnswer(
            Inspection inspection,
            InspectionTemplateQuestion question,
            boolean value) {
        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                value,
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
    }

    private WorkOrder assignedWorkOrder(Long id, Long assignedToUserId) {
        WorkOrder workOrder = createdWorkOrder(id);
        workOrder.assign(assignedToUserId, 40L, LocalDateTime.now().minusHours(2));
        return workOrder;
    }

    private WorkOrder createdWorkOrder(Long id) {
        OperationalDecision decision = decision();
        WorkOrder workOrder = new WorkOrder(
                decision,
                decision.getAsset(),
                WorkType.INTERNAL_MAINTENANCE,
                "Replace damaged swing chain",
                WorkOrderPriority.HIGH,
                40L,
                LocalDateTime.now().minusHours(3));
        workOrder.setId(id);
        return workOrder;
    }

    private OperationalDecision decision() {
        Issue issue = new Issue(
                null,
                asset(),
                "Damaged swing chain",
                IssueSeverity.HIGH,
                30L,
                LocalDateTime.now().minusDays(1));
        issue.setId(500L);
        OperationalDecision decision = new OperationalDecision(
                issue,
                issue.getAsset(),
                OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                "Proceed with maintenance",
                30L,
                LocalDateTime.now().minusHours(4));
        decision.setId(900L);
        return decision;
    }

    private BusinessTrigger businessTrigger() {
        Asset asset = asset();
        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
                false,
                10L);
        trigger.setId(1L);
        return trigger;
    }

    private Asset asset() {
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
                10L);
        asset.setId(5L);
        return asset;
    }
}
