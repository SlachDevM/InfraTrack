export const PLAN_TIME_UNITS = {
  DAY: 'DAY',
  WEEK: 'WEEK',
  MONTH: 'MONTH',
  YEAR: 'YEAR',
};

export const PLAN_TIME_UNIT_LABELS = {
  [PLAN_TIME_UNITS.DAY]: 'Day',
  [PLAN_TIME_UNITS.WEEK]: 'Week',
  [PLAN_TIME_UNITS.MONTH]: 'Month',
  [PLAN_TIME_UNITS.YEAR]: 'Year',
};

export const PLAN_TIME_UNIT_OPTIONS = Object.entries(PLAN_TIME_UNIT_LABELS).map(
  ([value, label]) => ({ value, label })
);
