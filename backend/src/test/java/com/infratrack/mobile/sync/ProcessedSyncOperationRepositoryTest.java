package com.infratrack.mobile.sync;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.util.ReflectionTestUtils.getField;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class ProcessedSyncOperationRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"));

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private ProcessedSyncOperationRepository repository;

    @Autowired
    private List<SyncOperationHandler> syncOperationHandlers;

    @Autowired
    private DefaultSyncOperationProcessor syncOperationProcessor;

    @Test
    void contextWiresBothProgressSyncHandlersWithWorkOrderListedFirst() {
        assertThat(syncOperationHandlers).hasSize(2);
        assertThat(syncOperationHandlers.get(0)).isInstanceOf(WorkOrderProgressSyncOperationHandler.class);
        assertThat(syncOperationHandlers.get(1)).isInstanceOf(InspectionProgressSyncOperationHandler.class);
    }

    @Test
    void defaultSyncOperationProcessor_usesConfiguredHandlerOrder() {
        @SuppressWarnings("unchecked")
        List<SyncOperationHandler> processorHandlers =
                (List<SyncOperationHandler>) getField(syncOperationProcessor, "handlers");

        assertThat(processorHandlers).hasSize(2);
        assertThat(processorHandlers.get(0)).isInstanceOf(WorkOrderProgressSyncOperationHandler.class);
        assertThat(processorHandlers.get(1)).isInstanceOf(InspectionProgressSyncOperationHandler.class);
    }

    @Test
    void saveAndFind_persistsRecord() {
        Instant processedAt = Instant.parse("2026-07-05T10:00:00Z");
        ProcessedSyncOperation record = ProcessedSyncOperation.recorded(
                "op-repo-1",
                20L,
                "INSPECTION",
                123L,
                "SAVE_INSPECTION_PROGRESS",
                1,
                processedAt,
                "ACCEPTED",
                null,
                processedAt,
                null,
                null);

        repository.save(record);

        assertThat(repository.findById("op-repo-1")).isPresent();
        assertThat(repository.findById("op-repo-1").orElseThrow().getUserId()).isEqualTo(20L);
    }

    @Test
    @Transactional
    void deleteByProcessedAtBefore_removesExpiredRecords() {
        Instant old = Instant.parse("2026-01-01T00:00:00Z");
        Instant recent = Instant.parse("2026-07-05T00:00:00Z");
        repository.save(ProcessedSyncOperation.recorded(
                "op-old", 20L, "INSPECTION", 1L, "SAVE_INSPECTION_PROGRESS", 1, old, "ACCEPTED", null, old, null, null));
        repository.save(ProcessedSyncOperation.recorded(
                "op-new", 20L, "INSPECTION", 2L, "SAVE_INSPECTION_PROGRESS", 1, recent, "ACCEPTED", null, recent, null, null));

        int deleted = repository.deleteByProcessedAtBefore(recent.minus(1, ChronoUnit.DAYS));

        assertThat(deleted).isEqualTo(1);
        assertThat(repository.findById("op-old")).isEmpty();
        assertThat(repository.findById("op-new")).isPresent();
    }
}
