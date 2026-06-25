export const ISSUE_SEVERITIES = {
  LOW: 'LOW',
  MEDIUM: 'MEDIUM',
  HIGH: 'HIGH',
  CRITICAL: 'CRITICAL',
};

export const ISSUE_SEVERITY_LABELS = {
  [ISSUE_SEVERITIES.LOW]: 'Low',
  [ISSUE_SEVERITIES.MEDIUM]: 'Medium',
  [ISSUE_SEVERITIES.HIGH]: 'High',
  [ISSUE_SEVERITIES.CRITICAL]: 'Critical',
};

export const ISSUE_SEVERITY_OPTIONS = Object.entries(ISSUE_SEVERITY_LABELS).map(
  ([value, label]) => ({ value, label })
);

export function getIssueSeverityLabel(severity) {
  return ISSUE_SEVERITY_LABELS[severity] || severity;
}
