export const PLAN_METER_TYPES = {
  OPERATING_HOURS: 'OPERATING_HOURS',
};

export const PLAN_METER_TYPE_LABELS = {
  [PLAN_METER_TYPES.OPERATING_HOURS]: 'Operating Hours',
};

export const PLAN_METER_TYPE_OPTIONS = Object.entries(PLAN_METER_TYPE_LABELS).map(
  ([value, label]) => ({ value, label })
);
