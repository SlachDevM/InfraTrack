package com.infratrack.mobile.sync;

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

/**
 * Mobile synchronization service (M5.2+). Accepts sync requests, processes supported pending
 * operations independently, builds download delta, and returns per-operation outcomes.
 */
@Service
public class MobileSyncService {

    private static final Logger log = LoggerFactory.getLogger(MobileSyncService.class);

    private final MobileAuthorizationService authorizationService;
    private final Clock clock;
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
        List<SyncInspectionDeltaResponse> inspectionDeltas = inspectionSyncDeltaService.buildDeltaRecords(
                user, updatedSinceMillis, updatedUntilMillis);
        List<SyncWorkOrderDeltaResponse> workOrderDeltas = workOrderSyncDeltaService.buildDeltaRecords(
                user, updatedSinceMillis, updatedUntilMillis);
        delta.setInspections(inspectionDeltas);
        delta.setWorkOrders(workOrderDeltas);
        delta.setDashboard(dashboardSyncDeltaService.buildSnapshot(user, watermark));
        delta.setAssets(assetSyncDeltaService.buildDeltaRecords(user, inspectionDeltas, workOrderDeltas));
        delta.setReferenceData(referenceDataSyncDeltaService.buildSnapshot(watermark));

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
        syncMetricsRecorder.record(diagnostics);
        log.info(
                "Sync completed userId={} protocolVersion={} operationCount={} accepted={} rejected={} "
                        + "conflicts={} ignored={} duplicateOperations={} deltaInspectionCount={} "
                        + "deltaWorkOrderCount={} durationMs={} requiresFullSync={}",
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
}
