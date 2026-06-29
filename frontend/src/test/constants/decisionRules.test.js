import { describe, it, expect } from 'vitest';
import {
  comparisonValueRequired,
  getOperatorsForConditionType,
  validateActionPayloadJson,
} from '../../constants/decisionRules';

describe('decisionRules constants', () => {
  it('returns boolean operators only for BOOLEAN condition type', () => {
    const operators = getOperatorsForConditionType('BOOLEAN');
    expect(operators.map((item) => item.value)).toEqual(['IS_TRUE', 'IS_FALSE']);
  });

  it('returns numeric operators for NUMBER condition type', () => {
    const operators = getOperatorsForConditionType('NUMBER');
    expect(operators.map((item) => item.value)).toContain('GREATER_THAN');
    expect(operators.map((item) => item.value)).not.toContain('IS_TRUE');
  });

  it('requires comparison value except for BOOLEAN', () => {
    expect(comparisonValueRequired('BOOLEAN')).toBe(false);
    expect(comparisonValueRequired('NUMBER')).toBe(true);
    expect(comparisonValueRequired('CHOICE')).toBe(true);
    expect(comparisonValueRequired('TEXT')).toBe(true);
  });

  it('validates action payload JSON', () => {
    expect(validateActionPayloadJson('')).toBeNull();
    expect(validateActionPayloadJson('{"severity":"HIGH"}')).toBeNull();
    expect(validateActionPayloadJson('{invalid')).toMatch(/valid JSON/i);
  });
});
