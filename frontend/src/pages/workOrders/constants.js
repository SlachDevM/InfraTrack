import { WORK_ORDER_PRIORITIES } from '../../constants/workOrderPriorities';

export function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

export function createInitialFormData() {
  return {
    operationalDecisionId: '',
    description: '',
    priority: WORK_ORDER_PRIORITIES.NORMAL,
    createdAtBusinessDate: toDateTimeLocalValue(),
  };
}

export function createInitialAssignFormData() {
  return {
    workOrderId: '',
    assignedToUserId: '',
    assignedAt: toDateTimeLocalValue(),
  };
}

export function createInitialCompleteFormData() {
  return {
    workOrderId: '',
    completionNotes: '',
    completedAt: toDateTimeLocalValue(),
  };
}

export function createInitialReviewFormData() {
  return {
    maintenanceActivityId: '',
    decision: 'APPROVED',
    reviewNotes: '',
    reviewedAt: toDateTimeLocalValue(),
    reworkSeverity: 'MEDIUM',
    rootCause: '',
    correctiveAction: '',
    preventiveAction: '',
  };
}
