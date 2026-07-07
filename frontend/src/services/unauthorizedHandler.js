let unauthorizedHandler = null;

export function registerUnauthorizedHandler(handler) {
  unauthorizedHandler = handler;
}

export function notifyUnauthorized() {
  if (unauthorizedHandler) {
    unauthorizedHandler();
  }
}
