package com.infratrack.mobile.sync;

import com.infratrack.mobile.MobileAuthorizationService;
import com.infratrack.mobile.sync.dto.SyncDeltaResponse;
import com.infratrack.mobile.sync.dto.SyncRequest;
import com.infratrack.mobile.sync.dto.SyncResponse;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.Collections;

/**
 * Mobile synchronization protocol foundation (M5.2-BE1).
 * Accepts sync requests and returns an empty response envelope — no domain mutations yet.
 */
@Service
public class MobileSyncService {

    private final MobileAuthorizationService authorizationService;
    private final Clock clock;
    private final SyncTokenService syncTokenService;
    private final SyncOperationProcessor syncOperationProcessor;
    private final SyncConflictResolver syncConflictResolver;

    public MobileSyncService(
            MobileAuthorizationService authorizationService,
            Clock clock,
            SyncTokenService syncTokenService,
            SyncOperationProcessor syncOperationProcessor,
            SyncConflictResolver syncConflictResolver) {
        this.authorizationService = authorizationService;
        this.clock = clock;
        this.syncTokenService = syncTokenService;
        this.syncOperationProcessor = syncOperationProcessor;
        this.syncConflictResolver = syncConflictResolver;
    }

    public SyncResponse sync(Long userId, SyncRequest request) {
        authorizationService.requireMobileUser(userId);

        SyncResponse response = new SyncResponse();
        response.setProtocolVersion(SyncProtocolVersion.CURRENT);
        response.setServerTime(clock.instant());
        response.setNextSyncToken(syncTokenService.resolveNextSyncToken(userId, request.getSyncToken()));
        response.setDelta(SyncDeltaResponse.empty());
        response.setOperations(syncOperationProcessor.process(userId, request.getPendingOperations()));
        response.setConflicts(syncConflictResolver.resolve(userId, request.getPendingOperations()));
        response.setWarnings(Collections.emptyList());
        response.setRequiresFullSync(false);
        return response;
    }
}
