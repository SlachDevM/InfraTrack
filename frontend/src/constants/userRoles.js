export const USER_ROLES = {
  ADMINISTRATOR: 'ADMINISTRATOR',
  MANAGER: 'MANAGER',
  OPERATIONAL_COORDINATOR: 'OPERATIONAL_COORDINATOR',
  FIELD_EMPLOYEE: 'FIELD_EMPLOYEE',
  CONTRACTOR: 'CONTRACTOR',
};

export const ROLE_LABELS = {
  [USER_ROLES.ADMINISTRATOR]: 'Administrator',
  [USER_ROLES.MANAGER]: 'Manager',
  [USER_ROLES.OPERATIONAL_COORDINATOR]: 'Operational Coordinator',
  [USER_ROLES.FIELD_EMPLOYEE]: 'Field Employee',
  [USER_ROLES.CONTRACTOR]: 'Contractor',
};

export const INVITABLE_ROLES = [
  USER_ROLES.MANAGER,
  USER_ROLES.OPERATIONAL_COORDINATOR,
  USER_ROLES.FIELD_EMPLOYEE,
  USER_ROLES.CONTRACTOR,
];

export function canManageUsers(role) {
  return role === USER_ROLES.ADMINISTRATOR;
}

export function canRegisterAssets(role) {
  return role === USER_ROLES.MANAGER || role === USER_ROLES.OPERATIONAL_COORDINATOR;
}

export function canCreateBusinessTriggers(role) {
  return canRegisterAssets(role);
}

export function canAssignInspections(role) {
  return role === USER_ROLES.OPERATIONAL_COORDINATOR;
}

export function canPerformInspections(role) {
  return role === USER_ROLES.FIELD_EMPLOYEE || role === USER_ROLES.CONTRACTOR;
}

export function canRecordIssues(role) {
  return canPerformInspections(role);
}

export function canMakeOperationalDecisions(role) {
  return role === USER_ROLES.MANAGER;
}

export function canCreateWorkOrders(role) {
  return role === USER_ROLES.OPERATIONAL_COORDINATOR;
}

export function canAssignWorkOrders(role) {
  return role === USER_ROLES.OPERATIONAL_COORDINATOR;
}

export function canCompleteMaintenance(role) {
  return role === USER_ROLES.FIELD_EMPLOYEE || role === USER_ROLES.CONTRACTOR;
}

export function canRecordCompletionReview(role) {
  return role === USER_ROLES.MANAGER;
}

export function getRoleLabel(role) {
  return ROLE_LABELS[role] || role;
}
