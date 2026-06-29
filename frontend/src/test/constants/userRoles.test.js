import { describe, it, expect } from 'vitest';
import {
  USER_ROLES,
  canRegisterAssets,
  canUploadOperationalDocuments,
  canAssignInspections,
  canPerformInspections,
  canCreateWorkOrders,
  canManageUsers,
  canViewInspectionTemplates,
  canManageInspectionTemplates,
  canViewPreventiveMaintenancePlans,
  canManagePreventiveMaintenancePlans,
} from '../../constants/userRoles';

describe('canRegisterAssets', () => {
  it('denies Administrator', () => {
    expect(canRegisterAssets(USER_ROLES.ADMINISTRATOR)).toBe(false);
  });

  it('allows Manager and Operational Coordinator', () => {
    expect(canRegisterAssets(USER_ROLES.MANAGER)).toBe(true);
    expect(canRegisterAssets(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(true);
  });

  it('denies Field Employee and Contractor', () => {
    expect(canRegisterAssets(USER_ROLES.FIELD_EMPLOYEE)).toBe(false);
    expect(canRegisterAssets(USER_ROLES.CONTRACTOR)).toBe(false);
  });
});

describe('canUploadOperationalDocuments', () => {
  it('denies Administrator', () => {
    expect(canUploadOperationalDocuments(USER_ROLES.ADMINISTRATOR)).toBe(false);
  });

  it('allows Manager and Operational Coordinator', () => {
    expect(canUploadOperationalDocuments(USER_ROLES.MANAGER)).toBe(true);
    expect(canUploadOperationalDocuments(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(true);
  });

  it('allows Field Employee and Contractor', () => {
    expect(canUploadOperationalDocuments(USER_ROLES.FIELD_EMPLOYEE)).toBe(true);
    expect(canUploadOperationalDocuments(USER_ROLES.CONTRACTOR)).toBe(true);
  });
});

describe('other role helpers', () => {
  it('restricts inspection assignment to Operational Coordinator', () => {
    expect(canAssignInspections(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(true);
    expect(canAssignInspections(USER_ROLES.MANAGER)).toBe(false);
  });

  it('restricts inspection performance to field roles', () => {
    expect(canPerformInspections(USER_ROLES.FIELD_EMPLOYEE)).toBe(true);
    expect(canPerformInspections(USER_ROLES.CONTRACTOR)).toBe(true);
    expect(canPerformInspections(USER_ROLES.MANAGER)).toBe(false);
  });

  it('restricts work order creation to Operational Coordinator', () => {
    expect(canCreateWorkOrders(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(true);
    expect(canCreateWorkOrders(USER_ROLES.FIELD_EMPLOYEE)).toBe(false);
  });

  it('restricts user management to Administrator', () => {
    expect(canManageUsers(USER_ROLES.ADMINISTRATOR)).toBe(true);
    expect(canManageUsers(USER_ROLES.MANAGER)).toBe(false);
  });

  it('allows inspection template viewing for admin manager and coordinator', () => {
    expect(canViewInspectionTemplates(USER_ROLES.ADMINISTRATOR)).toBe(true);
    expect(canViewInspectionTemplates(USER_ROLES.MANAGER)).toBe(true);
    expect(canViewInspectionTemplates(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(true);
    expect(canViewInspectionTemplates(USER_ROLES.FIELD_EMPLOYEE)).toBe(false);
    expect(canViewInspectionTemplates(USER_ROLES.CONTRACTOR)).toBe(false);
  });

  it('restricts inspection template management to Administrator', () => {
    expect(canManageInspectionTemplates(USER_ROLES.ADMINISTRATOR)).toBe(true);
    expect(canManageInspectionTemplates(USER_ROLES.MANAGER)).toBe(false);
    expect(canManageInspectionTemplates(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(false);
  });

  it('allows preventive maintenance plan viewing for admin manager and coordinator', () => {
    expect(canViewPreventiveMaintenancePlans(USER_ROLES.ADMINISTRATOR)).toBe(true);
    expect(canViewPreventiveMaintenancePlans(USER_ROLES.MANAGER)).toBe(true);
    expect(canViewPreventiveMaintenancePlans(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(true);
    expect(canViewPreventiveMaintenancePlans(USER_ROLES.FIELD_EMPLOYEE)).toBe(false);
    expect(canViewPreventiveMaintenancePlans(USER_ROLES.CONTRACTOR)).toBe(false);
  });

  it('restricts preventive maintenance plan management to Administrator', () => {
    expect(canManagePreventiveMaintenancePlans(USER_ROLES.ADMINISTRATOR)).toBe(true);
    expect(canManagePreventiveMaintenancePlans(USER_ROLES.MANAGER)).toBe(false);
    expect(canManagePreventiveMaintenancePlans(USER_ROLES.OPERATIONAL_COORDINATOR)).toBe(false);
  });
});
