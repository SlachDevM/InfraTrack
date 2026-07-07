package com.infratrack.mobile.sync;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.PhysicalCondition;
import com.infratrack.issue.Issue;
import com.infratrack.issue.IssueSeverity;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.MobileService;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.operationaldecision.OperationalDecision;
import com.infratrack.operationaldecision.OperationalDecisionOutcome;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserRole;
import com.infratrack.workorder.WorkOrder;
import com.infratrack.workorder.WorkOrderPriority;
import com.infratrack.workorder.WorkOrderStatus;
import com.infratrack.workorder.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkOrderSyncDeltaServiceTest {

    @Mock
    private MobileService mobileService;

    @Mock
    private MobileAuthorizationService authorizationService;

    @Mock
    private UserNameLookup userNameLookup;

    private WorkOrderSyncDeltaService deltaService;

    @BeforeEach
    void setUp() {
        deltaService = new WorkOrderSyncDeltaService(
                mobileService,
                authorizationService,
                userNameLookup);
    }

    @Test
    void build_nullSyncToken_returnsFullWorkOrderDelta() {
        WorkOrder workOrder = workOrder(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, null, null)).thenReturn(List.of(workOrder));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(authorizationService.canCompleteMaintenance(fieldUser, workOrder)).thenReturn(true);

        var result = buildWithToken(fieldUser, null, null);

        assertThat(result.warnings()).isEmpty();
        assertThat(result.workOrders()).hasSize(1);
        assertThat(result.workOrders().get(0).getWorkOrderId()).isEqualTo(100L);
        assertThat(result.workOrders().get(0).getAssignedToName()).isEqualTo("Field User");
        assertThat(result.workOrders().get(0).isCompletionEligible()).isTrue();
        verify(mobileService).listScopedWorkOrdersForSync(fieldUser, null, null);
    }

    @Test
    void build_validSyncToken_queriesWorkOrdersUpdatedSinceToken() {
        WorkOrder changed = workOrder(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        long sinceMillis = Instant.parse("2026-07-05T08:00:00Z").toEpochMilli();
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, sinceMillis, null)).thenReturn(List.of(changed));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(authorizationService.canCompleteMaintenance(fieldUser, changed)).thenReturn(true);

        String syncToken = SyncToken.issue(Instant.parse("2026-07-05T08:00:00Z")).toOpaqueValue();
        var result = buildWithToken(fieldUser, syncToken, null);

        assertThat(result.warnings()).isEmpty();
        assertThat(result.workOrders()).hasSize(1);
        verify(mobileService).listScopedWorkOrdersForSync(fieldUser, sinceMillis, null);
        verify(mobileService, never()).listScopedWorkOrdersForSync(fieldUser, null, null);
    }

    @Test
    void build_invalidSyncToken_returnsFullDeltaWithWarning() {
        WorkOrder workOrder = workOrder(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, null, null)).thenReturn(List.of(workOrder));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(authorizationService.canCompleteMaintenance(fieldUser, workOrder)).thenReturn(false);

        var result = buildWithToken(fieldUser, "not-a-valid-token", null);

        assertThat(result.warnings()).hasSize(1);
        assertThat(result.warnings().get(0).getCode()).isEqualTo(SyncWarningCode.FULL_SYNC_REQUIRED);
        assertThat(result.workOrders()).hasSize(1);
        verify(mobileService).listScopedWorkOrdersForSync(fieldUser, null, null);
    }

    @Test
    void build_onlyIncludesScopedWorkOrdersFromMobileService() {
        WorkOrder visible = workOrder(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, null, null)).thenReturn(List.of(visible));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(authorizationService.canCompleteMaintenance(fieldUser, visible)).thenReturn(true);

        var result = buildWithToken(fieldUser, null, null);

        assertThat(result.workOrders()).extracting("workOrderId").containsExactly(100L);
    }

    @Test
    void build_mapsDraftCompletionNotes() {
        WorkOrder workOrder = workOrder(100L, 20L, 5_000L);
        workOrder.saveDraftCompletionNotes("Draft completion text");
        User fieldUser = user(20L);
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, null, null)).thenReturn(List.of(workOrder));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(authorizationService.canCompleteMaintenance(fieldUser, workOrder)).thenReturn(true);

        var result = buildWithToken(fieldUser, null, null);

        assertThat(result.workOrders().get(0).getDraftCompletionNotes()).isEqualTo("Draft completion text");
    }

    @Test
    void build_emptyScopedList_returnsEmptyDelta() {
        User fieldUser = user(20L);
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, null, null)).thenReturn(List.of());

        var result = buildWithToken(fieldUser, null, null);

        assertThat(result.workOrders()).isEmpty();
        verify(userNameLookup, never()).resolveNames(any());
    }

    @Test
    void build_passesWatermarkUpperBoundToMobileService() {
        WorkOrder workOrder = workOrder(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        Instant watermark = Instant.parse("2026-07-05T10:00:00Z");
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, null, watermark.toEpochMilli()))
                .thenReturn(List.of(workOrder));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(authorizationService.canCompleteMaintenance(fieldUser, workOrder)).thenReturn(true);

        deltaService.buildDeltaRecords(fieldUser, null, watermark.toEpochMilli());

        verify(mobileService).listScopedWorkOrdersForSync(fieldUser, null, watermark.toEpochMilli());
    }

    @Test
    void build_mapsOperationalDecisionIdAndAssetFields() {
        WorkOrder workOrder = workOrder(100L, 20L, 5_000L);
        User fieldUser = user(20L);
        when(mobileService.listScopedWorkOrdersForSync(fieldUser, null, null)).thenReturn(List.of(workOrder));
        when(userNameLookup.resolveNames(any())).thenReturn(Map.of(20L, "Field User"));
        when(authorizationService.canCompleteMaintenance(fieldUser, workOrder)).thenReturn(true);

        var result = buildWithToken(fieldUser, null, null);

        var delta = result.workOrders().get(0);
        assertThat(delta.getAssetId()).isEqualTo(50L);
        assertThat(delta.getAssetName()).isEqualTo("Central Playground");
        assertThat(delta.getAssetCategoryName()).isEqualTo("Playground");
        assertThat(delta.getOperationalDecisionId()).isEqualTo(200L);
        assertThat(delta.getStatus()).isEqualTo(WorkOrderStatus.ASSIGNED);
        assertThat(delta.getPriority()).isEqualTo(WorkOrderPriority.HIGH);
        assertThat(delta.getWorkType()).isEqualTo(WorkType.INTERNAL_MAINTENANCE);
    }

    private DeltaBuildResult buildWithToken(User user, String syncToken, Instant watermark) {
        List<com.infratrack.mobile.sync.dto.SyncWarningResponse> warnings = new java.util.ArrayList<>();
        Long updatedSince = SyncDeltaTokenSupport.resolveUpdatedSinceMillis(syncToken, warnings);
        Long updatedUntil = watermark != null ? watermark.toEpochMilli() : null;
        return new DeltaBuildResult(
                deltaService.buildDeltaRecords(user, updatedSince, updatedUntil),
                warnings);
    }

    private record DeltaBuildResult(
            List<com.infratrack.mobile.sync.dto.SyncWorkOrderDeltaResponse> workOrders,
            List<com.infratrack.mobile.sync.dto.SyncWarningResponse> warnings) {
    }

    private User user(Long id) {
        User user = new User();
        user.setId(id);
        user.setEmail("field@test.com");
        user.setRole(UserRole.FIELD_EMPLOYEE);
        return user;
    }

    private WorkOrder workOrder(Long id, Long assignedToUserId, long updatedAt) {
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
        asset.setId(50L);

        BusinessTrigger trigger = new BusinessTrigger(
                asset,
                BusinessTriggerType.CUSTOMER_REQUEST,
                "Damaged equipment reported",
                false,
                10L);
        trigger.setId(1L);
        Inspection inspection = new Inspection(
                asset,
                trigger,
                assignedToUserId,
                10L,
                InspectionPriority.NORMAL,
                LocalDate.now().plusDays(7));
        inspection.setId(100L);
        inspection.complete(
                PhysicalCondition.POOR,
                "Damaged swing chain observed",
                true,
                LocalDateTime.now().minusHours(2),
                assignedToUserId);

        Issue issue = new Issue(
                inspection,
                asset,
                "Broken swing chain requires replacement",
                IssueSeverity.HIGH,
                assignedToUserId,
                LocalDateTime.now().minusHours(1));
        issue.setId(300L);

        OperationalDecision decision = new OperationalDecision(
                issue,
                asset,
                OperationalDecisionOutcome.INTERNAL_MAINTENANCE,
                "Replace swing",
                10L,
                LocalDateTime.now().minusHours(1));
        decision.setId(200L);

        WorkOrder workOrder = new WorkOrder(
                decision,
                asset,
                WorkType.INTERNAL_MAINTENANCE,
                "Fix swing",
                WorkOrderPriority.HIGH,
                10L,
                LocalDateTime.now().minusHours(1));
        workOrder.setId(id);
        workOrder.assign(assignedToUserId, 10L, LocalDateTime.of(2026, 7, 1, 9, 0));
        setUpdatedAt(workOrder, updatedAt);
        return workOrder;
    }

    private static void setUpdatedAt(WorkOrder workOrder, long updatedAt) {
        try {
            Field field = WorkOrder.class.getDeclaredField("updatedAt");
            field.setAccessible(true);
            field.set(workOrder, updatedAt);
        } catch (ReflectiveOperationException ex) {
            throw new IllegalStateException(ex);
        }
    }
}
