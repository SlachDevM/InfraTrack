package com.infratrack.preventivemaintenance;

import com.infratrack.exception.NotFoundException;
import com.infratrack.preventivemaintenance.dto.PreventiveExecutionReportResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persists and retrieves preventive execution audit reports (V2 Phase B Sprint B4).
 */
@Service
public class PreventiveExecutionReportService {

    private final PreventiveExecutionReportRepository reportRepository;
    private final PreventiveExecutionCandidateRepository candidateRepository;
    private final PreventiveExecutionHistoryRecorder historyRecorder;

    public PreventiveExecutionReportService(
            PreventiveExecutionReportRepository reportRepository,
            PreventiveExecutionCandidateRepository candidateRepository,
            PreventiveExecutionHistoryRecorder historyRecorder) {
        this.reportRepository = reportRepository;
        this.candidateRepository = candidateRepository;
        this.historyRecorder = historyRecorder;
    }

    @Transactional
    public PreventiveExecutionReport createReportForCandidate(
            PreventiveExecutionCandidate candidate,
            Long generatedByUserId) {
        PreventiveExecutionReport report = reportRepository.save(new PreventiveExecutionReport(candidate));
        if (generatedByUserId != null) {
            historyRecorder.recordCandidateGenerated(
                    candidate.getAsset(),
                    generatedByUserId,
                    candidate.getPlanCodeSnapshot());
        }
        return report;
    }

    @Transactional
    public void markApproved(PreventiveExecutionCandidate candidate, Long decidedByUserId) {
        PreventiveExecutionReport report = findReportForCandidate(candidate);
        report.markApproved(decidedByUserId);
        reportRepository.save(report);
        historyRecorder.recordCandidateApproved(
                candidate.getAsset(),
                decidedByUserId,
                candidate.getPlanCodeSnapshot());
    }

    @Transactional
    public void markInspectionCreated(
            PreventiveExecutionCandidate candidate,
            Long inspectionId,
            Long decidedByUserId) {
        PreventiveExecutionReport report = findReportForCandidate(candidate);
        report.markInspectionCreated(inspectionId, decidedByUserId);
        reportRepository.save(report);
    }

    @Transactional
    public void markRejected(PreventiveExecutionCandidate candidate, Long decidedByUserId, String reason) {
        PreventiveExecutionReport report = findReportForCandidate(candidate);
        report.markRejected(decidedByUserId, reason);
        reportRepository.save(report);
        historyRecorder.recordCandidateRejected(
                candidate.getAsset(),
                decidedByUserId,
                candidate.getPlanCodeSnapshot());
    }

    @Transactional
    public void markDismissed(PreventiveExecutionCandidate candidate, Long decidedByUserId, String comment) {
        PreventiveExecutionReport report = findReportForCandidate(candidate);
        report.markDismissed(decidedByUserId, comment);
        reportRepository.save(report);
        historyRecorder.recordCandidateDismissed(
                candidate.getAsset(),
                decidedByUserId,
                candidate.getPlanCodeSnapshot());
    }

    @Transactional(readOnly = true)
    public PreventiveExecutionReportResponse getReportByCandidateId(Long candidateId) {
        return PreventiveExecutionReportResponse.from(findReportByCandidateId(candidateId));
    }

    @Transactional(readOnly = true)
    public PreventiveExecutionReportResponse getReportById(Long reportId) {
        return PreventiveExecutionReportResponse.from(findReport(reportId));
    }

    @Transactional(readOnly = true)
    public Page<PreventiveExecutionReportResponse> listReports(
            ExecutionReportStatus status,
            Long assetId,
            Long planId,
            DecisionSource decisionSource,
            Pageable pageable) {
        return reportRepository.findFiltered(status, assetId, planId, decisionSource, pageable)
                .map(PreventiveExecutionReportResponse::from);
    }

    private PreventiveExecutionReport findReportForCandidate(PreventiveExecutionCandidate candidate) {
        return reportRepository.findByCandidateId(candidate.getId())
                .orElseThrow(() -> new NotFoundException("Preventive execution report not found"));
    }

    private PreventiveExecutionReport findReportByCandidateId(Long candidateId) {
        return reportRepository.findByCandidateId(candidateId)
                .orElseThrow(() -> new NotFoundException("Preventive execution report not found"));
    }

    private PreventiveExecutionReport findReport(Long reportId) {
        return reportRepository.findDetailedById(reportId)
                .orElseThrow(() -> new NotFoundException("Preventive execution report not found"));
    }
}
