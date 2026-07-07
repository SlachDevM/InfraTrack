package com.infratrack.mobile.sync;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncInspectionDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;
import com.infratrack.mobile.sync.dto.SyncWorkOrderDeltaResponse;
import com.infratrack.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * Mobile synchronization service (M5.2+). Accepts sync requests, processes supported pending
 * operations independently, builds download delta, and returns per-operation outcomes.
 */
@Service
public class MobileSyncService {

    private static final Logger log = LoggerFactory.getLogger(MobileSyncService.class);

    private final MobileAuthorizationService authorizationService;
    private final Clock clock;
    private final ObjectMapper objectMapper;
    private final SyncTokenService syncTokenService;
    private final SyncOperationProcessor syncOperationProcessor;
    private final InspectionSyncDeltaService inspectionSyncDeltaService;
    private final WorkOrderSyncDeltaService workOrderSyncDeltaService;
    private final DashboardSyncDeltaService dashboardSyncDeltaService;
    private final AssetSyncDeltaService assetSyncDeltaService;
    private final ReferenceDataSyncDeltaService referenceDataSyncDeltaService;
    private final SyncMetricsRecorder syncMetricsRecorder;

    public MobileSyncService(
            MobileAuthorizationService authorizationService,
            Clock clock,
            ObjectMapper objectMapper,
            SyncTokenService syncTokenService,
            SyncOperationProcessor syncOperationProcessor,
            InspectionSyncDeltaService inspectionSyncDeltaService,
            WorkOrderSyncDeltaService workOrderSyncDeltaService,
            DashboardSyncDeltaService dashboardSyncDeltaService,
            AssetSyncDeltaService assetSyncDeltaService,
            ReferenceDataSyncDeltaService referenceDataSyncDeltaService,
            SyncMetricsRecorder syncMetricsRecorder) {
        this.authorizationService = authorizationService;
        this.clock = clock;
        this.objectMapper = objectMapper;
        this.syncTokenService = syncTokenService;
        this.syncOperationProcessor = syncOperationProcessor;
        this.inspectionSyncDeltaService = inspectionSyncDeltaService;
        this.workOrderSyncDeltaService = workOrderSyncDeltaService;
        this.dashboardSyncDeltaService = dashboardSyncDeltaService;
        this.assetSyncDeltaService = assetSyncDeltaService;
        this.referenceDataSyncDeltaService = referenceDataSyncDeltaService;
        this.syncMetricsRecorder = syncMetricsRecorder;
    }

    public SyncResponse sync(Long userId, SyncRequest request) {
        User user = authorizationService.requireMobileUser(userId);
        validateBatchSize(request);

        SyncDiagnostics diagnostics = SyncDiagnostics.start();
        diagnostics.recordBatchSize(pendingOperationCount(request));
        diagnostics.recordProtocolVersion(SyncProtocolVersion.CURRENT);
        diagnostics.recordInvalidToken(hasInvalidSyncToken(request.getSyncToken()));

        SyncOperationBatchResult operationBatch =
                syncOperationProcessor.process(userId, request.getPendingOperations());
        diagnostics.recordDuplicateOperations(operationBatch.duplicateOperations());

        Instant watermark = clock.instant();
        Long updatedUntilMillis = watermark.toEpochMilli();

        List<SyncWarningResponse> warnings = new ArrayList<>();
        Long updatedSinceMillis =
                SyncDeltaTokenSupport.resolveUpdatedSinceMillis(request.getSyncToken(), warnings);

        SyncDeltaResponse delta = SyncDeltaResponse.empty();
        List<SyncInspectionDeltaResponse> inspectionDeltas = timed(
                diagnostics,
                SyncDeltaSections.INSPECTIONS,
                () -> inspectionSyncDeltaService.buildDeltaRecords(
                        user, updatedSinceMillis, updatedUntilMillis));
        List<SyncWorkOrderDeltaResponse> workOrderDeltas = timed(
                diagnostics,
                SyncDeltaSections.WORK_ORDERS,
                () -> workOrderSyncDeltaService.buildDeltaRecords(
                        user, updatedSinceMillis, updatedUntilMillis));
        delta.setInspections(inspectionDeltas);
        delta.setWorkOrders(workOrderDeltas);
        delta.setDashboard(timed(
                diagnostics,
                SyncDeltaSections.DASHBOARD,
                () -> dashboardSyncDeltaService.buildSnapshot(user, watermark)));
        delta.setAssets(timed(
                diagnostics,
                SyncDeltaSections.ASSETS,
                () -> assetSyncDeltaService.buildDeltaRecords(user, inspectionDeltas, workOrderDeltas)));
        delta.setReferenceData(timed(
                diagnostics,
                SyncDeltaSections.REFERENCE_DATA,
                () -> referenceDataSyncDeltaService.buildSnapshot(watermark)));

        SyncResponse response = new SyncResponse();
        response.setProtocolVersion(SyncProtocolVersion.CURRENT);
        response.setServerTime(watermark);
        response.setOperations(operationBatch.operations());
        response.setConflicts(operationBatch.conflicts());
        response.setDelta(delta);
        response.setWarnings(warnings.isEmpty() ? Collections.emptyList() : List.copyOf(warnings));
        response.setNextSyncToken(
                syncTokenService.resolveNextSyncToken(userId, request.getSyncToken(), watermark));

        boolean requiresFullSync = requiresFullSync(warnings);
        response.setRequiresFullSync(requiresFullSync);
        diagnostics.recordRequiresFullSync(requiresFullSync);

        diagnostics.recordOperations(response.getOperations());
        diagnostics.recordDeltaInspections(response.getDelta().getInspections().size());
        diagnostics.recordDeltaWorkOrders(response.getDelta().getWorkOrders().size());
        diagnostics.recordDeltaAssets(response.getDelta().getAssets().size());
        diagnostics.recordDashboardIncluded(response.getDelta().getDashboard() != null);
        diagnostics.recordReferenceDataIncluded(response.getDelta().getReferenceData() != null);
        recordResponseSize(diagnostics, response);
        syncMetricsRecorder.record(diagnostics);
        log.info(
                "Sync completed userId={} protocolVersion={} operationCount={} accepted={} rejected={} "
                        + "conflicts={} ignored={} duplicateOperations={} deltaInspectionCount={} "
                        + "deltaWorkOrderCount={} deltaAssetCount={} referenceDataIncluded={} "
                        + "dashboardIncluded={} durationMs={} requiresFullSync={}",
                userId,
                diagnostics.protocolVersion(),
                diagnostics.processed(),
                diagnostics.accepted(),
                diagnostics.rejected(),
                diagnostics.conflicts(),
                diagnostics.ignored(),
                diagnostics.duplicateOperations(),
                diagnostics.deltaInspections(),
                diagnostics.deltaWorkOrders(),
                diagnostics.deltaAssets(),
                diagnostics.referenceDataIncluded(),
                diagnostics.dashboardIncluded(),
                diagnostics.elapsedMillis(),
                diagnostics.requiresFullSync());

        return response;
    }

    private void validateBatchSize(SyncRequest request) {
        List<?> pendingOperations = request.getPendingOperations();
        if (pendingOperations != null && pendingOperations.size() > SyncLimits.MAX_PENDING_OPERATIONS) {
            throw new BusinessValidationException(SyncLimits.BATCH_LIMIT_MESSAGE);
        }
    }

    private static int pendingOperationCount(SyncRequest request) {
        List<?> pendingOperations = request.getPendingOperations();
        return pendingOperations == null ? 0 : pendingOperations.size();
    }

    private static boolean hasInvalidSyncToken(String syncToken) {
        return syncToken != null
                && !syncToken.isBlank()
                && SyncToken.tryFromOpaqueValue(syncToken).isEmpty();
    }

    private static boolean requiresFullSync(List<SyncWarningResponse> warnings) {
        return warnings.stream()
                .anyMatch(warning -> warning.getCode() == SyncWarningCode.FULL_SYNC_REQUIRED);
    }

    private static <T> T timed(SyncDiagnostics diagnostics, String section, Supplier<T> supplier) {
        long startNanos = System.nanoTime();
        try {
            return supplier.get();
        } finally {
            diagnostics.recordSectionDuration(section, (System.nanoTime() - startNanos) / 1_000_000L);
        }
    }

    private void recordResponseSize(SyncDiagnostics diagnostics, SyncResponse response) {
        try {
            diagnostics.recordResponseSizeBytes(objectMapper.writeValueAsBytes(response).length);
        } catch (JsonProcessingException ex) {
            log.debug("Could not measure sync response size", ex);
        }
    }
}
