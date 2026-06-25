export const COMPLETION_REVIEW_DECISIONS = {
  APPROVED: 'APPROVED',
  REWORK_REQUIRED: 'REWORK_REQUIRED',
};

export const COMPLETION_REVIEW_DECISION_OPTIONS = [
  { value: COMPLETION_REVIEW_DECISIONS.APPROVED, label: 'Approved' },
  { value: COMPLETION_REVIEW_DECISIONS.REWORK_REQUIRED, label: 'Rework Required' },
];

export function getCompletionReviewDecisionLabel(decision) {
  const option = COMPLETION_REVIEW_DECISION_OPTIONS.find((item) => item.value === decision);
  return option ? option.label : decision;
}
