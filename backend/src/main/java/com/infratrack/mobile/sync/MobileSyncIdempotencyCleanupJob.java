package com.infratrack.mobile.sync;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * Purges expired mobile sync idempotency records (DT-OFFLINE-1).
 */
@Component
class MobileSyncIdempotencyCleanupJob {

    private static final Logger log = LoggerFactory.getLogger(MobileSyncIdempotencyCleanupJob.class);

    private final ProcessedSyncOperationService processedSyncOperationService;
    private final MobileSyncIdempotencyProperties properties;
    private final Clock clock;

    MobileSyncIdempotencyCleanupJob(
            ProcessedSyncOperationService processedSyncOperationService,
            MobileSyncIdempotencyProperties properties,
            Clock clock) {
        this.processedSyncOperationService = processedSyncOperationService;
        this.properties = properties;
        this.clock = clock;
    }

    @Scheduled(cron = "${mobile.sync.idempotency.cleanup-cron:0 0 3 * * *}")
    @Transactional
    void purgeExpiredRecords() {
        Instant cutoff = clock.instant().minus(properties.getRetentionDays(), ChronoUnit.DAYS);
        int deleted = processedSyncOperationService.purgeExpired(cutoff);
        if (deleted > 0) {
            log.info("Purged expired mobile sync idempotency records count={} cutoff={}", deleted, cutoff);
        }
    }
}
