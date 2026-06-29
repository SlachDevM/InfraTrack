package com.infratrack.preventivemaintenance;

import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationOutcome;
import com.infratrack.preventivemaintenance.dto.ExecutionCandidateGenerationResultResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResponse;
import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResultResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveSchedulerServiceTest {

    @Mock
    private PreventiveExecutionCandidateService candidateService;

    @Mock
    private PreventiveSchedulerRunRepository runRepository;

    @InjectMocks
    private PreventiveSchedulerService schedulerService;

    @Test
    void runManual_shouldPersistRunReportWithCounts() {
        when(candidateService.generateCandidatesForActivePlans(30L, null)).thenReturn(List.of(
                ExecutionCandidateGenerationResultResponse.created(100L, "PUMP_MONTHLY", null),
                ExecutionCandidateGenerationResultResponse.skipped(101L, "PUMP_WEEKLY", null),
                ExecutionCandidateGenerationResultResponse.notEligible(102L, "DRAFT_PLAN")));
        when(runRepository.save(any(PreventiveSchedulerRun.class))).thenAnswer(invocation -> {
            PreventiveSchedulerRun run = invocation.getArgument(0);
            run.setId(900L);
            return run;
        });

        PreventiveSchedulerRunResultResponse result = schedulerService.runManual(30L, null);

        assertThat(result.getRunId()).isEqualTo(900L);
        assertThat(result.getStatus()).isEqualTo(PreventiveSchedulerRunStatus.SUCCESS);
        assertThat(result.getPlansEvaluatedCount()).isEqualTo(3);
        assertThat(result.getCandidatesCreatedCount()).isEqualTo(1);
        assertThat(result.getCandidatesSkippedDuplicateCount()).isEqualTo(1);
        assertThat(result.getPlansNotEligibleCount()).isEqualTo(1);

        ArgumentCaptor<PreventiveSchedulerRun> captor = ArgumentCaptor.forClass(PreventiveSchedulerRun.class);
        verify(runRepository).save(captor.capture());
        assertThat(captor.getValue().getTriggeredBy()).isEqualTo(PreventiveSchedulerTriggeredBy.MANUAL);
        assertThat(captor.getValue().getTriggeredByUserId()).isEqualTo(30L);
    }

    @Test
    void runManual_shouldMarkPartialWhenPlanFails() {
        when(candidateService.generateCandidatesForActivePlans(30L, 10L)).thenReturn(List.of(
                ExecutionCandidateGenerationResultResponse.created(100L, "PUMP_MONTHLY", null),
                ExecutionCandidateGenerationResultResponse.failed(101L, "PUMP_WEEKLY", "Evaluation failed")));
        when(runRepository.save(any(PreventiveSchedulerRun.class))).thenAnswer(invocation -> {
            PreventiveSchedulerRun run = invocation.getArgument(0);
            run.setId(901L);
            return run;
        });

        PreventiveSchedulerRunResultResponse result = schedulerService.runManual(30L, 10L);

        assertThat(result.getStatus()).isEqualTo(PreventiveSchedulerRunStatus.PARTIAL);
        verify(candidateService).generateCandidatesForActivePlans(30L, 10L);
    }

    @Test
    void runScheduled_shouldUseScheduledTriggerWithoutUserId() {
        when(candidateService.generateCandidatesForActivePlans(null, null)).thenReturn(List.of());
        when(runRepository.save(any(PreventiveSchedulerRun.class))).thenAnswer(invocation -> {
            PreventiveSchedulerRun run = invocation.getArgument(0);
            run.setId(902L);
            return run;
        });

        schedulerService.runScheduled();

        ArgumentCaptor<PreventiveSchedulerRun> captor = ArgumentCaptor.forClass(PreventiveSchedulerRun.class);
        verify(runRepository).save(captor.capture());
        assertThat(captor.getValue().getTriggeredBy()).isEqualTo(PreventiveSchedulerTriggeredBy.SCHEDULED);
        assertThat(captor.getValue().getTriggeredByUserId()).isNull();
        verify(candidateService).generateCandidatesForActivePlans(null, null);
    }

    @Test
    void listRuns_shouldReturnPagedHistory() {
        PreventiveSchedulerRun run = new PreventiveSchedulerRun(1000L, PreventiveSchedulerTriggeredBy.MANUAL, 30L);
        run.setId(1L);
        run.complete(2000L, PreventiveSchedulerRunStatus.SUCCESS, 1, 1, 0, 0, null);
        when(runRepository.findAllByOrderByStartedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(run)));

        Page<PreventiveSchedulerRunResponse> page = schedulerService.listRuns(Pageable.unpaged());

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().get(0).getStatus()).isEqualTo(PreventiveSchedulerRunStatus.SUCCESS);
    }

    @Test
    void getRun_shouldRejectMissingRun() {
        when(runRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> schedulerService.getRun(999L))
                .isInstanceOf(NotFoundException.class);
    }
}
