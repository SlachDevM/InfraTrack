package com.infratrack.operationsintelligence;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.operationsintelligence.dto.RecentActivityItemResponse;
import com.infratrack.operationsintelligence.dto.RecentActivityResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OperationsRecentActivityServiceTest {

    @Mock
    private OperationsRecentActivityAuthorizationService authorizationService;

    @Mock
    private OperationsRecentActivityAggregationRepository aggregationRepository;

    @InjectMocks
    private OperationsRecentActivityService recentActivityService;

    private final OperationsKpiScope globalScope = OperationsKpiScope.global();
    private final OperationsKpiScope departmentScope = OperationsKpiScope.forDepartment(10L);

    @Test
    void getRecentActivity_shouldReturnGlobalActivityForAdministrator() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findRecentActivityRows(globalScope, 20)).thenReturn(sampleRows());

        RecentActivityResponse response = recentActivityService.getRecentActivity(1L, null);

        assertThat(response.getItems()).hasSize(6);
        verify(aggregationRepository).findRecentActivityRows(globalScope, 20);
    }

    @Test
    void getRecentActivity_shouldUseDepartmentScopeForManager() {
        when(authorizationService.resolveScope(2L)).thenReturn(departmentScope);
        when(aggregationRepository.findRecentActivityRows(departmentScope, 20)).thenReturn(List.of());

        recentActivityService.getRecentActivity(2L, null);

        verify(aggregationRepository).findRecentActivityRows(departmentScope, 20);
    }

    @Test
    void getRecentActivity_shouldRejectFieldEmployee() {
        when(authorizationService.resolveScope(4L))
                .thenThrow(new ForbiddenOperationException("You do not have permission to view operational KPIs."));

        assertThatThrownBy(() -> recentActivityService.getRecentActivity(4L, null))
                .isInstanceOf(ForbiddenOperationException.class);

        verify(aggregationRepository, never()).findRecentActivityRows(any(), anyInt());
    }

    @Test
    void getRecentActivity_shouldRejectContractor() {
        when(authorizationService.resolveScope(5L))
                .thenThrow(new ForbiddenOperationException("You do not have permission to view operational KPIs."));

        assertThatThrownBy(() -> recentActivityService.getRecentActivity(5L, null))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getRecentActivity_shouldDefaultLimitTo20() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findRecentActivityRows(globalScope, 20)).thenReturn(List.of());

        recentActivityService.getRecentActivity(1L, null);

        verify(aggregationRepository).findRecentActivityRows(globalScope, 20);
    }

    @Test
    void getRecentActivity_shouldAcceptCustomLimit() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findRecentActivityRows(globalScope, 50)).thenReturn(List.of());

        recentActivityService.getRecentActivity(1L, 50);

        verify(aggregationRepository).findRecentActivityRows(globalScope, 50);
    }

    @Test
    void getRecentActivity_shouldRejectLimitAbove100() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);

        assertThatThrownBy(() -> recentActivityService.getRecentActivity(1L, 101))
                .isInstanceOf(BusinessValidationException.class)
                .hasMessage("Activity limit must be between 1 and 100.");
    }

    @Test
    void getRecentActivity_shouldRejectLimitBelow1() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);

        assertThatThrownBy(() -> recentActivityService.getRecentActivity(1L, 0))
                .isInstanceOf(BusinessValidationException.class);
    }

    @Test
    void getRecentActivity_shouldSortByOccurredAtDescending() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findRecentActivityRows(globalScope, 20)).thenReturn(List.of(
                row(RecentActivityType.ISSUE_CREATED, 1000L),
                row(RecentActivityType.INSPECTION_COMPLETED, 5000L),
                row(RecentActivityType.WORK_ORDER_COMPLETED, 3000L)));

        RecentActivityResponse response = recentActivityService.getRecentActivity(1L, null);

        assertThat(response.getItems())
                .extracting(RecentActivityItemResponse::getOccurredAt)
                .containsExactly(5000L, 3000L, 1000L);
    }

    @Test
    void getRecentActivity_shouldMapAllActivitySources() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findRecentActivityRows(globalScope, 20)).thenReturn(sampleRows());

        RecentActivityResponse response = recentActivityService.getRecentActivity(1L, null);

        assertThat(response.getItems())
                .extracting(RecentActivityItemResponse::getType)
                .containsExactlyInAnyOrder(
                        "INSPECTION_COMPLETED",
                        "ISSUE_CREATED",
                        "WORK_ORDER_COMPLETED",
                        "PREVENTIVE_CANDIDATE_GENERATED",
                        "PREVENTIVE_CANDIDATE_APPROVED",
                        "SUGGESTED_ACTION_ACCEPTED");
    }

    @Test
    void getRecentActivity_shouldMapItemFields() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findRecentActivityRows(globalScope, 20)).thenReturn(List.of(
                new RecentActivitySourceRow(
                        RecentActivityType.INSPECTION_COMPLETED,
                        12L,
                        "Street Light 001",
                        1719792000000L,
                        "Street Light 001")));

        RecentActivityItemResponse item = recentActivityService.getRecentActivity(1L, null).getItems().get(0);

        assertThat(item.getType()).isEqualTo("INSPECTION_COMPLETED");
        assertThat(item.getTitle()).isEqualTo("Inspection completed");
        assertThat(item.getDescription()).isEqualTo("Street Light 001");
        assertThat(item.getAssetId()).isEqualTo(12L);
        assertThat(item.getAssetName()).isEqualTo("Street Light 001");
        assertThat(item.getOccurredAt()).isEqualTo(1719792000000L);
        assertThat(item.getRoute()).isEqualTo("/inspections");
    }

    @Test
    void getRecentActivity_shouldRemainReadOnly() {
        when(authorizationService.resolveScope(1L)).thenReturn(globalScope);
        when(aggregationRepository.findRecentActivityRows(globalScope, 20)).thenReturn(List.of());

        recentActivityService.getRecentActivity(1L, null);

        verify(aggregationRepository).findRecentActivityRows(eq(globalScope), eq(20));
    }

    private static List<RecentActivitySourceRow> sampleRows() {
        return List.of(
                row(RecentActivityType.INSPECTION_COMPLETED, 6000L),
                row(RecentActivityType.ISSUE_CREATED, 5000L),
                row(RecentActivityType.WORK_ORDER_COMPLETED, 4000L),
                row(RecentActivityType.PREVENTIVE_CANDIDATE_GENERATED, 3000L),
                row(RecentActivityType.PREVENTIVE_CANDIDATE_APPROVED, 2000L),
                row(RecentActivityType.SUGGESTED_ACTION_ACCEPTED, 1000L));
    }

    private static RecentActivitySourceRow row(RecentActivityType type, long occurredAt) {
        return new RecentActivitySourceRow(type, 1L, "Asset A", occurredAt, "Asset A");
    }
}
