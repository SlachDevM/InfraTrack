package com.infratrack.inspection;

import com.infratrack.asset.Asset;
import com.infratrack.businesstrigger.BusinessTrigger;
import com.infratrack.businesstrigger.BusinessTriggerRepository;
import com.infratrack.businesstrigger.BusinessTriggerType;
import com.infratrack.department.Department;
import com.infratrack.exception.BusinessValidationException;
import com.infratrack.exception.ConflictException;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.dto.AssignInspectionRequest;
import com.infratrack.inspection.dto.CompleteInspectionRequest;
import com.infratrack.inspection.dto.InspectionAnswerRequest;
import com.infratrack.inspection.dto.InspectionAnswerResponse;
import com.infratrack.inspection.dto.InspectionResponse;
import com.infratrack.inspection.dto.InspectionSummaryResponse;
import com.infratrack.inspection.dto.SaveInspectionAnswersRequest;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateRepository;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.preventivemaintenance.PreventiveExecutionCandidate;
import com.infratrack.preventivemaintenance.PreventiveMaintenancePlan;
import com.infratrack.preventivemaintenance.PlanTargetAction;
import com.infratrack.preventivemaintenance.dto.ApprovePreventiveCandidateRequest;
import com.infratrack.ruleevaluation.RuleEvaluationReport;
import com.infratrack.ruleevaluation.RuleEvaluationReportService;
import com.infratrack.ruleevaluation.dto.RuleEvaluationReportSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.infratrack.notification.OperationalEventNotificationService;
import com.infratrack.user.User;
import com.infratrack.user.UserNameLookup;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Assigns and completes inspections (UC-003, UC-004) and records asset history events.
 */
@Service
public class InspectionService {

    private final InspectionRepository inspectionRepository;
    private final BusinessTriggerRepository businessTriggerRepository;
    private final InspectionTemplateRepository inspectionTemplateRepository;
    private final InspectionAuthorizationService authorizationService;
    private final InspectionHistoryRecorder historyRecorder;
    private final InspectionAnswerService inspectionAnswerService;
    private final UserService userService;
    private final UserNameLookup userNameLookup;
    private final OperationalEventNotificationService operationalEventNotificationService;
    private final RuleEvaluationReportService ruleEvaluationReportService;

    public InspectionService(
            InspectionRepository inspectionRepository,
            BusinessTriggerRepository businessTriggerRepository,
            InspectionTemplateRepository inspectionTemplateRepository,
            InspectionAuthorizationService authorizationService,
            InspectionHistoryRecorder historyRecorder,
            InspectionAnswerService inspectionAnswerService,
            UserService userService,
            UserNameLookup userNameLookup,
            OperationalEventNotificationService operationalEventNotificationService,
            RuleEvaluationReportService ruleEvaluationReportService) {
        this.inspectionRepository = inspectionRepository;
        this.businessTriggerRepository = businessTriggerRepository;
        this.inspectionTemplateRepository = inspectionTemplateRepository;
        this.authorizationService = authorizationService;
        this.historyRecorder = historyRecorder;
        this.inspectionAnswerService = inspectionAnswerService;
        this.userService = userService;
        this.userNameLookup = userNameLookup;
        this.operationalEventNotificationService = operationalEventNotificationService;
        this.ruleEvaluationReportService = ruleEvaluationReportService;
    }

    @Transactional(readOnly = true)
    public Page<InspectionSummaryResponse> listPage(Pageable pageable) {
        Page<Inspection> page = inspectionRepository.findAllByOrderByCreatedAtDesc(pageable);
        return mapInspectionSummaries(page);
    }

    @Transactional(readOnly = true)
    public Page<InspectionSummaryResponse> listEligibleForIssueRecordingPage(Long userId, Pageable pageable) {
        User user = userService.getById(userId);
        if (!user.getRole().isFieldEmployee() && !user.getRole().isContractor()) {
            throw new ForbiddenOperationException(
                    "Only field employees and contractors can record issues");
        }
        Department department = user.getDepartment();
        if (department == null) {
            return Page.empty(pageable);
        }
        Page<Inspection> page = inspectionRepository.findEligibleForIssueRecording(
                InspectionStatus.COMPLETED,
                userId,
                department.getId(),
                pageable);
        return mapInspectionSummaries(page);
    }

    private Page<InspectionSummaryResponse> mapInspectionSummaries(Page<Inspection> page) {
        Map<Long, String> userNames = userNameLookup.resolveNames(
                page.getContent().stream()
                        .map(Inspection::getAssignedToUserId)
                        .filter(Objects::nonNull)
                        .toList());
        return page.map(inspection -> InspectionSummaryResponse.from(inspection, userNames));
    }

    @Transactional(readOnly = true)
    public InspectionResponse getById(Long id) {
        return toResponse(findInspectionOrThrow(id));
    }

    @Transactional
    public InspectionResponse createInspectionFromApprovedPreventiveCandidate(
            PreventiveExecutionCandidate candidate,
            ApprovePreventiveCandidateRequest request,
            Long decidingUserId) {
        if (candidate.getTargetActionSnapshot() != PlanTargetAction.CREATE_INSPECTION) {
            throw new BusinessValidationException("Target action not supported yet.");
        }

        Asset asset = candidate.getAsset();
        User assignedToUser = findPreventiveAssigneeOrThrow(request.getAssigneeId(), asset);
        LocalDate expectedCompletionDate = resolvePlannedDate(request.getPlannedAt());
        validateExpectedCompletionDate(expectedCompletionDate);

        BusinessTrigger businessTrigger = businessTriggerRepository.save(new BusinessTrigger(
                asset,
                BusinessTriggerType.SCHEDULED_INSPECTION,
                buildPreventiveTriggerReason(candidate),
                false,
                decidingUserId));

        PreventiveMaintenancePlan plan = candidate.getPreventiveMaintenancePlan();
        InspectionTemplate inspectionTemplate = plan != null ? plan.getInspectionTemplate() : null;
        if (inspectionTemplate != null) {
            inspectionTemplate = resolveOptionalTemplate(inspectionTemplate.getId(), asset);
        }

        InspectionPriority priority = mapPlanPriority(plan);

        Inspection inspection = new Inspection(
                asset,
                businessTrigger,
                assignedToUser.getId(),
                decidingUserId,
                priority,
                expectedCompletionDate);
        inspection.setPreventiveExecutionCandidate(candidate);
        if (inspectionTemplate != null) {
            inspection.setInspectionTemplate(inspectionTemplate);
        }
        inspection = inspectionRepository.save(inspection);

        historyRecorder.recordInspectionAssigned(asset, decidingUserId, LocalDate.now());
        historyRecorder.recordPreventiveInspectionCreated(
                asset,
                decidingUserId,
                LocalDate.now(),
                candidate.getId(),
                candidate.getPlanCodeSnapshot());

        operationalEventNotificationService.notifyInspectionAssigned(assignedToUser.getId());

        User decidingUser = userService.getById(decidingUserId);
        return InspectionResponse.from(inspection, assignedToUser, decidingUser);
    }

    @Transactional
    public InspectionResponse assignInspection(AssignInspectionRequest request, Long userId) {
        User coordinator = userService.getById(userId);
        authorizationService.requireCanAssignInspections(coordinator);

        BusinessTrigger businessTrigger = findBusinessTriggerOrThrow(request.getBusinessTriggerId());
        Asset asset = businessTrigger.getAsset();
        requireOwnDepartment(coordinator, asset);
        User assignedToUser = findAssignableUserOrThrow(request.getAssignedToUserId(), coordinator, asset);
        validateNoActiveAssignment(businessTrigger.getId());
        validateExpectedCompletionDate(request.getExpectedCompletionDate());

        InspectionPriority priority = resolvePriority(businessTrigger, request.getPriority());
        InspectionTemplate inspectionTemplate = resolveOptionalTemplate(request.getInspectionTemplateId(), asset);

        Inspection inspection = new Inspection(
                asset,
                businessTrigger,
                assignedToUser.getId(),
                userId,
                priority,
                request.getExpectedCompletionDate()
        );
        if (inspectionTemplate != null) {
            inspection.setInspectionTemplate(inspectionTemplate);
        }
        inspection = inspectionRepository.save(inspection);

        historyRecorder.recordInspectionAssigned(asset, userId, LocalDate.now());

        operationalEventNotificationService.notifyInspectionAssigned(assignedToUser.getId());

        return InspectionResponse.from(inspection, assignedToUser, coordinator);
    }

    void requireOwnDepartment(User user, Asset asset) {
        Department userDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (userDepartment == null || assetDepartment == null
                || !userDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "You may only assign inspections for assets in your own department.");
        }
    }

    private User findPreventiveAssigneeOrThrow(Long assignedToUserId, Asset asset) {
        if (assignedToUserId == null) {
            throw new BusinessValidationException("Assigned user is required");
        }
        User user = userService.getById(assignedToUserId);
        if (!user.getRole().isFieldEmployee()) {
            throw new ForbiddenOperationException("Assigned user is not a Field Employee.");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ForbiddenOperationException("Assigned worker is disabled.");
        }
        Department assigneeDepartment = user.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (assigneeDepartment == null || assetDepartment == null
                || !assigneeDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "Assigned worker must belong to the asset department.");
        }
        return user;
    }

    private static LocalDate resolvePlannedDate(Long plannedAt) {
        if (plannedAt == null) {
            return null;
        }
        return Instant.ofEpochMilli(plannedAt).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static String buildPreventiveTriggerReason(PreventiveExecutionCandidate candidate) {
        return "Preventive maintenance: "
                + candidate.getPlanCodeSnapshot()
                + " - "
                + candidate.getPlanNameSnapshot();
    }

    private static InspectionPriority mapPlanPriority(PreventiveMaintenancePlan plan) {
        if (plan == null || plan.getPriority() == null) {
            return InspectionPriority.NORMAL;
        }
        return switch (plan.getPriority()) {
            case LOW -> InspectionPriority.LOW;
            case MEDIUM -> InspectionPriority.NORMAL;
            case HIGH -> InspectionPriority.HIGH;
            case CRITICAL -> InspectionPriority.URGENT;
        };
    }

    @Transactional
    public List<InspectionAnswerResponse> saveInspectionAnswers(
            Long inspectionId,
            SaveInspectionAnswersRequest request,
            Long userId) {
        Inspection inspection = findInspectionOrThrow(inspectionId);
        User user = userService.getById(userId);
        authorizationService.requireCanSaveInspectionAnswers(user, inspection);
        requireActiveForAnswerSave(inspection);

        List<InspectionAnswerRequest> answers = request.getAnswers() == null ? List.of() : request.getAnswers();
        return inspectionAnswerService.upsertProgressiveAnswers(inspection, answers);
    }

    @Transactional
    public InspectionResponse completeInspection(Long inspectionId, CompleteInspectionRequest request, Long userId) {
        Inspection inspection = findInspectionOrThrow(inspectionId);
        User performer = authorizationService.requireAssignedPerformer(userId, inspection);
        requireAssignedStatus(inspection);

        PhysicalCondition observedCondition = validateObservedCondition(request.getObservedCondition());
        String observations = normalizeObservations(request.getObservations());
        boolean issueIdentified = request.getIssueIdentified() != null && request.getIssueIdentified();
        LocalDateTime completedAt = validateCompletedAt(request.getCompletedAt());

        inspection.complete(observedCondition, observations, issueIdentified, completedAt, performer.getId());
        inspectionRepository.save(inspection);

        int answerCount = inspectionAnswerService.saveAnswers(inspection, request.getAnswers());

        RuleEvaluationReport evaluationReport = ruleEvaluationReportService.createReportIfApplicable(inspectionId);

        String historyDetails = buildCompletionHistoryDetails(answerCount, evaluationReport);
        historyRecorder.recordInspectionCompleted(
                inspection.getAsset(), performer.getId(), completedAt.toLocalDate(), historyDetails);

        RuleEvaluationReportSummaryResponse reportSummary =
                RuleEvaluationReportService.toSummary(evaluationReport);
        return toResponse(inspection, performer, reportSummary);
    }

    private static String buildCompletionHistoryDetails(int answerCount, RuleEvaluationReport evaluationReport) {
        StringBuilder details = new StringBuilder();
        if (answerCount > 0) {
            details.append("Structured answers recorded: ").append(answerCount);
        }
        if (evaluationReport != null) {
            if (details.length() > 0) {
                details.append(". ");
            }
            details.append(RuleEvaluationReportService.formatHistoryDetail(evaluationReport));
        }
        return details.length() > 0 ? details.toString() : null;
    }

    public void requireCanAssignInspections(Long userId) {
        authorizationService.requireCanAssignInspections(userId);
    }

    private InspectionResponse toResponse(Inspection inspection) {
        User assignedToUser = userService.getById(inspection.getAssignedToUserId());
        return toResponse(inspection, assignedToUser);
    }

    private InspectionResponse toResponse(Inspection inspection, User assignedToUser) {
        return toResponse(inspection, assignedToUser, null);
    }

    private InspectionResponse toResponse(
            Inspection inspection,
            User assignedToUser,
            RuleEvaluationReportSummaryResponse ruleEvaluationReportSummary) {
        List<InspectionAnswerResponse> answers = inspection.getInspectionTemplate() != null
                ? inspectionAnswerService.listByInspectionId(inspection.getId())
                : List.of();
        return InspectionResponse.from(
                inspection, assignedToUser, null, answers, ruleEvaluationReportSummary);
    }

    private InspectionTemplate resolveOptionalTemplate(Long inspectionTemplateId, Asset asset) {
        if (inspectionTemplateId == null) {
            return null;
        }
        InspectionTemplate template = inspectionTemplateRepository.findDetailedById(inspectionTemplateId)
                .orElseThrow(() -> new NotFoundException("Inspection template not found"));
        if (template.getStatus() != InspectionTemplateStatus.PUBLISHED) {
            throw new BusinessValidationException(
                    "Only published inspection templates can be used for inspections");
        }
        if (!template.getAssetCategory().getId().equals(asset.getAssetCategory().getId())) {
            throw new BusinessValidationException(
                    "Inspection template must belong to the asset category");
        }
        return template;
    }

    private Inspection findInspectionOrThrow(Long id) {
        return inspectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inspection not found"));
    }

    private BusinessTrigger findBusinessTriggerOrThrow(Long businessTriggerId) {
        if (businessTriggerId == null) {
            throw new BusinessValidationException("Business trigger is required");
        }
        return businessTriggerRepository.findById(businessTriggerId)
                .orElseThrow(() -> new BusinessValidationException("Business trigger not found"));
    }

    private User findAssignableUserOrThrow(Long assignedToUserId, User coordinator, Asset asset) {
        if (assignedToUserId == null) {
            throw new BusinessValidationException("Assigned user is required");
        }
        User user = userService.getById(assignedToUserId);
        if (!user.getRole().isFieldEmployee()) {
            throw new ForbiddenOperationException("Assigned user is not a Field Employee.");
        }
        if (!Boolean.TRUE.equals(user.getEnabled())) {
            throw new ForbiddenOperationException("Assigned worker is disabled.");
        }
        requireAssigneeDepartment(user, coordinator, asset);
        return user;
    }

    private void requireAssigneeDepartment(User assignee, User coordinator, Asset asset) {
        Department assigneeDepartment = assignee.getDepartment();
        Department coordinatorDepartment = coordinator.getDepartment();
        Department assetDepartment = asset.getDepartment();
        if (coordinatorDepartment == null || assigneeDepartment == null || assetDepartment == null
                || !assigneeDepartment.getId().equals(coordinatorDepartment.getId())
                || !assigneeDepartment.getId().equals(assetDepartment.getId())) {
            throw new ForbiddenOperationException(
                    "Assigned worker must belong to your department.");
        }
    }

    private void validateNoActiveAssignment(Long businessTriggerId) {
        if (inspectionRepository.existsByBusinessTriggerIdAndStatus(
                businessTriggerId, InspectionStatus.ASSIGNED)) {
            throw new BusinessValidationException(
                    "An active inspection is already assigned for this business trigger");
        }
    }

    private void validateExpectedCompletionDate(LocalDate expectedCompletionDate) {
        if (expectedCompletionDate != null && expectedCompletionDate.isBefore(LocalDate.now())) {
            throw new BusinessValidationException(
                    "Expected completion date cannot be in the past");
        }
    }

    private InspectionPriority resolvePriority(BusinessTrigger businessTrigger, InspectionPriority requested) {
        if (requested != null) {
            return requested;
        }
        if (businessTrigger.isUrgent()) {
            return InspectionPriority.URGENT;
        }
        return InspectionPriority.NORMAL;
    }

    private void requireAssignedStatus(Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.ASSIGNED) {
            throw new BusinessValidationException(
                    "Only assigned inspections can be completed");
        }
    }

    private void requireActiveForAnswerSave(Inspection inspection) {
        if (inspection.getStatus() != InspectionStatus.ASSIGNED) {
            throw new ConflictException("Inspection answers cannot be modified after completion");
        }
    }

    private PhysicalCondition validateObservedCondition(PhysicalCondition observedCondition) {
        if (observedCondition == null) {
            throw new BusinessValidationException("Observed condition is required");
        }
        return observedCondition;
    }

    private String normalizeObservations(String observations) {
        if (observations == null || observations.isBlank()) {
            throw new BusinessValidationException("Inspection observations are required");
        }
        return observations.trim();
    }

    private LocalDateTime validateCompletedAt(LocalDateTime completedAt) {
        if (completedAt == null) {
            throw new BusinessValidationException("Completion date and time are required");
        }
        if (completedAt.isAfter(LocalDateTime.now())) {
            throw new BusinessValidationException(
                    "Completion date and time cannot be in the future");
        }
        return completedAt;
    }
}
