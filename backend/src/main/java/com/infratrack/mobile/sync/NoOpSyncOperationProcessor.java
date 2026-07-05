package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;
import com.infratrack.mobile.sync.dto.SyncOperationResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
class NoOpSyncOperationProcessor implements SyncOperationProcessor {

    @Override
    public List<SyncOperationResponse> process(Long userId, List<PendingOperationRequest> pendingOperations) {
        return Collections.emptyList();
    }
}
