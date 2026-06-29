import { describe, it, expect, vi, beforeEach } from 'vitest';
import { render, screen, waitFor } from '@testing-library/react';
import RuleEvaluationReportPanel from '../../components/inspections/RuleEvaluationReportPanel';
import ruleEvaluationReportApi from '../../services/ruleEvaluationReportApi';

vi.mock('../../services/ruleEvaluationReportApi', () => ({
  default: {
    getLatest: vi.fn(),
  },
}));

describe('RuleEvaluationReportPanel', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('displays latest report summary and results table', async () => {
    ruleEvaluationReportApi.getLatest.mockResolvedValue({
      id: 1,
      inspectionId: 50,
      evaluatedAt: 1_700_000_000_000,
      engineVersion: 'A3.3-1.0',
      evaluationDurationMs: 12,
      resultCount: 2,
      matchedCount: 1,
      results: [
        {
          id: 10,
          ruleCodeSnapshot: 'HIGH_TEMP',
          ruleNameSnapshot: 'High temperature',
          actualValueSnapshot: '95',
          operatorSnapshot: 'GREATER_THAN',
          comparisonValueSnapshot: '90',
          matched: true,
          actionTypeSnapshot: 'SUGGEST_ISSUE',
          prioritySnapshot: 10,
        },
        {
          id: 11,
          ruleCodeSnapshot: 'LOW_TEMP',
          ruleNameSnapshot: 'Low temperature',
          actualValueSnapshot: '95',
          operatorSnapshot: 'LESS_THAN',
          comparisonValueSnapshot: '50',
          matched: false,
          actionTypeSnapshot: 'FLAG_FOR_REVIEW',
          prioritySnapshot: 20,
        },
      ],
    });

    render(<RuleEvaluationReportPanel inspectionId={50} assetName="Pump Station" />);

    await waitFor(() => {
      expect(screen.getByTestId('rule-report-panel-50')).toBeInTheDocument();
    });

    expect(screen.getByText('A3.3-1.0')).toBeInTheDocument();
    expect(screen.getByText('HIGH_TEMP')).toBeInTheDocument();
    expect(screen.getByText('LOW_TEMP')).toBeInTheDocument();
    expect(screen.getByTestId('rule-result-HIGH_TEMP')).toHaveTextContent('Yes');
    expect(screen.getByTestId('rule-result-LOW_TEMP')).toHaveTextContent('No');
    expect(screen.queryByRole('button', { name: /suggest/i })).not.toBeInTheDocument();
    expect(screen.queryByRole('button', { name: /action/i })).not.toBeInTheDocument();
  });

  it('renders nothing when no report exists', async () => {
    ruleEvaluationReportApi.getLatest.mockRejectedValue({ response: { status: 404 } });

    const { container } = render(<RuleEvaluationReportPanel inspectionId={99} />);

    await waitFor(() => {
      expect(ruleEvaluationReportApi.getLatest).toHaveBeenCalledWith(99);
    });

    expect(container).toBeEmptyDOMElement();
  });

  it('shows empty results note when report has zero evaluated rules', async () => {
    ruleEvaluationReportApi.getLatest.mockResolvedValue({
      id: 2,
      inspectionId: 60,
      evaluatedAt: 1_700_000_000_000,
      engineVersion: 'A3.3-1.0',
      evaluationDurationMs: 1,
      resultCount: 0,
      matchedCount: 0,
      results: [],
    });

    render(<RuleEvaluationReportPanel inspectionId={60} />);

    await waitFor(() => {
      expect(screen.getByText(/No active rules were evaluated/i)).toBeInTheDocument();
    });
  });
});
