package com.infratrack.preventivemaintenance;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.PreventiveExecutionReportResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PreventiveExecutionReportServiceTest {

    @Mock
    private PreventiveExecutionReportRepository reportRepository;

    @Mock
    private PreventiveExecutionCandidateRepository candidateRepository;

    @Mock
    private PreventiveExecutionHistoryRecorder historyRecorder;

    private PreventiveExecutionReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new PreventiveExecutionReportService(
                reportRepository,
                candidateRepository,
                historyRecorder);
    }

    @Test
    void createReportForCandidate_shouldCopySnapshotsAndSetGeneratedStatus() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        when(reportRepository.save(any(PreventiveExecutionReport.class))).thenAnswer(invocation -> {
            PreventiveExecutionReport report = invocation.getArgument(0);
            report.setId(900L);
            return report;
        });

        PreventiveExecutionReport report = reportService.createReportForCandidate(candidate, 30L);

        assertThat(report.getReportStatus()).isEqualTo(ExecutionReportStatus.GENERATED);
        assertThat(report.getDecisionSource()).isEqualTo(DecisionSource.PREVENTIVE_ENGINE);
        assertThat(report.getPlanCodeSnapshot()).isEqualTo("PUMP_MONTHLY");
        assertThat(report.getAssetNameSnapshot()).isEqualTo("Pump A");
        assertThat(report.getPreventiveMaintenancePlanIdSnapshot()).isEqualTo(100L);
        verify(historyRecorder).recordCandidateGenerated(candidate.getAsset(), 30L, "PUMP_MONTHLY");
    }

    @Test
    void markApprovedAndInspectionCreated_shouldUpdateReportLifecycle() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        candidate.setId(500L);
        PreventiveExecutionReport report = new PreventiveExecutionReport(candidate);
        report.setId(900L);
        when(reportRepository.findByCandidateId(500L)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        reportService.markApproved(candidate, 30L);
        reportService.markInspectionCreated(candidate, 700L, 30L);

        assertThat(report.getReportStatus()).isEqualTo(ExecutionReportStatus.INSPECTION_CREATED);
        assertThat(report.getCreatedInspectionId()).isEqualTo(700L);
        assertThat(report.getApprovedAt()).isNotNull();
        assertThat(report.getInspectionCreatedAt()).isNotNull();
        assertThat(report.getDecidedByUserId()).isEqualTo(30L);
        verify(historyRecorder).recordCandidateApproved(candidate.getAsset(), 30L, "PUMP_MONTHLY");
    }

    @Test
    void markRejected_shouldStoreReasonAndTimestamps() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        candidate.setId(500L);
        PreventiveExecutionReport report = new PreventiveExecutionReport(candidate);
        when(reportRepository.findByCandidateId(500L)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        reportService.markRejected(candidate, 30L, "Already inspected");

        assertThat(report.getReportStatus()).isEqualTo(ExecutionReportStatus.REJECTED);
        assertThat(report.getRejectedAt()).isNotNull();
        assertThat(report.getDecisionReason()).isEqualTo("Already inspected");
        verify(historyRecorder).recordCandidateRejected(candidate.getAsset(), 30L, "PUMP_MONTHLY");
    }

    @Test
    void markDismissed_shouldStoreCommentAndTimestamps() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        candidate.setId(500L);
        PreventiveExecutionReport report = new PreventiveExecutionReport(candidate);
        when(reportRepository.findByCandidateId(500L)).thenReturn(Optional.of(report));
        when(reportRepository.save(report)).thenReturn(report);

        reportService.markDismissed(candidate, 30L, "Not relevant");

        assertThat(report.getReportStatus()).isEqualTo(ExecutionReportStatus.DISMISSED);
        assertThat(report.getDismissedAt()).isNotNull();
        assertThat(report.getDecisionReason()).isEqualTo("Not relevant");
        verify(historyRecorder).recordCandidateDismissed(candidate.getAsset(), 30L, "PUMP_MONTHLY");
    }

    @Test
    void getReportByCandidateId_shouldReturnPersistedReport() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        candidate.setId(500L);
        PreventiveExecutionReport report = new PreventiveExecutionReport(candidate);
        report.setId(900L);
        when(reportRepository.findByCandidateId(500L)).thenReturn(Optional.of(report));

        PreventiveExecutionReportResponse response = reportService.getReportByCandidateId(500L);

        assertThat(response.getId()).isEqualTo(900L);
        assertThat(response.getCandidateId()).isEqualTo(500L);
        assertThat(response.getReportStatus()).isEqualTo(ExecutionReportStatus.GENERATED);
    }

    @Test
    void getReportByCandidateId_shouldRejectMissingReport() {
        when(reportRepository.findByCandidateId(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReportByCandidateId(999L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void createReportForCandidate_shouldPersistOneReportPerCandidate() {
        PreventiveExecutionCandidate candidate = pendingCandidate();
        ArgumentCaptor<PreventiveExecutionReport> captor =
                ArgumentCaptor.forClass(PreventiveExecutionReport.class);
        when(reportRepository.save(any(PreventiveExecutionReport.class))).thenAnswer(invocation -> {
            PreventiveExecutionReport report = invocation.getArgument(0);
            report.setId(900L);
            return report;
        });

        reportService.createReportForCandidate(candidate, 30L);

        verify(reportRepository).save(captor.capture());
        assertThat(captor.getValue().getCandidate()).isEqualTo(candidate);
    }

    private PreventiveExecutionCandidate pendingCandidate() {
        Department department = new Department("Water");
        department.setId(10L);
        Asset asset = new Asset(
                "Pump A",
                department,
                new AssetCategory("Pumps"),
                "Location",
                AssetStatus.ACTIVE,
                LocalDate.of(2024, 1, 1),
                1L);
        asset.setId(5L);
        PreventiveMaintenancePlan plan = new PreventiveMaintenancePlan(
                asset,
                "PUMP_MONTHLY",
                "Monthly Pump Inspection",
                null,
                1,
                PreventiveMaintenancePlanStatus.ACTIVE,
                PreventiveMaintenancePlanPriority.MEDIUM,
                PlanTargetAction.CREATE_INSPECTION,
                null);
        plan.setId(100L);
        PreventiveExecutionCandidate candidate = new PreventiveExecutionCandidate(
                plan,
                asset,
                PlanTriggerType.TIME,
                "One full month has elapsed.",
                1710000000000L,
                null,
                "PUMP_MONTHLY",
                1,
                "Monthly Pump Inspection",
                PlanTargetAction.CREATE_INSPECTION,
                "Every month",
                "Eligible once every full month from plan creation.");
        candidate.setId(500L);
        return candidate;
    }
}
