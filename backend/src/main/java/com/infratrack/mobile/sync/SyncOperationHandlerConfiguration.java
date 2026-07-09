package com.infratrack.mobile.sync;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registers sync operation handlers in deterministic routing order and exposes the list
 * for {@link DefaultSyncOperationProcessor} via the {@code syncOperationHandlers} bean name.
 */
@Configuration
class SyncOperationHandlerConfiguration {

    @Bean
    List<SyncOperationHandler> syncOperationHandlers(
            WorkOrderProgressSyncOperationHandler workOrderHandler,
            InspectionProgressSyncOperationHandler inspectionHandler) {
        return List.of(workOrderHandler, inspectionHandler);
    }
}
