package com.infratrack.suggestedaction;

import com.infratrack.asset.Asset;
import com.infratrack.asset.AssetStatus;
import com.infratrack.assetcategory.AssetCategory;
import com.infratrack.department.Department;
import com.infratrack.inspection.Inspection;
import com.infratrack.inspection.InspectionPriority;
import com.infratrack.inspectiontemplate.DecisionRuleActionType;
import com.infratrack.inspectiontemplate.DecisionRuleConditionType;
import com.infratrack.inspectiontemplate.DecisionRuleOperator;
import com.infratrack.inspectiontemplate.InspectionTemplate;
import com.infratrack.inspectiontemplate.InspectionTemplateStatus;
import com.infratrack.ruleevaluation.RuleEngineVersion;
import com.infratrack.ruleevaluation.RuleEvaluationReport;
import com.infratrack.ruleevaluation.RuleEvaluationResult;
import com.infratrack.ruleevaluation.RuleEvaluationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuggestedActionGenerationServiceTest {

    @Mock
    private SuggestedActionRepository suggestedActionRepository;

    private SuggestedActionGenerationService generationService;

    @BeforeEach
    void setUp() {
        generationService = new SuggestedActionGenerationService(suggestedActionRepository);
    }

    @Test
    void generateFromReport_shouldCreateSuggestionForMatchedResult() {
        RuleEvaluationReport report = reportWithResults(matchedResult(), unmatchedResult());
        when(suggestedActionRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        List<SuggestedAction> suggestions = generationService.generateFromReport(report);

        assertThat(suggestions).hasSize(1);
        SuggestedAction suggestion = suggestions.get(0);
        assertThat(suggestion.getActionType()).isEqualTo(DecisionRuleActionType.SUGGEST_ISSUE);
        assertThat(suggestion.getTitle()).isEqualTo("High temperature detected");
        assertThat(suggestion.getMessage()).isEqualTo("Temperature exceeds safe operating range.");
        assertThat(suggestion.getSeverity()).isEqualTo("HIGH");
        assertThat(suggestion.getMatchedRuleCount()).isEqualTo(1);
        assertThat(suggestion.getSourceRuleCodes()).isEqualTo("HIGH_TEMP");
        assertThat(suggestion.getStatus()).isEqualTo(SuggestedActionStatus.PENDING);
        assertThat(suggestion.getConfidence()).isEqualTo(SuggestionConfidence.LOW);
        assertThat(suggestion.getSuggestedPayload()).contains("severity");

        ArgumentCaptor<List<SuggestedAction>> captor = ArgumentCaptor.forClass(List.class);
        verify(suggestedActionRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
    }

    @Test
    void generateFromReport_shouldCreateNoSuggestionsWhenNoMatchedResults() {
        RuleEvaluationReport report = reportWithResults(unmatchedResult());

        List<SuggestedAction> suggestions = generationService.generateFromReport(report);

        assertThat(suggestions).isEmpty();
        verify(suggestedActionRepository, never()).saveAll(anyList());
    }

    @Test
    void generateFromReport_shouldCreateNoSuggestionsWhenEvaluationNotSuccessful() {
        RuleEvaluationReport report = reportWithResults(matchedResult());
        report.setEvaluationStatus(RuleEvaluationStatus.PARTIAL);

        List<SuggestedAction> suggestions = generationService.generateFromReport(report);

        assertThat(suggestions).isEmpty();
        verify(suggestedActionRepository, never()).saveAll(anyList());
    }

    private RuleEvaluationReport reportWithResults(RuleEvaluationResult... results) {
        Inspection inspection = templatedInspection();
        RuleEvaluationReport report = new RuleEvaluationReport(
                inspection,
                1_700_000_000_000L,
                RuleEngineVersion.CURRENT,
                5L,
                results.length,
                (int) java.util.Arrays.stream(results).filter(RuleEvaluationResult::isMatched).count());
        report.setTemplateVersionSnapshot(1);
        report.setEvaluationStatus(RuleEvaluationStatus.SUCCESS);
        for (RuleEvaluationResult result : results) {
            report.addResult(result);
        }
        return report;
    }

    private RuleEvaluationResult matchedResult() {
        return new RuleEvaluationResult(
                1L,
                "HIGH_TEMP",
                "High temperature",
                DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.GREATER_THAN,
                "90",
                "95",
                true,
                DecisionRuleActionType.SUGGEST_ISSUE,
                """
                        {
                          "title": "High temperature detected",
                          "message": "Temperature exceeds safe operating range.",
                          "severity": "HIGH"
                        }
                        """,
                10,
                1_700_000_000_000L,
                2L);
    }

    private RuleEvaluationResult unmatchedResult() {
        return new RuleEvaluationResult(
                2L,
                "LOW_TEMP",
                "Low temperature",
                DecisionRuleConditionType.NUMBER,
                DecisionRuleOperator.LESS_THAN,
                "50",
                "95",
                false,
                DecisionRuleActionType.SUGGEST_ISSUE,
                null,
                20,
                1_700_000_000_000L,
                2L);
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
}
