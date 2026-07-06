import { useEffect, useId } from 'react';

export default function ConfirmDialog({
  title,
  message,
  confirmLabel,
  onConfirm,
  onCancel,
  isLoading,
  isDangerous,
}) {
  const titleId = useId();

  useEffect(() => {
    const handleEscape = (event) => {
      if (event.key === 'Escape' && !isLoading) {
        onCancel();
      }
    };

    document.addEventListener('keydown', handleEscape);
    return () => document.removeEventListener('keydown', handleEscape);
  }, [isLoading, onCancel]);

  const confirmAriaLabel = isDangerous ? `${confirmLabel} — destructive action` : confirmLabel;

  return (
    <div className="modal-overlay" onClick={isLoading ? undefined : onCancel}>
      <div
        className={`confirm-dialog ${isDangerous ? 'dangerous' : ''}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 id={titleId}>{title}</h2>
        <p>{message}</p>
        <div className="confirm-actions">
          <button
            type="button"
            className="btn-cancel"
            onClick={onCancel}
            disabled={isLoading}
            aria-label="Cancel confirmation"
          >
            Cancel
          </button>
          <button
            type="button"
            className={`btn-confirm ${isDangerous ? 'dangerous' : ''}`}
            onClick={onConfirm}
            disabled={isLoading}
            aria-label={confirmAriaLabel}
            aria-busy={isLoading}
          >
            {isLoading ? 'Processing...' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
