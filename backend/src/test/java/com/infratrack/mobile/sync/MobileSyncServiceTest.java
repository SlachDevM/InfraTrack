package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncAssetDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncDashboardDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;
import com.infratrack.mobile.sync.dto.SyncWorkOrderDeltaResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileSyncServiceTest {

    private static final Long FIELD_USER_ID = 20L;
    private static final Instant FIXED_INSTANT = Instant.parse("2026-07-05T08:30:00Z");

    @Mock
    private MobileAuthorizationService authorizationService;

    @Mock
    private InspectionService inspectionService;

    @Mock
    private InspectionSyncDeltaService inspectionSyncDeltaService;

    @Mock
    private WorkOrderSyncDeltaService workOrderSyncDeltaService;

    @Mock
    private DashboardSyncDeltaService dashboardSyncDeltaService;

    @Mock
    private AssetSyncDeltaService assetSyncDeltaService;

    private SimpleMeterRegistry meterRegistry;

    private MobileSyncService mobileSyncService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        meterRegistry = new SimpleMeterRegistry();
        InspectionProgressSyncOperationHandler handler = new InspectionProgressSyncOperationHandler(
                inspectionService,
                new com.fasterxml.jackson.databind.ObjectMapper(),
                clock);
        mobileSyncService = new MobileSyncService(
                authorizationService,
                clock,
                new DefaultSyncTokenService(clock),
                new DefaultSyncOperationProcessor(
                        List.of(handler),
                        SyncTestIdempotencySupport.passthroughService(clock)),
                inspectionSyncDeltaService,
                workOrderSyncDeltaService,
                dashboardSyncDeltaService,
                assetSyncDeltaService,
                new SyncMetricsRecorder(meterRegistry));
        lenient().when(inspectionSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(workOrderSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(dashboardSyncDeltaService.buildSnapshot(any(User.class), eq(FIXED_INSTANT)))
                .thenReturn(dashboardDelta());
        lenient().when(assetSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
    }

    @Test
    void sync_validRequest_returnsEmptyOperations() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncRequest request = validRequest();
        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getServerTime()).isEqualTo(FIXED_INSTANT);
        assertThat(response.getProtocolVersion()).isEqualTo(SyncProtocolVersion.CURRENT);
        assertThat(response.getNextSyncToken()).isNotBlank();
        assertThat(response.getDelta().getWorkOrders()).isEmpty();
        assertThat(response.getDelta().getDashboard()).isNotNull();
        assertThat(response.getDelta().getDashboard().getAssignedInspections()).isEqualTo(3L);
        assertThat(response.getOperations()).isEmpty();
        assertThat(response.getConflicts()).isEmpty();
        assertThat(response.getWarnings()).isEmpty();
        assertThat(response.isRequiresFullSync()).isFalse();
        verify(authorizationService).requireMobileUser(FIELD_USER_ID);
    }

    @Test
    void sync_validSaveInspectionProgress_returnsAcceptedOperation() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);
        when(inspectionService.saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID)))
                .thenReturn(new InspectionResponse());

        SyncInspectionDeltaResponse deltaInspection = new SyncInspectionDeltaResponse();
        deltaInspection.setId(123L);
        when(inspectionSyncDeltaService.buildDeltaRecords(eq(fieldUser), isNull(), eq(FIXED_INSTANT.toEpochMilli())))
                .thenReturn(List.of(deltaInspection));

        SyncRequest request = validRequest();
        request.setPendingOperations(List.of(progressOperation()));

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getOperations()).hasSize(1);
        assertThat(response.getOperations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(response.getDelta().getInspections()).hasSize(1);
        assertThat(response.getDelta().getInspections().get(0).getId()).isEqualTo(123L);
        verify(inspectionService).saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID));
    }

    @Test
    void sync_processesOperationsBeforeBuildingDelta() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncRequest request = validRequest();
        mobileSyncService.sync(FIELD_USER_ID, request);

        InOrder order = inOrder(
                inspectionSyncDeltaService,
                workOrderSyncDeltaService,
                dashboardSyncDeltaService,
                assetSyncDeltaService);
        order.verify(inspectionSyncDeltaService).buildDeltaRecords(fieldUser, null, FIXED_INSTANT.toEpochMilli());
        order.verify(workOrderSyncDeltaService).buildDeltaRecords(fieldUser, null, FIXED_INSTANT.toEpochMilli());
        order.verify(dashboardSyncDeltaService).buildSnapshot(fieldUser, FIXED_INSTANT);
        order.verify(assetSyncDeltaService).buildDeltaRecords(eq(fieldUser), any(), any());
    }

    @Test
    void sync_conflictingOperation_doesNotFailWholeSync() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);
        doThrow(new ForbiddenOperationException("Only the assigned user can save inspection answers"))
                .when(inspectionService)
                .saveInspectionProgress(eq(123L), org.mockito.ArgumentMatchers.any(), eq(FIELD_USER_ID));

        SyncRequest request = validRequest();
        request.setPendingOperations(List.of(progressOperation()));

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getOperations()).hasSize(1);
        assertThat(response.getOperations().get(0).getStatus()).isEqualTo(SyncOperationStatus.CONFLICT);
        assertThat(response.getConflicts()).hasSize(1);
        assertThat(response.getConflicts().get(0).getOperationId()).isEqualTo("op-1");
        assertThat(response.getNextSyncToken()).isNotBlank();
    }

    @Test
    void sync_operationalCoordinator_isRejected() {
        doThrow(new ForbiddenOperationException("Mobile API access is not available for this role."))
                .when(authorizationService).requireMobileUser(FIELD_USER_ID);

        assertThatThrownBy(() -> mobileSyncService.sync(FIELD_USER_ID, validRequest()))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Mobile API access is not available for this role.");
    }

    @Test
    void sync_invalidSyncToken_recordsInvalidTokenAndFullSyncMetrics() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncRequest request = validRequest();
        request.setSyncToken("bad-token");

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.isRequiresFullSync()).isTrue();
        assertThat(response.getWarnings()).hasSize(1);
        assertThat(response.getWarnings().get(0).getCode()).isEqualTo(SyncWarningCode.FULL_SYNC_REQUIRED);
        assertThat(meterRegistry.get("mobile.sync.invalid_token").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.full_sync_required").counter().count()).isEqualTo(1.0);
        verify(inspectionSyncDeltaService).buildDeltaRecords(fieldUser, null, FIXED_INSTANT.toEpochMilli());
        verify(workOrderSyncDeltaService).buildDeltaRecords(fieldUser, null, FIXED_INSTANT.toEpochMilli());
        verify(dashboardSyncDeltaService).buildSnapshot(fieldUser, FIXED_INSTANT);
    }

    @Test
    void sync_invalidSyncToken_stillIncludesDashboardDelta() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncRequest request = validRequest();
        request.setSyncToken("bad-token");

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getDelta().getDashboard()).isNotNull();
        assertThat(response.getDelta().getDashboard().getAssignedInspections()).isEqualTo(3L);
        verify(dashboardSyncDeltaService).buildSnapshot(fieldUser, FIXED_INSTANT);
    }

    @Test
    void sync_invalidSyncToken_setsRequiresFullSyncTrue() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncRequest request = validRequest();
        request.setSyncToken("bad-token");

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.isRequiresFullSync()).isTrue();
        assertThat(SyncToken.fromOpaqueValue(response.getNextSyncToken()).getIssuedAt()).isEqualTo(FIXED_INSTANT);
    }

    @Test
    void sync_returnsWorkOrderDeltaFromService() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);
        SyncWorkOrderDeltaResponse workOrderDelta = new SyncWorkOrderDeltaResponse();
        workOrderDelta.setWorkOrderId(500L);
        workOrderDelta.setDraftCompletionNotes("Saved offline");
        when(workOrderSyncDeltaService.buildDeltaRecords(eq(fieldUser), isNull(), eq(FIXED_INSTANT.toEpochMilli())))
                .thenReturn(List.of(workOrderDelta));

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, validRequest());

        assertThat(response.getDelta().getWorkOrders()).hasSize(1);
        assertThat(response.getDelta().getWorkOrders().get(0).getWorkOrderId()).isEqualTo(500L);
        assertThat(response.getDelta().getWorkOrders().get(0).getDraftCompletionNotes()).isEqualTo("Saved offline");
    }

    @Test
    void sync_invalidSyncToken_emitsSingleFullSyncWarning() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncRequest request = validRequest();
        request.setSyncToken("bad-token");

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getWarnings()).hasSize(1);
    }

    @Test
    void sync_validSyncToken_setsRequiresFullSyncFalse() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, validRequest());

        assertThat(response.isRequiresFullSync()).isFalse();
    }

    @Test
    void sync_returnsAssetDeltaFromService() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);
        SyncAssetDeltaResponse assetDelta = new SyncAssetDeltaResponse();
        com.infratrack.mobile.dto.AssetContextSummaryResponse summary =
                com.infratrack.mobile.dto.AssetContextSummaryResponse.from(
                        assetEntity(50L));
        assetDelta.setAsset(summary);
        when(assetSyncDeltaService.buildDeltaRecords(eq(fieldUser), any(), any()))
                .thenReturn(List.of(assetDelta));

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, validRequest());

        assertThat(response.getDelta().getAssets()).hasSize(1);
        assertThat(response.getDelta().getAssets().get(0).getAsset().getId()).isEqualTo(50L);
    }

    @Test
    void sync_invalidSyncToken_stillIncludesAssetDelta() {
        User fieldUser = user(UserRole.FIELD_EMPLOYEE);
        when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);
        SyncAssetDeltaResponse assetDelta = new SyncAssetDeltaResponse();
        when(assetSyncDeltaService.buildDeltaRecords(eq(fieldUser), any(), any()))
                .thenReturn(List.of(assetDelta));

        SyncRequest request = validRequest();
        request.setSyncToken("bad-token");

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getDelta().getAssets()).hasSize(1);
        verify(assetSyncDeltaService).buildDeltaRecords(eq(fieldUser), any(), any());
    }

    private com.infratrack.asset.Asset assetEntity(Long id) {
        com.infratrack.department.Department department = new com.infratrack.department.Department("Parks");
        department.setId(1L);
        com.infratrack.assetcategory.AssetCategory category =
                new com.infratrack.assetcategory.AssetCategory("Playground");
        category.setId(2L);
        com.infratrack.asset.Asset asset = new com.infratrack.asset.Asset(
                "Central Playground",
                department,
                category,
                "Memorial Park",
                com.infratrack.asset.AssetStatus.ACTIVE,
                java.time.LocalDate.of(2026, 6, 25),
                10L);
        asset.setId(id);
        return asset;
    }

    private SyncDashboardDeltaResponse dashboardDelta() {
        SyncDashboardDeltaResponse dashboard = new SyncDashboardDeltaResponse();
        dashboard.setGeneratedAt(FIXED_INSTANT.toEpochMilli());
        dashboard.setAssignedInspections(3L);
        dashboard.setAssignedWorkOrders(2L);
        dashboard.setOverdueInspections(1L);
        dashboard.setOverdueWorkOrders(0L);
        dashboard.setCompletedToday(4L);
        return dashboard;
    }

    private SyncRequest validRequest() {
        SyncRequest request = new SyncRequest();
        request.setClientId("android-install-uuid");
        request.setClientVersion("1");
        request.setAppVersion("1.1.0");
        return request;
    }

    private PendingOperationRequest progressOperation() {
        PendingOperationRequest pending = new PendingOperationRequest();
        pending.setOperationId("op-1");
        pending.setEntityType("INSPECTION");
        pending.setEntityId(123L);
        pending.setOperationType("SAVE_INSPECTION_PROGRESS");
        pending.setPayload("{\"answers\":[]}");
        return pending;
    }

    private User user(UserRole role) {
        User user = new User();
        user.setId(FIELD_USER_ID);
        user.setEmail("field@test.com");
        user.setRole(role);
        return user;
    }
}
