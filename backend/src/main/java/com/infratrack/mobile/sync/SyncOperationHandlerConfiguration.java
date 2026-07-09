package com.infratrack.mobile.sync;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Registers sync operation handlers in deterministic routing order.
 * Work-order progress is listed before inspection progress so
 * {@link DefaultSyncOperationProcessor} never evaluates the inspection handler first.
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
