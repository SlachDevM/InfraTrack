package com.infratrack.mobile.sync;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

import java.time.Clock;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class SyncTestIdempotencySupport {

    private SyncTestIdempotencySupport() {
    }

    static ProcessedSyncOperationService passthroughService(Clock clock) {
        ProcessedSyncOperationRepository repository = mock(ProcessedSyncOperationRepository.class);
        Map<String, ProcessedSyncOperation> store = new HashMap<>();
        lenient().when(repository.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(store.get(invocation.getArgument(0))));
        lenient().when(repository.save(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });
        lenient().when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });
        lenient().doAnswer(invocation -> {
            store.remove(invocation.getArgument(0));
            return null;
        }).when(repository).deleteById(any());
        return new ProcessedSyncOperationService(
                repository,
                clock,
                new SyncMetricsRecorder(new SimpleMeterRegistry()));
    }

    static ProcessedSyncOperationRepository concurrentRepository(Map<String, ProcessedSyncOperation> store) {
        ProcessedSyncOperationRepository repository = mock(ProcessedSyncOperationRepository.class);
        AtomicBoolean insertInProgress = new AtomicBoolean(false);
        when(repository.findById(any())).thenAnswer(invocation ->
                Optional.ofNullable(store.get(invocation.getArgument(0))));
        when(repository.saveAndFlush(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            if (!insertInProgress.compareAndSet(false, true)) {
                if (store.containsKey(saved.getOperationId())) {
                    throw new org.springframework.dao.DataIntegrityViolationException("duplicate operation");
                }
            }
            try {
                if (store.putIfAbsent(saved.getOperationId(), saved) != null) {
                    throw new org.springframework.dao.DataIntegrityViolationException("duplicate operation");
                }
                return saved;
            } finally {
                insertInProgress.set(false);
            }
        });
        when(repository.save(any())).thenAnswer(invocation -> {
            ProcessedSyncOperation saved = invocation.getArgument(0);
            store.put(saved.getOperationId(), saved);
            return saved;
        });
        lenient().doAnswer(invocation -> {
            store.remove(invocation.getArgument(0));
            return null;
        }).when(repository).deleteById(any());
        return repository;
    }
}
