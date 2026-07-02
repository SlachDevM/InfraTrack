import { describe, it, expect } from 'vitest';
import {
  getApiErrorMessage,
  isForbidden,
  isConflict,
  isValidationError,
  isUploadAuthorizationError,
} from '../../utils/apiError';

describe('getApiErrorMessage', () => {
  it('returns fallback when error is missing', () => {
    expect(getApiErrorMessage(null)).toBe('Request failed.');
    expect(getApiErrorMessage(undefined, 'Custom fallback')).toBe('Custom fallback');
  });

  it('returns fallback when error has no message', () => {
    expect(getApiErrorMessage({ status: 500 }, 'Server error')).toBe('Server error');
  });

  it('returns trimmed backend string response', () => {
    const error = { message: '  Invalid document  ' };
    expect(getApiErrorMessage(error)).toBe('Invalid document');
  });

  it('returns message from JSON backend response', () => {
    const error = { message: JSON.stringify({ message: 'Asset not found' }) };
    expect(getApiErrorMessage(error)).toBe('Asset not found');
  });

  it('returns detail from JSON backend response when message is absent', () => {
    const error = { message: JSON.stringify({ detail: 'Validation failed' }) };
    expect(getApiErrorMessage(error)).toBe('Validation failed');
  });

  it('returns error field from JSON backend response', () => {
    const error = { message: JSON.stringify({ error: 'Conflict detected' }) };
    expect(getApiErrorMessage(error)).toBe('Conflict detected');
  });

  it('returns fallback for long non-JSON messages', () => {
    const error = { message: 'x'.repeat(301) };
    expect(getApiErrorMessage(error, 'Too long')).toBe('Too long');
  });

  it('returns countdown message when retryAfterSeconds is present', () => {
    const error = {
      message: JSON.stringify({
        message: 'Too many login attempts. Please try again later.',
        retryAfterSeconds: 60,
      }),
    };
    expect(getApiErrorMessage(error)).toBe(
      'Too many login attempts. Please try again in 60 seconds.'
    );
  });

  it('returns generic JSON message when retryAfterSeconds is absent', () => {
    const error = {
      message: JSON.stringify({
        message: 'Too many login attempts. Please try again later.',
      }),
    };
    expect(getApiErrorMessage(error)).toBe('Too many login attempts. Please try again later.');
  });
});

describe('isForbidden', () => {
  it('detects forbidden responses', () => {
    expect(isForbidden({ status: 403 })).toBe(true);
    expect(isForbidden({ status: 401 })).toBe(false);
  });
});

describe('isConflict', () => {
  it('detects conflict responses', () => {
    expect(isConflict({ status: 409 })).toBe(true);
    expect(isConflict({ status: 400 })).toBe(false);
  });
});

describe('isValidationError', () => {
  it('detects validation responses', () => {
    expect(isValidationError({ status: 400 })).toBe(true);
    expect(isValidationError({ status: 409 })).toBe(false);
  });
});

describe('isUploadAuthorizationError', () => {
  it('returns false when not forbidden', () => {
    expect(isUploadAuthorizationError({ status: 400, message: 'Invalid document' })).toBe(false);
  });

  it('returns false for multipart or media type errors', () => {
    expect(
      isUploadAuthorizationError({
        status: 403,
        message: 'Unsupported media type',
      })
    ).toBe(false);
  });

  it('detects upload permission errors', () => {
    expect(
      isUploadAuthorizationError({
        status: 403,
        message: 'Administrator cannot upload operational documents',
      })
    ).toBe(true);
  });
});
