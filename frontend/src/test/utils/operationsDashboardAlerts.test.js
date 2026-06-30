import { describe, it, expect } from 'vitest';
import { buildAttentionAlerts } from '../../utils/operationsDashboardAlerts';

const emptyKpis = {
  assets: { totalAssets: 0 },
  inspections: { overdueInspections: 0 },
  issues: { openIssues: 0, issuesBySeverity: {} },
  preventive: { pendingExecutionCandidates: 0 },
  decisionEngine: { suggestedActionsPending: 0 },
};

describe('buildAttentionAlerts', () => {
  it('returns no alerts when all relevant KPI values are zero', () => {
    expect(buildAttentionAlerts(emptyKpis)).toEqual([]);
  });

  it('returns alerts for non-zero KPI values', () => {
    const alerts = buildAttentionAlerts({
      ...emptyKpis,
      inspections: { overdueInspections: 2 },
      issues: { openIssues: 3, issuesBySeverity: { HIGH: 1, CRITICAL: 1 } },
      preventive: { pendingExecutionCandidates: 1 },
      decisionEngine: { suggestedActionsPending: 4 },
    });

    expect(alerts.map((alert) => alert.id)).toEqual([
      'overdueInspections',
      'openIssues',
      'highSeverityIssues',
      'pendingExecutionCandidates',
      'suggestedActionsPending',
    ]);
  });
});
