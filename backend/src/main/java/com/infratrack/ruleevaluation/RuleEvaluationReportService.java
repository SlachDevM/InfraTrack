package com.infratrack.ruleevaluation;

import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspectiontemplate.DecisionRuleEvaluationService;
import com.infratrack.inspectiontemplate.dto.DecisionRuleEvaluationResult;
import com.infratrack.ruleevaluation.dto.RuleEvaluationReportResponse;
import com.infratrack.ruleevaluation.dto.RuleEvaluationReportSummaryResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Persists and retrieves rule evaluation reports (V2 Domain Engine A3.3).
 */
@Service
public class RuleEvaluationReportService {

    private final InspectionRepository inspectionRepository;
    private final InspectionAnswerRepository answerRepository;
    private final RuleEvaluationReportRepository reportRepository;
    private final DecisionRuleEvaluationService decisionRuleEvaluationService;
    private final InspectionAuthorizationService authorizationService;
    private final UserService userService;

    public RuleEvaluationReportService(
            InspectionRepository inspectionRepository,
            InspectionAnswerRepository answerRepository,
            RuleEvaluationReportRepository reportRepository,
            DecisionRuleEvaluationService decisionRuleEvaluationService,
            InspectionAuthorizationService authorizationService,
            UserService userService) {
        this.inspectionRepository = inspectionRepository;
        this.answerRepository = answerRepository;
        this.reportRepository = reportRepository;
        this.decisionRuleEvaluationService = decisionRuleEvaluationService;
        this.authorizationService = authorizationService;
        this.userService = userService;
    }

    /**
     * Creates a persisted evaluation report after inspection completion when applicable.
     * Returns null when the inspection has no template or no structured answers.
     */
    @Transactional
    public RuleEvaluationReport createReportIfApplicable(Long inspectionId) {
        Inspection inspection = inspectionRepository.findWithEvaluationContextById(inspectionId)
                .orElseThrow(() -> new NotFoundException("Inspection not found"));

        if (inspection.getInspectionTemplate() == null) {
            return null;
        }

        List<InspectionAnswer> answers =
                answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(inspectionId);
        if (answers.isEmpty()) {
            return null;
        }

        long evaluatedAt = System.currentTimeMillis();
        long startNanos = System.nanoTime();
        List<DecisionRuleEvaluationResult> evaluationResults =
                decisionRuleEvaluationService.evaluateLoadedInspection(inspection, answers);
        long evaluationDurationMs = (System.nanoTime() - startNanos) / 1_000_000L;

        int matchedCount = (int) evaluationResults.stream()
                .filter(DecisionRuleEvaluationResult::isMatched)
                .count();

        RuleEvaluationReport report = new RuleEvaluationReport(
                inspection,
                evaluatedAt,
                RuleEngineVersion.CURRENT,
                evaluationDurationMs,
                evaluationResults.size(),
                matchedCount
        );

        for (DecisionRuleEvaluationResult evaluationResult : evaluationResults) {
            report.addResult(toPersistedResult(evaluationResult));
        }

        return reportRepository.save(report);
    }

    @Transactional(readOnly = true)
    public List<RuleEvaluationReportResponse> listReports(Long inspectionId, Long userId) {
        requireVisibleInspection(inspectionId, userId);
        return reportRepository.findByInspection_IdOrderByEvaluatedAtDesc(inspectionId).stream()
                .map(report -> RuleEvaluationReportResponse.from(report, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public RuleEvaluationReportResponse getLatestReport(Long inspectionId, Long userId) {
        requireVisibleInspection(inspectionId, userId);
        RuleEvaluationReport report = reportRepository.findFirstByInspection_IdOrderByEvaluatedAtDesc(inspectionId)
                .orElseThrow(() -> new NotFoundException("Rule evaluation report not found"));
        return RuleEvaluationReportResponse.from(report);
    }

    @Transactional(readOnly = true)
    public RuleEvaluationReportResponse getReport(Long inspectionId, Long reportId, Long userId) {
        requireVisibleInspection(inspectionId, userId);
        RuleEvaluationReport report = reportRepository.findByIdAndInspection_Id(reportId, inspectionId)
                .orElseThrow(() -> new NotFoundException("Rule evaluation report not found"));
        return RuleEvaluationReportResponse.from(report);
    }

    public static String formatHistoryDetail(RuleEvaluationReport report) {
        return "Rule evaluation completed: "
                + report.getMatchedCount()
                + " matched / "
                + report.getResultCount()
                + " evaluated.";
    }

    public static RuleEvaluationReportSummaryResponse toSummary(RuleEvaluationReport report) {
        return report == null ? null : RuleEvaluationReportSummaryResponse.from(report);
    }

    private Inspection requireVisibleInspection(Long inspectionId, Long userId) {
        Inspection inspection = inspectionRepository.findWithEvaluationContextById(inspectionId)
                .orElseThrow(() -> new NotFoundException("Inspection not found"));
        User user = userService.getById(userId);
        authorizationService.requireCanViewInspection(user, inspection);
        return inspection;
    }

    private static RuleEvaluationResult toPersistedResult(DecisionRuleEvaluationResult evaluationResult) {
        return new RuleEvaluationResult(
                evaluationResult.getRuleId(),
                evaluationResult.getRuleCode(),
                evaluationResult.getRuleName(),
                evaluationResult.getConditionType(),
                evaluationResult.getOperator(),
                evaluationResult.getComparisonValue(),
                evaluationResult.getActualValue(),
                evaluationResult.isMatched(),
                evaluationResult.getActionType(),
                evaluationResult.getActionPayload(),
                evaluationResult.getPriority(),
                evaluationResult.getEvaluatedAt(),
                evaluationResult.getEvaluationDurationMs()
        );
    }
}
