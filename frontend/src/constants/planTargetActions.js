export const PLAN_TARGET_ACTIONS = {
  CREATE_INSPECTION: 'CREATE_INSPECTION',
  CREATE_WORK_ORDER: 'CREATE_WORK_ORDER',
  CREATE_MAINTENANCE: 'CREATE_MAINTENANCE',
};

export const PLAN_TARGET_ACTION_LABELS = {
  [PLAN_TARGET_ACTIONS.CREATE_INSPECTION]: 'Create Inspection',
  [PLAN_TARGET_ACTIONS.CREATE_WORK_ORDER]: 'Create Work Order',
  [PLAN_TARGET_ACTIONS.CREATE_MAINTENANCE]: 'Create Maintenance',
};

export const PLAN_TARGET_ACTION_OPTIONS = Object.entries(PLAN_TARGET_ACTION_LABELS).map(
  ([value, label]) => ({ value, label })
);

export function getPlanTargetActionLabel(targetAction) {
  return PLAN_TARGET_ACTION_LABELS[targetAction] || targetAction;
}
