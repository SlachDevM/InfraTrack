package com.infratrack.mobile;

import com.infratrack.asset.AssetRepository;
import com.infratrack.delegatedauthority.DelegatedAuthorityService;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspection.InspectionStatus;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionChoiceRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionRepository;
import com.infratrack.issue.IssueRepository;
import com.infratrack.maintenanceactivity.MaintenanceActivityRepository;
import com.infratrack.operationaldocument.OperationalDocumentService;
import com.infratrack.organization.policy.visibility.InspectionVisibilityPolicyService;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlanRepository;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderRepository;
import com.infratrack.workorder.WorkOrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileServiceSyncScopingTest {

    private static final long UPDATED_SINCE = 1_700_000_000_000L;

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
                workOrderRepository,
                maintenanceActivityRepository,
                preventiveMaintenancePlanRepository,
                operationalDocumentService,
                userNameLookup);
    }

    @Test
    void listScopedInspectionsForSync_administratorUsesAssignedStatusScope() {
        User admin = user(1L, UserRole.ADMINISTRATOR, null);
        Inspection inspection = inspection(100L, 20L);
        when(userService.getById(1L)).thenReturn(admin);
        when(inspectionRepository.findByStatus(InspectionStatus.ASSIGNED)).thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(admin);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByStatus(InspectionStatus.ASSIGNED);
        verify(inspectionRepository, never()).findByAssignedToUserId(20L);
    }

    @Test
    void listScopedInspectionsForSync_administratorIncrementalUsesUpdatedAtFilter() {
        User admin = user(1L, UserRole.ADMINISTRATOR, null);
        Inspection inspection = inspection(100L, 20L);
        when(userService.getById(1L)).thenReturn(admin);
        when(inspectionRepository.findByStatusAndUpdatedAtGreaterThanEqual(InspectionStatus.ASSIGNED, UPDATED_SINCE))
                .thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(admin, UPDATED_SINCE);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByStatusAndUpdatedAtGreaterThanEqual(
                InspectionStatus.ASSIGNED, UPDATED_SINCE);
    }

    @Test
    void listScopedInspectionsForSync_administratorIncrementalUsesWatermarkWindow() {
        User admin = user(1L, UserRole.ADMINISTRATOR, null);
        Inspection inspection = inspection(100L, 20L);
        long updatedUntil = UPDATED_SINCE + 1_000L;
        when(userService.getById(1L)).thenReturn(admin);
        when(inspectionRepository.findByStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                InspectionStatus.ASSIGNED, UPDATED_SINCE, updatedUntil))
                .thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(admin, UPDATED_SINCE, updatedUntil);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByStatusAndUpdatedAtGreaterThanEqualAndUpdatedAtLessThanEqual(
                InspectionStatus.ASSIGNED, UPDATED_SINCE, updatedUntil);
    }

    @Test
    void listScopedInspectionsForSync_managerUsesDepartmentAssignedScope() {
        Department department = department(5L);
        User manager = user(2L, UserRole.MANAGER, department);
        Inspection inspection = inspection(100L, 20L);
        when(userService.getById(2L)).thenReturn(manager);
        when(inspectionRepository.findByAsset_Department_IdAndStatus(5L, InspectionStatus.ASSIGNED))
                .thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(manager);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByAsset_Department_IdAndStatus(5L, InspectionStatus.ASSIGNED);
        verify(inspectionRepository, never()).findByAssignedToUserId(20L);
    }

    @Test
    void listScopedInspectionsForSync_managerIncrementalUsesDepartmentUpdatedAtFilter() {
        Department department = department(5L);
        User manager = user(2L, UserRole.MANAGER, department);
        Inspection inspection = inspection(100L, 20L);
        when(userService.getById(2L)).thenReturn(manager);
        when(inspectionRepository.findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqual(
                5L, InspectionStatus.ASSIGNED, UPDATED_SINCE))
                .thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(manager, UPDATED_SINCE);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByAsset_Department_IdAndStatusAndUpdatedAtGreaterThanEqual(
                5L, InspectionStatus.ASSIGNED, UPDATED_SINCE);
    }

    @Test
    void listScopedInspectionsForSync_managerWithoutDepartmentReturnsEmpty() {
        User manager = user(2L, UserRole.MANAGER, null);
        when(userService.getById(2L)).thenReturn(manager);

        List<Inspection> result = mobileService.listScopedInspectionsForSync(manager, UPDATED_SINCE);

        assertThat(result).isEmpty();
        verify(inspectionRepository, never()).findByAsset_Department_IdAndStatus(5L, InspectionStatus.ASSIGNED);
    }

    @Test
    void listScopedInspectionsForSync_fieldEmployeeUsesAssignedUserScope() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE, department(1L));
        Inspection inspection = inspection(100L, 20L);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findByAssignedToUserId(20L)).thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(fieldEmployee);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByAssignedToUserId(20L);
        verify(inspectionRepository, never()).findByStatus(InspectionStatus.ASSIGNED);
    }

    @Test
    void listScopedInspectionsForSync_fieldEmployeeIncrementalUsesAssignedUserUpdatedAtFilter() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE, department(1L));
        Inspection inspection = inspection(100L, 20L);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(inspectionRepository.findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(20L, UPDATED_SINCE))
                .thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(fieldEmployee, UPDATED_SINCE);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(20L, UPDATED_SINCE);
    }

    @Test
    void listScopedInspectionsForSync_contractorUsesAssignedUserScope() {
        User contractor = user(30L, UserRole.CONTRACTOR, department(1L));
        Inspection inspection = inspection(100L, 30L);
        when(userService.getById(30L)).thenReturn(contractor);
        when(inspectionRepository.findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(30L, UPDATED_SINCE))
                .thenReturn(List.of(inspection));

        List<Inspection> result = mobileService.listScopedInspectionsForSync(contractor, UPDATED_SINCE);

        assertThat(result).hasSize(1);
        verify(inspectionRepository).findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(30L, UPDATED_SINCE);
    }

    @Test
    void listScopedWorkOrdersForSync_administratorUsesAssignedStatusScope() {
        User admin = user(1L, UserRole.ADMINISTRATOR, null);
        WorkOrder workOrder = workOrder(100L);
        when(userService.getById(1L)).thenReturn(admin);
        when(workOrderRepository.findByStatus(WorkOrderStatus.ASSIGNED)).thenReturn(List.of(workOrder));

        List<WorkOrder> result = mobileService.listScopedWorkOrdersForSync(admin);

        assertThat(result).hasSize(1);
        verify(workOrderRepository).findByStatus(WorkOrderStatus.ASSIGNED);
        verify(workOrderRepository, never()).findByAssignedToUserId(20L);
    }

    @Test
    void listScopedWorkOrdersForSync_administratorIncrementalUsesUpdatedAtFilter() {
        User admin = user(1L, UserRole.ADMINISTRATOR, null);
        WorkOrder workOrder = workOrder(100L);
        when(userService.getById(1L)).thenReturn(admin);
        when(workOrderRepository.findByStatusAndUpdatedAtGreaterThanEqual(WorkOrderStatus.ASSIGNED, UPDATED_SINCE))
                .thenReturn(List.of(workOrder));

        List<WorkOrder> result = mobileService.listScopedWorkOrdersForSync(admin, UPDATED_SINCE);

        assertThat(result).hasSize(1);
        verify(workOrderRepository).findByStatusAndUpdatedAtGreaterThanEqual(
                WorkOrderStatus.ASSIGNED, UPDATED_SINCE);
    }

    @Test
    void listScopedWorkOrdersForSync_managerUsesDepartmentAssignedScope() {
        Department department = department(5L);
        User manager = user(2L, UserRole.MANAGER, department);
        WorkOrder workOrder = workOrder(100L);
        when(userService.getById(2L)).thenReturn(manager);
        when(workOrderRepository.findByAsset_Department_IdAndStatus(5L, WorkOrderStatus.ASSIGNED))
                .thenReturn(List.of(workOrder));

        List<WorkOrder> result = mobileService.listScopedWorkOrdersForSync(manager);

        assertThat(result).hasSize(1);
        verify(workOrderRepository).findByAsset_Department_IdAndStatus(5L, WorkOrderStatus.ASSIGNED);
        verify(workOrderRepository, never()).findByAssignedToUserId(20L);
    }

    @Test
    void listScopedWorkOrdersForSync_fieldEmployeeUsesAssignedUserScope() {
        User fieldEmployee = user(20L, UserRole.FIELD_EMPLOYEE, department(1L));
        WorkOrder workOrder = workOrder(100L);
        when(userService.getById(20L)).thenReturn(fieldEmployee);
        when(workOrderRepository.findByAssignedToUserId(20L)).thenReturn(List.of(workOrder));

        List<WorkOrder> result = mobileService.listScopedWorkOrdersForSync(fieldEmployee);

        assertThat(result).hasSize(1);
        verify(workOrderRepository).findByAssignedToUserId(20L);
        verify(workOrderRepository, never()).findByStatus(WorkOrderStatus.ASSIGNED);
    }

    @Test
    void listScopedWorkOrdersForSync_contractorUsesAssignedUserScope() {
        User contractor = user(30L, UserRole.CONTRACTOR, department(1L));
        WorkOrder workOrder = workOrder(100L);
        when(userService.getById(30L)).thenReturn(contractor);
        when(workOrderRepository.findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(30L, UPDATED_SINCE))
                .thenReturn(List.of(workOrder));

        List<WorkOrder> result = mobileService.listScopedWorkOrdersForSync(contractor, UPDATED_SINCE);

        assertThat(result).hasSize(1);
        verify(workOrderRepository).findByAssignedToUserIdAndUpdatedAtGreaterThanEqual(30L, UPDATED_SINCE);
    }

    private WorkOrder workOrder(Long id) {
        return org.mockito.Mockito.mock(WorkOrder.class);
    }

    private User user(Long id, UserRole role, Department department) {
        User user = new User();
        user.setId(id);
        user.setEmail("user" + id + "@test.com");
        user.setName("User " + id);
        user.setRole(role);
        user.setDepartment(department);
        user.setEnabled(true);
        return user;
    }

    private Department department(Long id) {
        Department department = new Department("Dept " + id);
        department.setId(id);
        return department;
    }

    private Inspection inspection(Long id, Long assignedToUserId) {
        return org.mockito.Mockito.mock(Inspection.class);
    }
}
