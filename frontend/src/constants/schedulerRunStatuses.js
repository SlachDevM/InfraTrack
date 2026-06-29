export const SCHEDULER_RUN_STATUS = {
  SUCCESS: 'SUCCESS',
  PARTIAL: 'PARTIAL',
  FAILED: 'FAILED',
};

export function getSchedulerRunStatusLabel(status) {
  switch (status) {
    case SCHEDULER_RUN_STATUS.SUCCESS:
      return 'Success';
    case SCHEDULER_RUN_STATUS.PARTIAL:
      return 'Partial';
    case SCHEDULER_RUN_STATUS.FAILED:
      return 'Failed';
    default:
      return status ?? '-';
  }
}

export const SCHEDULER_TRIGGERED_BY = {
  MANUAL: 'MANUAL',
  SCHEDULED: 'SCHEDULED',
};

export function getSchedulerTriggeredByLabel(triggeredBy) {
  switch (triggeredBy) {
    case SCHEDULER_TRIGGERED_BY.MANUAL:
      return 'Manual';
    case SCHEDULER_TRIGGERED_BY.SCHEDULED:
      return 'Scheduled';
    default:
      return triggeredBy ?? '-';
  }
}
