export const PHYSICAL_CONDITIONS = {
  EXCELLENT: 'EXCELLENT',
  GOOD: 'GOOD',
  FAIR: 'FAIR',
  POOR: 'POOR',
  CRITICAL: 'CRITICAL',
};

export const PHYSICAL_CONDITION_LABELS = {
  [PHYSICAL_CONDITIONS.EXCELLENT]: 'Excellent',
  [PHYSICAL_CONDITIONS.GOOD]: 'Good',
  [PHYSICAL_CONDITIONS.FAIR]: 'Fair',
  [PHYSICAL_CONDITIONS.POOR]: 'Poor',
  [PHYSICAL_CONDITIONS.CRITICAL]: 'Critical',
};

export const PHYSICAL_CONDITION_OPTIONS = Object.entries(PHYSICAL_CONDITION_LABELS).map(
  ([value, label]) => ({ value, label })
);

export function getPhysicalConditionLabel(condition) {
  return PHYSICAL_CONDITION_LABELS[condition] || condition;
}
