import { describe, it, expect } from 'vitest';
import {
  isPasswordLengthValid,
  PASSWORD_MIN_LENGTH,
  PASSWORD_MAX_LENGTH,
} from '../../constants/passwordPolicy';

describe('passwordPolicy', () => {
  it('requires at least twelve characters', () => {
    expect(isPasswordLengthValid('elevenchars')).toBe(false);
    expect(isPasswordLengthValid('twelvechars!')).toBe(true);
  });

  it('rejects passwords longer than one hundred twenty eight characters', () => {
    expect(isPasswordLengthValid('a'.repeat(PASSWORD_MAX_LENGTH + 1))).toBe(false);
    expect(isPasswordLengthValid('a'.repeat(PASSWORD_MAX_LENGTH))).toBe(true);
  });

  it('exports consistent minimum length', () => {
    expect(PASSWORD_MIN_LENGTH).toBe(12);
  });
});
