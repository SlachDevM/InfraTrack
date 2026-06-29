export const EXECUTION_CANDIDATE_STATUS_OPTIONS = [
  { value: '', label: 'All statuses' },
  { value: 'PENDING', label: 'Pending' },
  { value: 'APPROVED', label: 'Approved' },
  { value: 'REJECTED', label: 'Rejected' },
  { value: 'DISMISSED', label: 'Dismissed' },
  { value: 'EXECUTED', label: 'Executed' },
];

const STATUS_LABELS = {
  PENDING: 'Pending',
  APPROVED: 'Approved',
  REJECTED: 'Rejected',
  DISMISSED: 'Dismissed',
  EXECUTED: 'Executed',
};

export function getExecutionCandidateStatusLabel(status) {
  return STATUS_LABELS[status] || status;
}
