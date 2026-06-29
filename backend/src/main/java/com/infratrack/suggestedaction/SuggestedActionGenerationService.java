package com.infratrack.suggestedaction;

import com.infratrack.ruleevaluation.RuleEvaluationReport;
import com.infratrack.ruleevaluation.RuleEvaluationResult;
import com.infratrack.ruleevaluation.RuleEvaluationStatus;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Generates read-only suggested actions from matched rule evaluation results (A3.4).
 */
@Service
public class SuggestedActionGenerationService {

    private final SuggestedActionRepository suggestedActionRepository;

    public SuggestedActionGenerationService(SuggestedActionRepository suggestedActionRepository) {
        this.suggestedActionRepository = suggestedActionRepository;
    }

    public List<SuggestedAction> generateFromReport(RuleEvaluationReport report) {
        if (report == null
                || report.getEvaluationStatus() != RuleEvaluationStatus.SUCCESS
                || report.getMatchedCount() == 0) {
            return List.of();
        }

        List<SuggestedAction> suggestions = new ArrayList<>();
        for (RuleEvaluationResult result : report.getResults()) {
            if (!result.isMatched()) {
                continue;
            }
            suggestions.add(toSuggestedAction(report, result));
        }

        if (suggestions.isEmpty()) {
            return List.of();
        }

        return suggestedActionRepository.saveAll(suggestions);
    }

    private static SuggestedAction toSuggestedAction(
            RuleEvaluationReport report,
            RuleEvaluationResult result) {
        SuggestedActionPayloadInterpreter.InterpretedPayload payload =
                SuggestedActionPayloadInterpreter.interpret(
                        result.getActionTypeSnapshot(),
                        result.getRuleNameSnapshot(),
                        result.getRuleCodeSnapshot(),
                        result.getActionPayloadSnapshot());

        return new SuggestedAction(
                report.getInspection(),
                report,
                result,
                result.getActionTypeSnapshot(),
                payload.title(),
                payload.message(),
                payload.severity(),
                payload.originalPayload(),
                1,
                result.getRuleCodeSnapshot(),
                SuggestionConfidenceCalculator.calculate(1, result.getActionTypeSnapshot()));
    }
}
