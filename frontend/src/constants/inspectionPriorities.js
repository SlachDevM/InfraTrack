export const INSPECTION_PRIORITIES = {
  LOW: 'LOW',
  NORMAL: 'NORMAL',
  HIGH: 'HIGH',
  URGENT: 'URGENT',
};

export const INSPECTION_PRIORITY_LABELS = {
  [INSPECTION_PRIORITIES.LOW]: 'Low',
  [INSPECTION_PRIORITIES.NORMAL]: 'Normal',
  [INSPECTION_PRIORITIES.HIGH]: 'High',
  [INSPECTION_PRIORITIES.URGENT]: 'Urgent',
};

export const INSPECTION_PRIORITY_OPTIONS = Object.entries(INSPECTION_PRIORITY_LABELS).map(
  ([value, label]) => ({ value, label })
);

export const INSPECTION_STATUSES = {
  ASSIGNED: 'ASSIGNED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
};

export function getInspectionPriorityLabel(priority) {
  return INSPECTION_PRIORITY_LABELS[priority] || priority;
}
