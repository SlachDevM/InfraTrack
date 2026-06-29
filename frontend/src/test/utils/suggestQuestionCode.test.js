import { describe, it, expect } from 'vitest';
import { isValidQuestionCode, suggestQuestionCode } from '../../utils/suggestQuestionCode';

describe('suggestQuestionCode', () => {
  it('derives uppercase snake_case code from question text', () => {
    expect(suggestQuestionCode('Is abnormal vibration present?')).toBe(
      'IS_ABNORMAL_VIBRATION_PRESENT'
    );
  });

  it('prefixes codes that do not start with a letter', () => {
    expect(suggestQuestionCode('123 vibration check')).toBe('Q_123_VIBRATION_CHECK');
  });

  it('returns empty string for blank input', () => {
    expect(suggestQuestionCode('   ')).toBe('');
  });
});

describe('isValidQuestionCode', () => {
  it('accepts valid business codes', () => {
    expect(isValidQuestionCode('VIBRATION')).toBe(true);
    expect(isValidQuestionCode('HIGH_TEMPERATURE')).toBe(true);
  });

  it('rejects invalid business codes', () => {
    expect(isValidQuestionCode('lowercase')).toBe(false);
    expect(isValidQuestionCode('1_STARTS_WITH_DIGIT')).toBe(false);
    expect(isValidQuestionCode('HAS SPACE')).toBe(false);
  });
});
