import { FIELD_LIMITS } from '../constants/limits';
import { HTTP_STATUS } from '../constants/httpStatus';
import { COMMON_MESSAGES } from '../constants/messages';

export function getApiErrorMessage(error, fallback = COMMON_MESSAGES.REQUEST_FAILED) {
  if (!error?.message) {
    return fallback;
  }

  try {
    const parsed = JSON.parse(error.message);
    const retryAfterSeconds = Number(parsed.retryAfterSeconds);
    if (Number.isFinite(retryAfterSeconds) && retryAfterSeconds > 0) {
      return `Too many login attempts. Please try again in ${retryAfterSeconds} seconds.`;
    }
    return parsed.message || parsed.detail || parsed.error || fallback;
  } catch {
    const trimmed = error.message.trim();
    if (trimmed && trimmed.length <= FIELD_LIMITS.API_ERROR_MESSAGE_MAX_LENGTH) {
      return trimmed;
    }
    return fallback;
  }
}

export function isForbidden(error) {
  return error?.status === HTTP_STATUS.FORBIDDEN;
}

export function isConflict(error) {
  return error?.status === HTTP_STATUS.CONFLICT;
}

export function isValidationError(error) {
  return error?.status === HTTP_STATUS.BAD_REQUEST;
}

export function isUploadAuthorizationError(error) {
  if (!isForbidden(error)) {
    return false;
  }

  const message = getApiErrorMessage(error, '');
  if (!message) {
    return false;
  }

  if (/media type|octet-stream|multipart|unsupported/i.test(message)) {
    return false;
  }

  return /unauthorized|forbidden|cannot upload|permission|administrator/i.test(message);
}
