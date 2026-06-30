export const ATTENTION_ALERT_MESSAGES = {
  overdueInspections: 'Overdue inspections require attention.',
  openIssues: 'Open issues are waiting for review.',
  highSeverityIssues: 'Critical or high severity issues require attention.',
  pendingExecutionCandidates: 'Preventive candidates are waiting for decision.',
  suggestedActionsPending: 'Suggested actions are waiting for manager review.',
};

export function buildAttentionAlerts(kpis) {
  if (!kpis) {
    return [];
  }

  const alerts = [];

  if ((kpis.inspections?.overdueInspections ?? 0) > 0) {
    alerts.push({
      id: 'overdueInspections',
      message: ATTENTION_ALERT_MESSAGES.overdueInspections,
    });
  }

  if ((kpis.issues?.openIssues ?? 0) > 0) {
    alerts.push({
      id: 'openIssues',
      message: ATTENTION_ALERT_MESSAGES.openIssues,
    });
  }

  const severity = kpis.issues?.issuesBySeverity ?? {};
  const highSeverityCount = (severity.HIGH ?? 0) + (severity.CRITICAL ?? 0);
  if (highSeverityCount > 0) {
    alerts.push({
      id: 'highSeverityIssues',
      message: ATTENTION_ALERT_MESSAGES.highSeverityIssues,
    });
  }

  if ((kpis.preventive?.pendingExecutionCandidates ?? 0) > 0) {
    alerts.push({
      id: 'pendingExecutionCandidates',
      message: ATTENTION_ALERT_MESSAGES.pendingExecutionCandidates,
    });
  }

  if ((kpis.decisionEngine?.suggestedActionsPending ?? 0) > 0) {
    alerts.push({
      id: 'suggestedActionsPending',
      message: ATTENTION_ALERT_MESSAGES.suggestedActionsPending,
    });
  }

  return alerts;
}
