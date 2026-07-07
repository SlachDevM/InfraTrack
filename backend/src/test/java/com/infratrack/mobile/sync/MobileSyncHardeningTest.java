package com.infratrack.mobile.sync;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncOperationStatus;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MobileSyncHardeningTest {

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

    @Mock
    private ReferenceDataSyncDeltaService referenceDataSyncDeltaService;

    private SimpleMeterRegistry meterRegistry;
    private MobileSyncService mobileSyncService;
    private ListAppender<ILoggingEvent> logAppender;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(FIXED_INSTANT, ZoneOffset.UTC);
        InspectionProgressSyncOperationHandler handler = new InspectionProgressSyncOperationHandler(
                inspectionService,
                new ObjectMapper(),
                clock);
        meterRegistry = new SimpleMeterRegistry();
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
                referenceDataSyncDeltaService,
                new SyncMetricsRecorder(meterRegistry));

        Logger logger = (Logger) LoggerFactory.getLogger(MobileSyncService.class);
        logAppender = new ListAppender<>();
        logAppender.start();
        logger.addAppender(logAppender);

        User fieldUser = user();
        lenient().when(authorizationService.requireMobileUser(FIELD_USER_ID)).thenReturn(fieldUser);
        lenient().when(inspectionSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(workOrderSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(dashboardSyncDeltaService.buildSnapshot(any(User.class), any()))
                .thenReturn(new com.infratrack.mobile.sync.dto.SyncDashboardDeltaResponse());
        lenient().when(assetSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
        lenient().when(referenceDataSyncDeltaService.buildSnapshot(any()))
                .thenReturn(new com.infratrack.mobile.sync.dto.SyncReferenceDataDeltaResponse());
    }

    @AfterEach
    void tearDown() {
        Logger logger = (Logger) LoggerFactory.getLogger(MobileSyncService.class);
        logger.detachAppender(logAppender);
    }

    @Test
    void sync_exceedsBatchLimit_throwsBeforeProcessing() {
        SyncRequest request = validRequest();
        request.setPendingOperations(IntStream.range(0, 101)
                .mapToObj(i -> progressOperation("op-" + i))
                .toList());

        assertThatThrownBy(() -> mobileSyncService.sync(FIELD_USER_ID, request))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage(SyncLimits.BATCH_LIMIT_MESSAGE);

        verify(inspectionSyncDeltaService, never()).buildDeltaRecords(any(User.class), any(), any());
        verify(workOrderSyncDeltaService, never()).buildDeltaRecords(any(User.class), any(), any());
        verify(dashboardSyncDeltaService, never()).buildSnapshot(any(User.class), any());
        verify(assetSyncDeltaService, never()).buildDeltaRecords(any(User.class), any(), any());
        verify(referenceDataSyncDeltaService, never()).buildSnapshot(any());
        assertThat(meterRegistry.get("mobile.sync.requests").counter().count()).isEqualTo(0.0);
    }

    @Test
    void sync_oversizedPayload_rejectsOperationOnly() {
        when(inspectionService.saveInspectionProgress(eq(123L), any(), eq(FIELD_USER_ID)))
                .thenReturn(new InspectionResponse());

        PendingOperationRequest valid = progressOperation("op-valid");
        PendingOperationRequest oversized = progressOperation("op-large");
        oversized.setPayload(oversizedPayload());

        SyncRequest request = validRequest();
        request.setPendingOperations(List.of(valid, oversized));

        SyncResponse response = mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(response.getOperations()).hasSize(2);
        assertThat(response.getOperations().get(0).getStatus()).isEqualTo(SyncOperationStatus.ACCEPTED);
        assertThat(response.getOperations().get(1).getStatus()).isEqualTo(SyncOperationStatus.REJECTED);
        assertThat(response.getOperations().get(1).getMessage()).isEqualTo(SyncLimits.PAYLOAD_SIZE_MESSAGE);
        verify(inspectionService).saveInspectionProgress(eq(123L), any(), eq(FIELD_USER_ID));
    }

    @Test
    void sync_recordsMetricsAndStructuredLog() {
        when(inspectionService.saveInspectionProgress(eq(123L), any(), eq(FIELD_USER_ID)))
                .thenReturn(new InspectionResponse());

        when(inspectionSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
        when(workOrderSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());
        when(dashboardSyncDeltaService.buildSnapshot(any(User.class), any()))
                .thenReturn(new com.infratrack.mobile.sync.dto.SyncDashboardDeltaResponse());
        when(assetSyncDeltaService.buildDeltaRecords(any(User.class), any(), any()))
                .thenReturn(List.of());

        SyncRequest request = validRequest();
        request.setPendingOperations(List.of(progressOperation("op-1")));

        mobileSyncService.sync(FIELD_USER_ID, request);

        assertThat(meterRegistry.get("mobile.sync.requests").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.operations.accepted").counter().count()).isEqualTo(1.0);
        assertThat(meterRegistry.get("mobile.sync.duration").timer().count()).isEqualTo(1);

        assertThat(logAppender.list)
                .anyMatch(event -> event.getFormattedMessage().startsWith("Sync completed")
                        && event.getFormattedMessage().contains("userId=20")
                        && event.getFormattedMessage().contains("protocolVersion=1")
                        && event.getFormattedMessage().contains("operationCount=1")
                        && event.getFormattedMessage().contains("accepted=1")
                        && event.getFormattedMessage().contains("duplicateOperations=0")
                        && event.getFormattedMessage().contains("deltaWorkOrderCount=0")
                        && event.getFormattedMessage().contains("requiresFullSync=false"));
    }

    private static String oversizedPayload() {
        int bytesNeeded = SyncLimits.MAX_OPERATION_PAYLOAD_BYTES + 1;
        byte[] bytes = new byte[bytesNeeded];
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private SyncRequest validRequest() {
        SyncRequest request = new SyncRequest();
        request.setClientId("android-install-uuid");
        request.setClientVersion("1");
        return request;
    }

    private PendingOperationRequest progressOperation(String operationId) {
        PendingOperationRequest pending = new PendingOperationRequest();
        pending.setOperationId(operationId);
        pending.setEntityType("INSPECTION");
        pending.setEntityId(123L);
        pending.setOperationType("SAVE_INSPECTION_PROGRESS");
        pending.setPayload("{\"answers\":[]}");
        return pending;
    }

    private User user() {
        User user = new User();
        user.setId(FIELD_USER_ID);
        user.setEmail("field@test.com");
        user.setRole(UserRole.FIELD_EMPLOYEE);
        return user;
    }
}
