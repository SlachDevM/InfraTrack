package com.infratrack.mobile.sync;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;
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
                new SyncMetricsRecorder(meterRegistry));
        lenient().when(inspectionSyncDeltaService.build(any(User.class), any()))
                .thenReturn(new InspectionSyncDeltaService.SyncDeltaBuildResult(
                        SyncDeltaResponse.empty(),
                        List.of()));
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
        assertThat(response.getDelta().getAssets()).isEmpty();
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
        SyncDeltaResponse delta = SyncDeltaResponse.empty();
        delta.setInspections(List.of(deltaInspection));
        when(inspectionSyncDeltaService.build(fieldUser, null))
                .thenReturn(new InspectionSyncDeltaService.SyncDeltaBuildResult(delta, List.of()));

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

        InOrder order = inOrder(inspectionSyncDeltaService);
        order.verify(inspectionSyncDeltaService).build(fieldUser, null);
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
        when(inspectionSyncDeltaService.build(fieldUser, "bad-token"))
                .thenReturn(new InspectionSyncDeltaService.SyncDeltaBuildResult(
                        SyncDeltaResponse.empty(),
                        List.of(new SyncWarningResponse(
                                SyncWarningCode.FULL_SYNC_REQUIRED,
                                "Sync token is invalid; returning full inspection delta."))));

        SyncRequest request = validRequest();
        request.setSyncToken("bad-token");

        mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(meterRegistry.get("mobile.sync.invalid_token").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.full_sync_required").counter().count()).isEqualTo(1.0);
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
