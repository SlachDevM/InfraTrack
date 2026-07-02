import { USER_ROLES, canViewOperationsDashboard } from './userRoles';
import {
  ROUTES,
  DASHBOARD_ROUTE,
  INSPECTION_TEMPLATES_ROUTE,
  PREVENTIVE_MAINTENANCE_PLANS_ROUTE,
  PREVENTIVE_EXECUTION_CANDIDATES_ROUTE,
  PREVENTIVE_SCHEDULER_ROUTE,
  INSPECTION_TEMPLATE_ROUTE_PREFIX,
} from './routes';

export {
  DASHBOARD_ROUTE,
  INSPECTION_TEMPLATES_ROUTE,
  PREVENTIVE_MAINTENANCE_PLANS_ROUTE,
  PREVENTIVE_EXECUTION_CANDIDATES_ROUTE,
  PREVENTIVE_SCHEDULER_ROUTE,
};

export const FIELD_EMPLOYEE_ROUTES = new Set([
  ROUTES.HOME,
  ROUTES.INSPECTIONS,
  ROUTES.WORK_ORDERS,
  ROUTES.NOTIFICATIONS,
  ROUTES.ASSETS,
]);

const INSPECTION_TEMPLATE_VIEWER_ROLES = new Set([
  USER_ROLES.ADMINISTRATOR,
  USER_ROLES.MANAGER,
  USER_ROLES.OPERATIONAL_COORDINATOR,
]);

const PREVENTIVE_MAINTENANCE_PLAN_VIEWER_ROLES = INSPECTION_TEMPLATE_VIEWER_ROLES;
const PREVENTIVE_EXECUTION_CANDIDATE_VIEWER_ROLES = INSPECTION_TEMPLATE_VIEWER_ROLES;
const PREVENTIVE_SCHEDULER_VIEWER_ROLES = INSPECTION_TEMPLATE_VIEWER_ROLES;

export const PRIMARY_NAVIGATION_PATHS = new Set([
  ROUTES.DASHBOARD,
  ROUTES.ASSETS,
  ROUTES.INSPECTIONS,
  ROUTES.WORK_ORDERS,
  ROUTES.ISSUES,
]);

export const APP_NAVIGATION_ITEMS = [
  { path: ROUTES.DASHBOARD, label: 'Dashboard', dashboardOnly: true },
  { path: ROUTES.ASSETS, label: 'Assets', fieldEmployeeLabel: 'Documents' },
  { path: ROUTES.BUSINESS_TRIGGERS, label: 'Business Triggers' },
  { path: ROUTES.INSPECTIONS, label: 'Inspections' },
  { path: ROUTES.INSPECTION_TEMPLATES, label: 'Inspection Templates' },
  { path: ROUTES.PREVENTIVE_PLANS, label: 'Preventive Maintenance Plans' },
  { path: ROUTES.PREVENTIVE_CANDIDATES, label: 'Preventive Execution Candidates' },
  { path: ROUTES.PREVENTIVE_SCHEDULER, label: 'Preventive Scheduler' },
  { path: ROUTES.ISSUES, label: 'Issues' },
  { path: ROUTES.OPERATIONAL_DECISIONS, label: 'Decisions' },
  { path: ROUTES.DELEGATED_AUTHORITIES, label: 'Delegations' },
  { path: ROUTES.WORK_ORDERS, label: 'Work Orders' },
  { path: ROUTES.DEPARTMENTS, label: 'Departments' },
  { path: ROUTES.ASSET_CATEGORIES, label: 'Categories' },
];

function normalizeRole(role) {
  return role ? String(role).trim().toUpperCase() : '';
}

function normalizePath(path) {
  if (!path) {
    return ROUTES.HOME;
  }

  const pathname = path.split('?')[0].replace(/\/+$/, '');
  return pathname || ROUTES.HOME;
}

export function isFieldEmployeeRole(role) {
  return normalizeRole(role) === USER_ROLES.FIELD_EMPLOYEE;
}

export function canAccessRoute(role, path) {
  const normalizedPath = normalizePath(path);
  const normalizedRole = normalizeRole(role);

  if (normalizedPath === DASHBOARD_ROUTE) {
    return canViewOperationsDashboard(normalizedRole);
  }

  if (
    normalizedPath === INSPECTION_TEMPLATES_ROUTE ||
    normalizedPath.startsWith(INSPECTION_TEMPLATE_ROUTE_PREFIX)
  ) {
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
  return APP_NAVIGATION_ITEMS.filter((item) => {
    if (item.dashboardOnly && !canViewOperationsDashboard(role)) {
      return false;
    }
    return canAccessRoute(role, item.path);
  }).map((item) => ({
    path: item.path,
    label:
      isFieldEmployeeRole(role) && item.fieldEmployeeLabel ? item.fieldEmployeeLabel : item.label,
  }));
}

export function getPrimaryNavigationItems(role) {
  return getNavigationItems(role).filter((item) => PRIMARY_NAVIGATION_PATHS.has(item.path));
}

export function getOverflowNavigationItems(role) {
  return getNavigationItems(role).filter((item) => !PRIMARY_NAVIGATION_PATHS.has(item.path));
}

export const FIELD_EMPLOYEE_SHORTCUTS = [
  { path: ROUTES.INSPECTIONS, label: 'Assigned Inspections' },
  { path: ROUTES.WORK_ORDERS, label: 'Assigned Work Orders' },
  { path: ROUTES.ASSETS, label: 'Operational Documents' },
  { path: ROUTES.NOTIFICATIONS, label: 'Notifications' },
];
