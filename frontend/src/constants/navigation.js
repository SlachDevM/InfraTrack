import { USER_ROLES } from './userRoles';

export const FIELD_EMPLOYEE_ROUTES = new Set([
  '/',
  '/inspections',
  '/work-orders',
  '/notifications',
  '/assets',
]);

export const INSPECTION_TEMPLATES_ROUTE = '/inspection-templates';
export const PREVENTIVE_MAINTENANCE_PLANS_ROUTE = '/preventive-maintenance-plans';
export const PREVENTIVE_EXECUTION_CANDIDATES_ROUTE = '/preventive-execution-candidates';
export const PREVENTIVE_SCHEDULER_ROUTE = '/preventive-scheduler';

const INSPECTION_TEMPLATE_ROUTE_PREFIX = '/inspection-templates/';

const INSPECTION_TEMPLATE_VIEWER_ROLES = new Set([
  USER_ROLES.ADMINISTRATOR,
  USER_ROLES.MANAGER,
  USER_ROLES.OPERATIONAL_COORDINATOR,
]);

const PREVENTIVE_MAINTENANCE_PLAN_VIEWER_ROLES = INSPECTION_TEMPLATE_VIEWER_ROLES;
const PREVENTIVE_EXECUTION_CANDIDATE_VIEWER_ROLES = INSPECTION_TEMPLATE_VIEWER_ROLES;
const PREVENTIVE_SCHEDULER_VIEWER_ROLES = INSPECTION_TEMPLATE_VIEWER_ROLES;

export const APP_NAVIGATION_ITEMS = [
  { path: '/assets', label: 'Assets', fieldEmployeeLabel: 'Documents' },
  { path: '/business-triggers', label: 'Business Triggers' },
  { path: '/inspections', label: 'Inspections' },
  { path: '/inspection-templates', label: 'Inspection Templates' },
  { path: '/preventive-maintenance-plans', label: 'Preventive Maintenance Plans' },
  { path: '/preventive-execution-candidates', label: 'Preventive Execution Candidates' },
  { path: '/preventive-scheduler', label: 'Preventive Scheduler' },
  { path: '/issues', label: 'Issues' },
  { path: '/operational-decisions', label: 'Decisions' },
  { path: '/delegated-authorities', label: 'Delegations' },
  { path: '/work-orders', label: 'Work Orders' },
  { path: '/departments', label: 'Departments' },
  { path: '/asset-categories', label: 'Categories' },
];

function normalizeRole(role) {
  return role ? String(role).trim().toUpperCase() : '';
}

function normalizePath(path) {
  if (!path) {
    return '/';
  }

  const pathname = path.split('?')[0].replace(/\/+$/, '');
  return pathname || '/';
}

export function isFieldEmployeeRole(role) {
  return normalizeRole(role) === USER_ROLES.FIELD_EMPLOYEE;
}

export function canAccessRoute(role, path) {
  const normalizedPath = normalizePath(path);
  const normalizedRole = normalizeRole(role);

  if (normalizedPath === INSPECTION_TEMPLATES_ROUTE
      || normalizedPath.startsWith(INSPECTION_TEMPLATE_ROUTE_PREFIX)) {
    return INSPECTION_TEMPLATE_VIEWER_ROLES.has(normalizedRole);
  }

  if (normalizedPath === PREVENTIVE_MAINTENANCE_PLANS_ROUTE) {
    return PREVENTIVE_MAINTENANCE_PLAN_VIEWER_ROLES.has(normalizedRole);
  }

  if (normalizedPath === PREVENTIVE_EXECUTION_CANDIDATES_ROUTE) {
    return PREVENTIVE_EXECUTION_CANDIDATE_VIEWER_ROLES.has(normalizedRole);
  }

  if (normalizedPath === PREVENTIVE_SCHEDULER_ROUTE) {
    return PREVENTIVE_SCHEDULER_VIEWER_ROLES.has(normalizedRole);
  }

  if (!isFieldEmployeeRole(role)) {
    return true;
  }

  return FIELD_EMPLOYEE_ROUTES.has(normalizedPath);
}

export function getNavigationItems(role) {
  return APP_NAVIGATION_ITEMS
    .filter((item) => canAccessRoute(role, item.path))
    .map((item) => ({
      path: item.path,
      label: isFieldEmployeeRole(role) && item.fieldEmployeeLabel
        ? item.fieldEmployeeLabel
        : item.label,
    }));
}

export const FIELD_EMPLOYEE_SHORTCUTS = [
  { path: '/inspections', label: 'Assigned Inspections' },
  { path: '/work-orders', label: 'Assigned Work Orders' },
  { path: '/assets', label: 'Operational Documents' },
  { path: '/notifications', label: 'Notifications' },
];
