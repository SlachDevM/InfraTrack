package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncConflictResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
class NoOpSyncConflictResolver implements SyncConflictResolver {

    @Override
    public List<SyncConflictResponse> resolve(Long userId, List<PendingOperationRequest> pendingOperations) {
        return Collections.emptyList();
    }
}
