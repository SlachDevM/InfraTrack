package com.infratrack.preventivemaintenance;

import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResultResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled preventive candidate generation (disabled by default via configuration).
 */
@Component
public class PreventiveSchedulerJob {

    private static final Logger log = LoggerFactory.getLogger(PreventiveSchedulerJob.class);

    private final PreventiveSchedulerProperties properties;
    private final PreventiveSchedulerService schedulerService;

    public PreventiveSchedulerJob(
            PreventiveSchedulerProperties properties,
            PreventiveSchedulerService schedulerService) {
        this.properties = properties;
        this.schedulerService = schedulerService;
    }

    @Scheduled(cron = "${app.preventive.scheduler.cron:0 0 6 * * *}")
    public void runScheduledGeneration() {
        if (!properties.isEnabled()) {
            return;
        }

        log.info("Starting scheduled preventive candidate generation");
        PreventiveSchedulerRunResultResponse result = schedulerService.runScheduled();
        log.info(
                "Scheduled preventive candidate generation finished: runId={}, status={}, created={}",
                result.getRunId(),
                result.getStatus(),
                result.getCandidatesCreatedCount());
    }
}
