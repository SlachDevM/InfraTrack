import { describe, it, expect } from 'vitest';
import { filterInspectionAssignees } from '../../utils/inspectionAssignees';

describe('filterInspectionAssignees', () => {
  const workers = [
    { id: 1, name: 'Alex Field', role: 'FIELD_EMPLOYEE', status: 'ACTIVE', departmentId: 1 },
    { id: 2, name: 'Disabled Field', role: 'FIELD_EMPLOYEE', status: 'DISABLED', departmentId: 1 },
    { id: 3, name: 'Other Dept', role: 'FIELD_EMPLOYEE', status: 'ACTIVE', departmentId: 2 },
    { id: 4, name: 'Contractor', role: 'CONTRACTOR', status: 'ACTIVE', departmentId: 1 },
    { id: 5, name: 'Manager', role: 'MANAGER', status: 'ACTIVE', departmentId: 1 },
  ];

  it('returns only active field employees from the same department', () => {
    const result = filterInspectionAssignees(workers, 1);

    expect(result).toHaveLength(1);
    expect(result[0].name).toBe('Alex Field');
  });

  it('excludes workers from other departments', () => {
    const result = filterInspectionAssignees(workers, 1);

    expect(result.map((worker) => worker.id)).not.toContain(3);
  });

  it('returns empty list when no eligible assignees exist', () => {
    expect(filterInspectionAssignees(workers, 99)).toEqual([]);
    expect(filterInspectionAssignees([], 1)).toEqual([]);
    expect(filterInspectionAssignees(workers, null)).toEqual([]);
  });
});
