package com.infratrack.operationsintelligence;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.operationsintelligence.dto.OperationsTrendResponse;
import com.infratrack.operationsintelligence.dto.TrendDataPointResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsTrendServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Australia/Sydney");

    @Mock
    private OperationsTrendAuthorizationService authorizationService;

    @Mock
    private OperationsTrendAggregationRepository aggregationRepository;

    private OperationsTrendService trendService;

    private final OperationsKpiScope globalScope = OperationsKpiScope.global();
    private final OperationsKpiScope departmentScope = OperationsKpiScope.forDepartment(10L);

    private long fromMillis;
    private long toMillis;

    @BeforeEach
    void setUp() {
        trendService = new OperationsTrendService(authorizationService, aggregationRepository, ZONE);
        LocalDate toDate = LocalDate.of(2026, 6, 5);
        LocalDate fromDate = LocalDate.of(2026, 6, 1);
        fromMillis = fromDate.atStartOfDay(ZONE).toInstant().toEpochMilli();
        toMillis = toDate.plusDays(1).atStartOfDay(ZONE).toInstant().toEpochMilli();
    }

    @Test
    void getTrends_shouldReturnGlobalSeriesForAdministrator() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        stubTimestamps(globalScope);

        OperationsTrendResponse response = trendService.getTrends(1L, fromMillis, toMillis, "DAY");

        assertThat(response.getScope().getType()).isEqualTo("GLOBAL");
        assertThat(response.getBucket()).isEqualTo("DAY");
        assertThat(response.getSeries().getInspectionsCompleted()).hasSize(5);
        assertThat(response.getSeries().getIssuesCreated()).hasSize(5);
        verify(aggregationRepository).findCompletedInspectionTimestamps(globalScope, fromMillis, toMillis);
    }

    @Test
    void getTrends_shouldUseDepartmentScopeForManager() {
        when(authorizationService.resolveScope(2L)).thenReturn(departmentScope);
        stubTimestamps(departmentScope);

        OperationsTrendResponse response = trendService.getTrends(2L, fromMillis, toMillis, "DAY");

        assertThat(response.getScope().getType()).isEqualTo("DEPARTMENT");
        assertThat(response.getScope().getDepartmentId()).isEqualTo(10L);
        verify(aggregationRepository).findIssueCreatedTimestamps(departmentScope, fromMillis, toMillis);
    }

    @Test
    void getTrends_shouldRejectFieldEmployee() {
        when(authorizationService.resolveScope(4L))
                .thenThrow(new ForbiddenOperationException("You do not have permission to view operational KPIs."));

        assertThatThrownBy(() -> trendService.getTrends(4L, fromMillis, toMillis, "DAY"))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(aggregationRepository, never()).findCompletedInspectionTimestamps(any(), anyLong(), anyLong());
    }

    @Test
    void getTrends_shouldRejectContractor() {
        when(authorizationService.resolveScope(5L))
                .thenThrow(new ForbiddenOperationException("You do not have permission to view operational KPIs."));

        assertThatThrownBy(() -> trendService.getTrends(5L, fromMillis, toMillis, "DAY"))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getTrends_shouldApplyDefaultRangeWhenParametersOmitted() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findCompletedInspectionTimestamps(any(), anyLong(), anyLong()))
                .thenReturn(List.of());
        when(aggregationRepository.findIssueCreatedTimestamps(any(), anyLong(), anyLong()))
                .thenReturn(List.of());
        when(aggregationRepository.findCompletedWorkOrderTimestamps(any(), anyLong(), anyLong()))
                .thenReturn(List.of());
        when(aggregationRepository.findPreventiveCandidateGeneratedTimestamps(any(), anyLong(), anyLong()))
                .thenReturn(List.of());
        when(aggregationRepository.findAcceptedSuggestedActionTimestamps(any(), anyLong(), anyLong()))
                .thenReturn(List.of());

        OperationsTrendResponse response = trendService.getTrends(1L, null, null, null);

        assertThat(response.getFrom()).isLessThan(response.getTo());
        assertThat(response.getBucket()).isEqualTo("DAY");
        assertThat(response.getSeries().getInspectionsCompleted()).isNotEmpty();
    }

    @Test
    void getTrends_shouldRejectInvalidDateRange() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);

        assertThatThrownBy(() -> trendService.getTrends(1L, toMillis, fromMillis, "DAY"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Trend start time must be before end time.");
    }

    @Test
    void getTrends_shouldRejectRangeLongerThan365Days() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        long start = LocalDate.of(2025, 1, 1).atStartOfDay(ZONE).toInstant().toEpochMilli();
        long end = LocalDate.of(2026, 6, 1).atStartOfDay(ZONE).toInstant().toEpochMilli();

        assertThatThrownBy(() -> trendService.getTrends(1L, start, end, "DAY"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Trend date range cannot exceed 365 days.");
    }

    @Test
    void getTrends_shouldRejectInvalidBucket() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);

        assertThatThrownBy(() -> trendService.getTrends(1L, fromMillis, toMillis, "QUARTER"))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Unsupported trend bucket. Allowed values: DAY, WEEK, MONTH.");
    }

    @Test
    void getTrends_shouldGroupInspectionsByDayAndFillMissingBuckets() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findCompletedInspectionTimestamps(globalScope, fromMillis, toMillis))
                .thenReturn(List.of(
                        LocalDateTime.of(2026, 6, 1, 9, 0),
                        LocalDateTime.of(2026, 6, 1, 15, 0),
                        LocalDateTime.of(2026, 6, 3, 11, 0)));
        stubRemainingTimestamps(globalScope);

        OperationsTrendResponse response = trendService.getTrends(1L, fromMillis, toMillis, "DAY");

        List<TrendDataPointResponse> inspections = response.getSeries().getInspectionsCompleted();
        assertThat(inspections).extracting(TrendDataPointResponse::getPeriod)
                .containsExactly("2026-06-01", "2026-06-02", "2026-06-03", "2026-06-04", "2026-06-05");
        assertThat(inspections).extracting(TrendDataPointResponse::getCount)
                .containsExactly(2L, 0L, 1L, 0L, 0L);
    }

    @Test
    void getTrends_shouldGroupIssuesCreatedByDay() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findCompletedInspectionTimestamps(globalScope, fromMillis, toMillis))
                .thenReturn(List.of());
        when(aggregationRepository.findIssueCreatedTimestamps(globalScope, fromMillis, toMillis))
                .thenReturn(List.of(LocalDateTime.of(2026, 6, 2, 8, 0)));
        stubRemainingTimestampsExceptIssues(globalScope);

        OperationsTrendResponse response = trendService.getTrends(1L, fromMillis, toMillis, "DAY");

        assertThat(response.getSeries().getIssuesCreated())
                .filteredOn(point -> point.getCount() > 0)
                .extracting(TrendDataPointResponse::getPeriod)
                .containsExactly("2026-06-02");
    }

    @Test
    void getTrends_shouldRemainReadOnly() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        stubTimestamps(globalScope);

        trendService.getTrends(1L, fromMillis, toMillis, "DAY");

        verify(aggregationRepository).findCompletedInspectionTimestamps(eq(globalScope), eq(fromMillis), eq(toMillis));
        verify(aggregationRepository).findIssueCreatedTimestamps(eq(globalScope), eq(fromMillis), eq(toMillis));
        verify(aggregationRepository).findCompletedWorkOrderTimestamps(eq(globalScope), eq(fromMillis), eq(toMillis));
        verify(aggregationRepository).findPreventiveCandidateGeneratedTimestamps(eq(globalScope), eq(fromMillis), eq(toMillis));
        verify(aggregationRepository).findAcceptedSuggestedActionTimestamps(eq(globalScope), eq(fromMillis), eq(toMillis));
    }

    private void stubTimestamps(OperationsKpiScope scope) {
        when(aggregationRepository.findCompletedInspectionTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
        stubRemainingTimestamps(scope);
    }

    private void stubRemainingTimestamps(OperationsKpiScope scope) {
        when(aggregationRepository.findIssueCreatedTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
        when(aggregationRepository.findCompletedWorkOrderTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
        when(aggregationRepository.findPreventiveCandidateGeneratedTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
        when(aggregationRepository.findAcceptedSuggestedActionTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
    }

    private void stubRemainingTimestampsExceptIssues(OperationsKpiScope scope) {
        when(aggregationRepository.findCompletedWorkOrderTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
        when(aggregationRepository.findPreventiveCandidateGeneratedTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
        when(aggregationRepository.findAcceptedSuggestedActionTimestamps(scope, fromMillis, toMillis))
                .thenReturn(List.of());
    }
}
