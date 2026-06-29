package com.infratrack.preventivemaintenance;

import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.InspectionService;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.preventivemaintenance.dto.ApprovePreventiveCandidateRequest;
import com.infratrack.preventivemaintenance.dto.ApprovePreventiveCandidateResponse;
import com.infratrack.preventivemaintenance.dto.DismissPreventiveCandidateRequest;
import com.infratrack.preventivemaintenance.dto.PreventiveExecutionCandidateResponse;
import com.infratrack.preventivemaintenance.dto.RejectPreventiveCandidateRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manager review of preventive execution candidates (V2 Phase B Sprint B3).
 */
@Service
public class PreventiveDecisionAssistantService {

    private final PreventiveExecutionCandidateRepository candidateRepository;
    private final PreventiveExecutionCandidateAuthorizationService authorizationService;
    private final InspectionService inspectionService;
    private final PreventiveExecutionReportService reportService;

    public PreventiveDecisionAssistantService(
            PreventiveExecutionCandidateRepository candidateRepository,
            PreventiveExecutionCandidateAuthorizationService authorizationService,
            InspectionService inspectionService,
            PreventiveExecutionReportService reportService) {
        this.candidateRepository = candidateRepository;
        this.authorizationService = authorizationService;
        this.inspectionService = inspectionService;
        this.reportService = reportService;
    }

    @Transactional
    public ApprovePreventiveCandidateResponse approve(
            Long candidateId,
            ApprovePreventiveCandidateRequest request,
            Long userId) {
        PreventiveExecutionCandidate candidate = findAuthorizedCandidate(candidateId, userId);
        candidate.requirePending();
        requireSupportedTargetAction(candidate);

        InspectionResponse inspection = inspectionService.createInspectionFromApprovedPreventiveCandidate(
                candidate,
                request,
                userId);
        reportService.markApproved(candidate, userId);
        candidate.markApproved(userId, inspection.getId(), normalizeOptionalText(request.getNotes()));
        PreventiveExecutionCandidate saved = candidateRepository.save(candidate);
        reportService.markInspectionCreated(candidate, inspection.getId(), userId);

        return new ApprovePreventiveCandidateResponse(
                PreventiveExecutionCandidateResponse.from(saved),
                inspection);
    }

    @Transactional
    public PreventiveExecutionCandidateResponse reject(
            Long candidateId,
            RejectPreventiveCandidateRequest request,
            Long userId) {
        PreventiveExecutionCandidate candidate = findAuthorizedCandidate(candidateId, userId);
        candidate.markRejected(userId, normalizeOptionalText(request.getReason()));
        PreventiveExecutionCandidate saved = candidateRepository.save(candidate);
        reportService.markRejected(saved, userId, saved.getRejectionReason());
        return PreventiveExecutionCandidateResponse.from(saved);
    }

    @Transactional
    public PreventiveExecutionCandidateResponse dismiss(
            Long candidateId,
            DismissPreventiveCandidateRequest request,
            Long userId) {
        PreventiveExecutionCandidate candidate = findAuthorizedCandidate(candidateId, userId);
        candidate.markDismissed(userId, normalizeOptionalText(request.getComment()));
        PreventiveExecutionCandidate saved = candidateRepository.save(candidate);
        reportService.markDismissed(saved, userId, saved.getDismissComment());
        return PreventiveExecutionCandidateResponse.from(saved);
    }

    private PreventiveExecutionCandidate findAuthorizedCandidate(Long candidateId, Long userId) {
        authorizationService.requireCanReviewCandidates(userId);
        PreventiveExecutionCandidate candidate = candidateRepository.findDetailedById(candidateId)
                .orElseThrow(() -> new NotFoundException("Preventive execution candidate not found"));
        authorizationService.requireAuthorizedForCandidateAsset(userId, candidate.getAsset());
        return candidate;
    }

    private static void requireSupportedTargetAction(PreventiveExecutionCandidate candidate) {
        if (candidate.getTargetActionSnapshot() != PlanTargetAction.CREATE_INSPECTION) {
            throw new BusinessValidationException("Target action not supported yet.");
        }
    }

    private static String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
