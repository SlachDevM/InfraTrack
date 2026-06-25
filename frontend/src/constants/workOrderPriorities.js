export const WORK_ORDER_PRIORITIES = {
  LOW: 'LOW',
  NORMAL: 'NORMAL',
  HIGH: 'HIGH',
  URGENT: 'URGENT',
};

export const WORK_ORDER_PRIORITY_LABELS = {
  [WORK_ORDER_PRIORITIES.LOW]: 'Low',
  [WORK_ORDER_PRIORITIES.NORMAL]: 'Normal',
  [WORK_ORDER_PRIORITIES.HIGH]: 'High',
  [WORK_ORDER_PRIORITIES.URGENT]: 'Urgent',
};

export const WORK_ORDER_PRIORITY_OPTIONS = Object.entries(WORK_ORDER_PRIORITY_LABELS).map(
  ([value, label]) => ({ value, label })
);

export function getWorkOrderPriorityLabel(priority) {
  return WORK_ORDER_PRIORITY_LABELS[priority] || priority;
}
