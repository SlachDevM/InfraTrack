import { describe, it, expect } from 'vitest';
import {
  filterAssetsByUserDepartment,
  resolveUserDepartmentId,
} from '../../utils/businessTriggerAssets';

describe('businessTriggerAssets', () => {
  const assets = [
    { id: 10, name: 'Central Playground', departmentId: 1, departmentName: 'Parks' },
    { id: 11, name: 'Other Asset', departmentId: 2, departmentName: 'Roads' },
    { id: 12, name: 'Nested Dept Asset', department: { id: '1', name: 'Parks' } },
  ];

  it('returns only assets from the user department', () => {
    const result = filterAssetsByUserDepartment(assets, 1);

    expect(result.map((asset) => asset.id)).toEqual([10, 12]);
  });

  it('handles string department ids consistently', () => {
    const result = filterAssetsByUserDepartment(assets, '1');

    expect(result.map((asset) => asset.id)).toEqual([10, 12]);
  });

  it('returns empty list when department is missing or no matches exist', () => {
    expect(filterAssetsByUserDepartment(assets, null)).toEqual([]);
    expect(filterAssetsByUserDepartment(assets, 99)).toEqual([]);
    expect(filterAssetsByUserDepartment([], 1)).toEqual([]);
  });

  it('prefers auth user department id over profile fallback', () => {
    expect(resolveUserDepartmentId({ departmentId: 1 }, { departmentId: 2 })).toBe(1);
    expect(resolveUserDepartmentId({}, { departmentId: 2 })).toBe(2);
    expect(resolveUserDepartmentId({}, {})).toBeNull();
  });
});
