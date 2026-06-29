package com.infratrack.ruleevaluation;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.exception.ForbiddenOperationException;
import com.infratrack.exception.NotFoundException;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionAnswer;
import com.infratrack.inspection.InspectionAnswerQuestionTypeSnapshot;
import com.infratrack.inspection.InspectionAnswerRepository;
import com.infratrack.inspection.InspectionAuthorizationService;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspection.InspectionRepository;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleEvaluationService;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestion;
import com.infratrack.inspectiontemplate.InspectionTemplateQuestionType;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.inspectiontemplate.dto.DecisionRuleEvaluationResult;
import com.infratrack.ruleevaluation.dto.RuleEvaluationReportResponse;
import com.infratrack.user.User;
import com.infratrack.user.UserRole;
import com.infratrack.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleEvaluationReportServiceTest {

    @Mock
    private InspectionRepository inspectionRepository;

    @Mock
    private InspectionAnswerRepository answerRepository;

    @Mock
    private RuleEvaluationReportRepository reportRepository;

    @Mock
    private DecisionRuleEvaluationService decisionRuleEvaluationService;

    @Mock
    private InspectionAuthorizationService authorizationService;

    @Mock
    private UserService userService;

    private RuleEvaluationReportService reportService;

    @BeforeEach
    void setUp() {
        reportService = new RuleEvaluationReportService(
                inspectionRepository,
                answerRepository,
                reportRepository,
                decisionRuleEvaluationService,
                authorizationService,
                userService);
    }

    @Test
    void createReportIfApplicable_shouldReturnNullForLegacyInspectionWithoutTemplate() {
        Inspection inspection = templatedInspection();
        inspection.setInspectionTemplate(null);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));

        RuleEvaluationReport report = reportService.createReportIfApplicable(100L);

        assertThat(report).isNull();
        verify(reportRepository, never()).save(any());
    }

    @Test
    void createReportIfApplicable_shouldReturnNullWhenNoStructuredAnswers() {
        Inspection inspection = templatedInspection();
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L)).thenReturn(List.of());

        RuleEvaluationReport report = reportService.createReportIfApplicable(100L);

        assertThat(report).isNull();
        verify(decisionRuleEvaluationService, never()).evaluateLoadedInspection(any(), any());
    }

    @Test
    void createReportIfApplicable_shouldPersistReportWithZeroResultsWhenNoActiveRules() {
        Inspection inspection = templatedInspection();
        InspectionAnswer answer = booleanAnswer(inspection);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L)).thenReturn(List.of(answer));
        when(decisionRuleEvaluationService.evaluateLoadedInspection(inspection, List.of(answer))).thenReturn(List.of());
        when(reportRepository.save(any(RuleEvaluationReport.class))).thenAnswer(invocation -> {
            RuleEvaluationReport saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        RuleEvaluationReport report = reportService.createReportIfApplicable(100L);

        assertThat(report.getResultCount()).isZero();
        assertThat(report.getMatchedCount()).isZero();
        assertThat(report.getEngineVersion()).isEqualTo(RuleEngineVersion.CURRENT);
        assertThat(report.getResults()).isEmpty();
    }

    @Test
    void createReportIfApplicable_shouldPersistSnapshotsAndCounts() {
        Inspection inspection = templatedInspection();
        InspectionAnswer answer = numberAnswer(inspection);
        DecisionRuleEvaluationResult evaluationResult = evaluationResult(true);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(answerRepository.findByInspectionIdOrderByQuestionDisplayOrder(100L))
                .thenReturn(List.of(answer));
        when(decisionRuleEvaluationService.evaluateLoadedInspection(inspection, List.of(answer)))
                .thenReturn(List.of(evaluationResult));
        when(reportRepository.save(any(RuleEvaluationReport.class))).thenAnswer(invocation -> {
            RuleEvaluationReport saved = invocation.getArgument(0);
            saved.setId(10L);
            return saved;
        });

        reportService.createReportIfApplicable(100L);

        ArgumentCaptor<RuleEvaluationReport> captor = ArgumentCaptor.forClass(RuleEvaluationReport.class);
        verify(reportRepository).save(captor.capture());
        RuleEvaluationReport saved = captor.getValue();
        assertThat(saved.getResultCount()).isEqualTo(1);
        assertThat(saved.getMatchedCount()).isEqualTo(1);
        assertThat(saved.getResults()).hasSize(1);
        RuleEvaluationResult persisted = saved.getResults().get(0);
        assertThat(persisted.getRuleCodeSnapshot()).isEqualTo("HIGH_TEMP");
        assertThat(persisted.getRuleNameSnapshot()).isEqualTo("High temperature");
        assertThat(persisted.getActionTypeSnapshot()).isEqualTo(DecisionRuleActionType.SUGGEST_ISSUE);
        assertThat(persisted.getPrioritySnapshot()).isEqualTo(10);
        assertThat(persisted.isMatched()).isTrue();
    }

    @Test
    void getLatestReport_shouldReturnNewestReport() {
        Inspection inspection = templatedInspection();
        User viewer = user(20L, UserRole.OPERATIONAL_COORDINATOR);
        RuleEvaluationReport latest = savedReport(inspection, 2000L, 2, 1);
        latest.setId(2L);
        RuleEvaluationReport older = savedReport(inspection, 1000L, 1, 0);
        older.setId(1L);

        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(viewer);
        when(reportRepository.findFirstByInspection_IdOrderByEvaluatedAtDesc(100L))
                .thenReturn(Optional.of(latest));

        RuleEvaluationReportResponse response = reportService.getLatestReport(100L, 20L);

        assertThat(response.getId()).isEqualTo(2L);
        assertThat(response.getMatchedCount()).isEqualTo(1);
        verify(authorizationService).requireCanViewInspection(viewer, inspection);
    }

    @Test
    void getReport_shouldRejectCrossDepartmentAccess() {
        Inspection inspection = templatedInspection();
        User otherDepartmentUser = user(99L, UserRole.OPERATIONAL_COORDINATOR);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(99L)).thenReturn(otherDepartmentUser);
        org.mockito.Mockito.doThrow(new ForbiddenOperationException(
                        "You may only view inspections for assets in your own department."))
                .when(authorizationService).requireCanViewInspection(otherDepartmentUser, inspection);

        assertThatThrownBy(() -> reportService.getReport(100L, 5L, 99L))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void getReport_shouldRejectWhenReportNotFoundForInspection() {
        Inspection inspection = templatedInspection();
        User viewer = user(20L, UserRole.OPERATIONAL_COORDINATOR);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(viewer);
        when(reportRepository.findByIdAndInspection_Id(5L, 100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.getReport(100L, 5L, 20L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void listReports_shouldReturnSummariesWithoutResults() {
        Inspection inspection = templatedInspection();
        User viewer = user(20L, UserRole.MANAGER);
        RuleEvaluationReport report = savedReport(inspection, 1000L, 1, 1);
        report.setId(1L);
        when(inspectionRepository.findWithEvaluationContextById(100L)).thenReturn(Optional.of(inspection));
        when(userService.getById(20L)).thenReturn(viewer);
        when(reportRepository.findByInspection_IdOrderByEvaluatedAtDesc(100L)).thenReturn(List.of(report));

        List<RuleEvaluationReportResponse> responses = reportService.listReports(100L, 20L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getResults()).isEmpty();
    }

    private Inspection templatedInspection() {
        Department department = new Department("Parks");
        department.setId(1L);
        AssetCategory category = new AssetCategory("Pump");
        category.setId(2L);
        Asset asset = new Asset(
                "Pump Station",
                department,
                category,
                "Site A",
                AssetStatus.ACTIVE,
                null,
                1L);
        asset.setId(5L);
        InspectionTemplate template = new InspectionTemplate(
                "Pump Checklist", null, category, 1, InspectionTemplateStatus.PUBLISHED);
        template.setId(50L);
        Inspection inspection = new Inspection(
                asset, null, 20L, 10L, InspectionPriority.NORMAL, null);
        inspection.setId(100L);
        inspection.setInspectionTemplate(template);
        return inspection;
    }

    private InspectionAnswer booleanAnswer(Inspection inspection) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                inspection.getInspectionTemplate(),
                "Leak detected",
                "LEAK",
                null,
                InspectionTemplateQuestionType.BOOLEAN,
                true,
                1);
        question.setId(1L);
        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.BOOLEAN,
                true,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private InspectionAnswer numberAnswer(Inspection inspection) {
        InspectionTemplateQuestion question = new InspectionTemplateQuestion(
                inspection.getInspectionTemplate(),
                "Temperature",
                "TEMP",
                null,
                InspectionTemplateQuestionType.NUMBER,
                true,
                1);
        question.setId(2L);
        return new InspectionAnswer(
                inspection,
                question,
                question.getCode(),
                question.getQuestionText(),
                InspectionAnswerQuestionTypeSnapshot.NUMBER,
                null,
                null,
                new BigDecimal("95"),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private DecisionRuleEvaluationResult evaluationResult(boolean matched) {
        DecisionRuleEvaluationResult result = new DecisionRuleEvaluationResult();
        result.setRuleId(500L);
        result.setRuleCode("HIGH_TEMP");
        result.setRuleName("High temperature");
        result.setMatched(matched);
        result.setConditionType(DecisionRuleConditionType.NUMBER);
        result.setOperator(DecisionRuleOperator.GREATER_THAN);
        result.setComparisonValue("90");
        result.setActualValue("95");
        result.setActionType(DecisionRuleActionType.SUGGEST_ISSUE);
        result.setActionPayload("{\"severity\":\"HIGH\"}");
        result.setPriority(10);
        result.setEvaluatedAt(1_700_000_000_000L);
        result.setEvaluationDurationMs(3L);
        return result;
    }

    private RuleEvaluationReport savedReport(
            Inspection inspection,
            long evaluatedAt,
            int resultCount,
            int matchedCount) {
        RuleEvaluationReport report = new RuleEvaluationReport(
                inspection,
                evaluatedAt,
                RuleEngineVersion.CURRENT,
                5L,
                resultCount,
                matchedCount);
        report.addResult(new RuleEvaluationResult(
                500L,
                "HIGH_TEMP",
                "High temperature",
                DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN,
                "90",
                "95",
                matchedCount > 0,
                DecisionRuleActionType.SUGGEST_ISSUE,
                null,
                10,
                evaluatedAt,
                3L));
        return report;
    }

    private User user(Long id, UserRole role) {
        User user = new User("user@test.com", "password", "User", role);
        user.setId(id);
        user.setEnabled(true);
        if (role != UserRole.ADMINISTRATOR) {
            Department department = new Department("Parks");
            department.setId(1L);
            user.setDepartment(department);
        }
        return user;
    }
}
