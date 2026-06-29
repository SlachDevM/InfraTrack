package com.infratrack.preventivemaintenance;

import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationOutcome;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationResultResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResultResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Controlled preventive scheduler (V2 Phase B Sprint B5).
 * Generates execution candidates only; never approves or creates workflow records.
 */
@Service
public class PreventiveSchedulerService {

    private final PreventiveExecutionCandidateService candidateService;
    private final PreventiveSchedulerRunRepository runRepository;

    public PreventiveSchedulerService(
            PreventiveExecutionCandidateService candidateService,
            PreventiveSchedulerRunRepository runRepository) {
        this.candidateService = candidateService;
        this.runRepository = runRepository;
    }

    @Transactional
    public PreventiveSchedulerRunResultResponse runManual(Long userId, Long departmentId) {
        return executeRun(PreventiveSchedulerTriggeredBy.MANUAL, userId, departmentId);
    }

    @Transactional
    public PreventiveSchedulerRunResultResponse runScheduled() {
        return executeRun(PreventiveSchedulerTriggeredBy.SCHEDULED, null, null);
    }

    @Transactional(readOnly = true)
    public Page<PreventiveSchedulerRunResponse> listRuns(Pageable pageable) {
        return runRepository.findAllByOrderByStartedAtDesc(pageable)
                .map(PreventiveSchedulerRunResponse::from);
    }

    @Transactional(readOnly = true)
    public PreventiveSchedulerRunResponse getRun(Long runId) {
        return PreventiveSchedulerRunResponse.from(findRun(runId));
    }

    private PreventiveSchedulerRunResultResponse executeRun(
            PreventiveSchedulerTriggeredBy triggeredBy,
            Long userId,
            Long departmentId) {
        long startedAt = System.currentTimeMillis();
        PreventiveSchedulerRun run = new PreventiveSchedulerRun(startedAt, triggeredBy, userId);

        try {
            List<ExecutionCandidateGenerationResultResponse> results =
                    candidateService.generateCandidatesForActivePlans(userId, departmentId);
            RunSummary summary = summarize(results);
            PreventiveSchedulerRunStatus status = resolveStatus(summary);
            run.complete(
                    System.currentTimeMillis(),
                    status,
                    summary.plansEvaluated(),
                    summary.created(),
                    summary.skipped(),
                    summary.notEligible(),
                    summary.errorMessage());
            PreventiveSchedulerRun saved = runRepository.save(run);
            return toResult(saved);
        } catch (Exception ex) {
            run.complete(
                    System.currentTimeMillis(),
                    PreventiveSchedulerRunStatus.FAILED,
                    0,
                    0,
                    0,
                    0,
                    ex.getMessage());
            PreventiveSchedulerRun saved = runRepository.save(run);
            return toResult(saved);
        }
    }

    private static RunSummary summarize(List<ExecutionCandidateGenerationResultResponse> results) {
        int created = 0;
        int skipped = 0;
        int notEligible = 0;
        int failed = 0;
        String firstFailure = null;

        for (ExecutionCandidateGenerationResultResponse result : results) {
            switch (result.getOutcome()) {
                case CREATED -> created++;
                case SKIPPED_DUPLICATE -> skipped++;
                case NOT_ELIGIBLE -> notEligible++;
                case FAILED -> {
                    failed++;
                    if (firstFailure == null) {
                        firstFailure = result.getFailureMessage();
                    }
                }
            }
        }

        return new RunSummary(results.size(), created, skipped, notEligible, failed, firstFailure);
    }

    private static PreventiveSchedulerRunStatus resolveStatus(RunSummary summary) {
        if (summary.plansEvaluated() == 0 && summary.failed() > 0) {
            return PreventiveSchedulerRunStatus.FAILED;
        }
        if (summary.failed() > 0) {
            return PreventiveSchedulerRunStatus.PARTIAL;
        }
        return PreventiveSchedulerRunStatus.SUCCESS;
    }

    private static PreventiveSchedulerRunResultResponse toResult(PreventiveSchedulerRun run) {
        return new PreventiveSchedulerRunResultResponse(
                run.getId(),
                run.getStatus(),
                run.getPlansEvaluatedCount(),
                run.getCandidatesCreatedCount(),
                run.getCandidatesSkippedDuplicateCount(),
                run.getPlansNotEligibleCount(),
                run.getDurationMs());
    }

    private PreventiveSchedulerRun findRun(Long runId) {
        return runRepository.findById(runId)
                .orElseThrow(() -> new NotFoundException("Preventive scheduler run not found"));
    }

    private record RunSummary(
            int plansEvaluated,
            int created,
            int skipped,
            int notEligible,
            int failed,
            String errorMessage) {
    }
}
