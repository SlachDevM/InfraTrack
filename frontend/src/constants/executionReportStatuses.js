export const EXECUTION_REPORT_STATUS = {
  GENERATED: 'GENERATED',
  UNDER_REVIEW: 'UNDER_REVIEW',
  APPROVED: 'APPROVED',
  REJECTED: 'REJECTED',
  DISMISSED: 'DISMISSED',
  INSPECTION_CREATED: 'INSPECTION_CREATED',
};

export const EXECUTION_REPORT_STATUS_OPTIONS = [
  { value: '', label: 'All statuses' },
  { value: EXECUTION_REPORT_STATUS.GENERATED, label: 'Generated' },
  { value: EXECUTION_REPORT_STATUS.UNDER_REVIEW, label: 'Under review' },
  { value: EXECUTION_REPORT_STATUS.APPROVED, label: 'Approved' },
  { value: EXECUTION_REPORT_STATUS.REJECTED, label: 'Rejected' },
  { value: EXECUTION_REPORT_STATUS.DISMISSED, label: 'Dismissed' },
  { value: EXECUTION_REPORT_STATUS.INSPECTION_CREATED, label: 'Inspection created' },
];

export function getExecutionReportStatusLabel(status) {
  const option = EXECUTION_REPORT_STATUS_OPTIONS.find((item) => item.value === status);
  return option?.label ?? status ?? '-';
}

export const DECISION_SOURCE = {
  MANUAL: 'MANUAL',
  RULE_ENGINE: 'RULE_ENGINE',
  PREVENTIVE_ENGINE: 'PREVENTIVE_ENGINE',
  SYSTEM: 'SYSTEM',
  EXTERNAL_API: 'EXTERNAL_API',
};

export function getDecisionSourceLabel(source) {
  switch (source) {
    case DECISION_SOURCE.PREVENTIVE_ENGINE:
      return 'Preventive engine';
    case DECISION_SOURCE.MANUAL:
      return 'Manual';
    case DECISION_SOURCE.RULE_ENGINE:
      return 'Rule engine';
    case DECISION_SOURCE.SYSTEM:
      return 'System';
    case DECISION_SOURCE.EXTERNAL_API:
      return 'External API';
    default:
      return source ?? '-';
  }
}
