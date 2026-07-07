import { COMMON_MESSAGES } from '../constants/messages';

export function PageErrorMessage({ message }) {
  if (!message) {
    return null;
  }

  return (
    <div className="error-message" role="alert" aria-live="assertive">
      {message}
    </div>
  );
}

export function PageSuccessMessage({ message, children }) {
  if (!message && !children) {
    return null;
  }

  return (
    <div className="success-message" role="status" aria-live="polite">
      {message}
      {children}
    </div>
  );
}

export function ListLoadingIndicator({ message = COMMON_MESSAGES.LOADING_PAGE }) {
  return (
    <div className="loading-state-inline list-loading-indicator" role="status" aria-live="polite">
      {message}
    </div>
  );
}

export function TableEmptyRow({ colSpan, message }) {
  return (
    <tr>
      <td colSpan={colSpan} className="table-empty-cell empty-state">
        {message}
      </td>
    </tr>
  );
}
