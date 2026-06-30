import { describe, it, expect } from 'vitest';
import {
  APP_NAVIGATION_ITEMS,
  canAccessRoute,
  getNavigationItems,
  getOverflowNavigationItems,
  getPrimaryNavigationItems,
  isFieldEmployeeRole,
} from '../../constants/navigation';
import { USER_ROLES } from '../../constants/userRoles';

describe('isFieldEmployeeRole', () => {
  it('identifies field employee role only', () => {
    expect(isFieldEmployeeRole(USER_ROLES.FIELD_EMPLOYEE)).toBe(true);
    expect(isFieldEmployeeRole(USER_ROLES.MANAGER)).toBe(false);
    expect(isFieldEmployeeRole(USER_ROLES.CONTRACTOR)).toBe(false);
  });
});

describe('canAccessRoute', () => {
  it('allows field employees only operational routes', () => {
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/')).toBe(true);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/inspections')).toBe(true);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/work-orders')).toBe(true);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/notifications')).toBe(true);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/assets')).toBe(true);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/departments')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/users')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/issues')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/operational-decisions')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/delegated-authorities')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/business-triggers')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/asset-categories')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/inspection-templates')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/preventive-maintenance-plans')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/preventive-execution-candidates')).toBe(false);
    expect(canAccessRoute(USER_ROLES.FIELD_EMPLOYEE, '/preventive-scheduler')).toBe(false);
  });

  it('blocks contractor from inspection templates', () => {
    expect(canAccessRoute(USER_ROLES.CONTRACTOR, '/inspection-templates')).toBe(false);
    expect(canAccessRoute(USER_ROLES.CONTRACTOR, '/inspection-templates/100/questions')).toBe(false);
    expect(canAccessRoute(USER_ROLES.CONTRACTOR, '/inspections')).toBe(true);
  });

  it('allows manager to access template question routes', () => {
    expect(canAccessRoute(USER_ROLES.MANAGER, '/inspection-templates/100/questions')).toBe(true);
  });

  it('does not restrict manager navigation', () => {
    APP_NAVIGATION_ITEMS.forEach((item) => {
      expect(canAccessRoute(USER_ROLES.MANAGER, item.path)).toBe(true);
    });
    expect(canAccessRoute(USER_ROLES.MANAGER, '/users')).toBe(true);
  });

  it('does not restrict operational coordinator navigation', () => {
    APP_NAVIGATION_ITEMS.forEach((item) => {
      expect(canAccessRoute(USER_ROLES.OPERATIONAL_COORDINATOR, item.path)).toBe(true);
    });
  });

  it('does not restrict administrator navigation', () => {
    expect(canAccessRoute(USER_ROLES.ADMINISTRATOR, '/users')).toBe(true);
    expect(canAccessRoute(USER_ROLES.ADMINISTRATOR, '/departments')).toBe(true);
  });
});

describe('getNavigationItems', () => {
  it('returns only operational pages for field employees', () => {
    const labels = getNavigationItems(USER_ROLES.FIELD_EMPLOYEE).map((item) => item.label);

    expect(labels).toEqual(['Documents', 'Inspections', 'Work Orders']);
    expect(labels).not.toContain('Departments');
    expect(labels).not.toContain('Issues');
    expect(labels).not.toContain('Business Triggers');
  });

  it('returns full navigation for managers', () => {
    const labels = getNavigationItems(USER_ROLES.MANAGER).map((item) => item.label);

    expect(labels).toContain('Assets');
    expect(labels).toContain('Departments');
    expect(labels).toContain('Issues');
    expect(labels).toContain('Decisions');
    expect(labels).toContain('Delegations');
    expect(labels).toContain('Inspection Templates');
    expect(labels).toContain('Preventive Maintenance Plans');
    expect(labels).toContain('Preventive Execution Candidates');
  });

  it('returns full navigation for operational coordinators', () => {
    expect(getNavigationItems(USER_ROLES.OPERATIONAL_COORDINATOR)).toHaveLength(
      APP_NAVIGATION_ITEMS.length
    );
  });

  it('splits manager navigation into primary and overflow groups', () => {
    const primary = getPrimaryNavigationItems(USER_ROLES.MANAGER).map((item) => item.label);
    const overflow = getOverflowNavigationItems(USER_ROLES.MANAGER).map((item) => item.label);

    expect(primary).toEqual(['Assets', 'Inspections', 'Issues', 'Work Orders']);
    expect(overflow).toContain('Departments');
    expect(overflow).toContain('Inspection Templates');
    expect(overflow).not.toContain('Assets');
  });
});
