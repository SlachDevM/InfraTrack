import { WORK_ORDER_PRIORITIES } from '../../constants/workOrderPriorities';
import { toDateTimeLocalValue } from '../../utils/dateTime';

export { toDateTimeLocalValue };

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
