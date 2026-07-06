package com.infratrack.mobile.sync;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

interface ProcessedSyncOperationRepository extends JpaRepository<ProcessedSyncOperation, String> {

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ProcessedSyncOperation p WHERE p.processedAt < :cutoff")
    int deleteByProcessedAtBefore(@Param("cutoff") Instant cutoff);
}
