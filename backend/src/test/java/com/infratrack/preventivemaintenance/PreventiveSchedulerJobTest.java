package com.infratrack.preventivemaintenance;

import com.infratrack.preventivemaintenance.dto.PreventiveSchedulerRunResultResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveSchedulerJobTest {

    @Mock
    private PreventiveSchedulerProperties properties;

    @Mock
    private PreventiveSchedulerService schedulerService;

    @InjectMocks
    private PreventiveSchedulerJob schedulerJob;

    @Test
    void runScheduledGeneration_shouldDoNothingWhenDisabled() {
        when(properties.isEnabled()).thenReturn(false);

        schedulerJob.runScheduledGeneration();

        verify(schedulerService, never()).runScheduled();
    }

    @Test
    void runScheduledGeneration_shouldRunWhenEnabled() {
        when(properties.isEnabled()).thenReturn(true);
        when(schedulerService.runScheduled()).thenReturn(
                new PreventiveSchedulerRunResultResponse(1L, PreventiveSchedulerRunStatus.SUCCESS, 0, 0, 0, 0, 10L));

        schedulerJob.runScheduledGeneration();

        verify(schedulerService).runScheduled();
    }
}
