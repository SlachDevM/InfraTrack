export function getApiErrorMessage(error, fallback = 'Request failed.') {
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
    if (trimmed && trimmed.length <= 300) {
      return trimmed;
    }
    return fallback;
  }
}

export function isForbidden(error) {
  return error?.status === 403;
}

export function isConflict(error) {
  return error?.status === 409;
}

export function isValidationError(error) {
  return error?.status === 400;
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
