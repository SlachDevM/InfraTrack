package com.infratrack.mobile.sync;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Clock;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class SyncTestIdempotencySupport {

    private SyncTestIdempotencySupport() {
    }

    static ProcessedSyncOperationService passthroughService(Clock clock) {
        ProcessedSyncOperationRepository repository = mock(ProcessedSyncOperationRepository.class);
        lenient().when(repository.findById(any())).thenReturn(Optional.empty());
        lenient().when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        return new ProcessedSyncOperationService(
                repository,
                clock,
                new SyncMetricsRecorder(new SimpleMeterRegistry()));
    }
}
