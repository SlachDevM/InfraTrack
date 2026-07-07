export const WORK_ORDER_STATUSES = {
  CREATED: 'CREATED',
  ASSIGNED: 'ASSIGNED',
  COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED',
};

export const WORK_ORDER_STATUS_LABELS = {
  [WORK_ORDER_STATUSES.CREATED]: 'Created',
  [WORK_ORDER_STATUSES.ASSIGNED]: 'Assigned',
  [WORK_ORDER_STATUSES.COMPLETED]: 'Completed',
  [WORK_ORDER_STATUSES.CANCELLED]: 'Cancelled',
};

export function getWorkOrderStatusLabel(status) {
  return WORK_ORDER_STATUS_LABELS[status] || status;
}
