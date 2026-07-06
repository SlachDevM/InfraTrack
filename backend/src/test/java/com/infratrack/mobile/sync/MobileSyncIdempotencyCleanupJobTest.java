package com.infratrack.mobile.sync;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MobileSyncIdempotencyCleanupJobTest {

    @Test
    void purgeExpiredRecords_delegatesToService() {
        ProcessedSyncOperationService service = mock(ProcessedSyncOperationService.class);
        MobileSyncIdempotencyProperties properties = new MobileSyncIdempotencyProperties();
        properties.setRetentionDays(90);
        Clock clock = Clock.fixed(Instant.parse("2026-07-05T03:00:00Z"), ZoneOffset.UTC);
        when(service.purgeExpired(any())).thenReturn(2);

        MobileSyncIdempotencyCleanupJob job =
                new MobileSyncIdempotencyCleanupJob(service, properties, clock);
        job.purgeExpiredRecords();

        Instant expectedCutoff = clock.instant().minus(90, ChronoUnit.DAYS);
        verify(service).purgeExpired(expectedCutoff);
    }
}
