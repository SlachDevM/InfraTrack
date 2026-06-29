export const PLAN_TRIGGER_TYPES = {
  TIME: 'TIME',
  METER: 'METER',
  EVENT: 'EVENT',
};

export const PLAN_TRIGGER_TYPE_LABELS = {
  [PLAN_TRIGGER_TYPES.TIME]: 'Time',
  [PLAN_TRIGGER_TYPES.METER]: 'Meter',
  [PLAN_TRIGGER_TYPES.EVENT]: 'Event',
};

export const PLAN_TRIGGER_TYPE_OPTIONS = Object.entries(PLAN_TRIGGER_TYPE_LABELS).map(
  ([value, label]) => ({ value, label })
);

export function getPlanTriggerTypeLabel(triggerType) {
  return PLAN_TRIGGER_TYPE_LABELS[triggerType] || triggerType;
}
