package com.infratrack.mobile.sync;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import com.infratrack.mobile.sync.dto.SyncWarningCode;
import com.infratrack.mobile.sync.dto.SyncWarningResponse;
import com.infratrack.user.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Clock;
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
    private final SyncMetricsRecorder syncMetricsRecorder;

    public MobileSyncService(
            MobileAuthorizationService authorizationService,
            Clock clock,
            SyncTokenService syncTokenService,
            SyncOperationProcessor syncOperationProcessor,
            InspectionSyncDeltaService inspectionSyncDeltaService,
            SyncMetricsRecorder syncMetricsRecorder) {
        this.authorizationService = authorizationService;
        this.clock = clock;
        this.syncTokenService = syncTokenService;
        this.syncOperationProcessor = syncOperationProcessor;
        this.inspectionSyncDeltaService = inspectionSyncDeltaService;
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

        SyncResponse response = new SyncResponse();
        response.setProtocolVersion(SyncProtocolVersion.CURRENT);
        response.setServerTime(clock.instant());
        response.setOperations(operationBatch.operations());
        response.setConflicts(operationBatch.conflicts());

        InspectionSyncDeltaService.SyncDeltaBuildResult deltaResult =
                inspectionSyncDeltaService.build(user, request.getSyncToken());
        response.setDelta(deltaResult.delta());
        response.setWarnings(deltaResult.warnings().isEmpty()
                ? Collections.emptyList()
                : List.copyOf(deltaResult.warnings()));
        response.setNextSyncToken(syncTokenService.resolveNextSyncToken(userId, request.getSyncToken()));

        boolean requiresFullSync = requiresFullSync(deltaResult.warnings());
        response.setRequiresFullSync(false);
        diagnostics.recordRequiresFullSync(requiresFullSync);

        diagnostics.recordOperations(response.getOperations());
        diagnostics.recordDeltaInspections(response.getDelta().getInspections().size());
        syncMetricsRecorder.record(diagnostics);
        log.info(
                "Sync completed userId={} protocolVersion={} operationCount={} accepted={} rejected={} "
                        + "conflicts={} ignored={} duplicateOperations={} deltaInspectionCount={} durationMs={} "
                        + "requiresFullSync={}",
                userId,
                diagnostics.protocolVersion(),
                diagnostics.processed(),
                diagnostics.accepted(),
                diagnostics.rejected(),
                diagnostics.conflicts(),
                diagnostics.ignored(),
                diagnostics.duplicateOperations(),
                diagnostics.deltaInspections(),
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
