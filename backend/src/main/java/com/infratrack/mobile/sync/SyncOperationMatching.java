package com.infratrack.mobile.sync;

import com.infratrack.mobile.sync.dto.PendingOperationRequest;

/**
 * Exact (entityType, operationType) matching for mobile sync operation routing.
 */
final class SyncOperationMatching {

    private SyncOperationMatching() {
    }

    static boolean matches(PendingOperationRequest operation, String entityType, String operationType) {
        if (operation == null || entityType == null || operationType == null) {
            return false;
        }
        String requestEntityType = operation.getEntityType();
        String requestOperationType = operation.getOperationType();
        if (requestEntityType == null || requestOperationType == null) {
            return false;
        }
        return entityType.equals(requestEntityType) && operationType.equals(requestOperationType);
    }
}
