import { useEffect, useRef, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { REPORTING_EXPORT_MENU_OPTIONS } from '../constants/reportingExports';
import { runReportingExport } from '../services/reportingExportHandlers';
import { COMMON_LABELS } from '../constants/uiLabels';
import { COMMON_MESSAGES } from '../constants/messages';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';

export default function ExportReportingMenu({
  exportType,
  onError,
  className = 'export-csv-btn',
  menuClassName = 'navbar-more-menu',
  menuItemClassName = 'navbar-more-item',
}) {
  const { auth } = useAuth();
  const [open, setOpen] = useState(false);
  const [exporting, setExporting] = useState(false);
  const containerRef = useRef(null);

  useEffect(() => {
    if (!open) {
      return undefined;
    }

    const handleClickOutside = (event) => {
      if (!containerRef.current?.contains(event.target)) {
        setOpen(false);
      }
    };

    const handleEscape = (event) => {
      if (event.key === 'Escape') {
        setOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    document.addEventListener('keydown', handleEscape);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      document.removeEventListener('keydown', handleEscape);
    };
  }, [open]);

  const handleExport = async (format) => {
    setOpen(false);

    try {
      setExporting(true);
      await runReportingExport(exportType, auth?.token, undefined, format);
    } catch (err) {
      const message = isForbidden(err)
        ? COMMON_MESSAGES.EXPORT_FORBIDDEN
        : getApiErrorMessage(err, COMMON_MESSAGES.EXPORT_FAILED);
      if (onError) {
        onError(message);
      }
    } finally {
      setExporting(false);
    }
  };

  const buttonLabel = exporting ? COMMON_LABELS.EXPORTING : COMMON_LABELS.EXPORT;

  return (
    <div className="export-reporting-menu" ref={containerRef}>
      <button
        type="button"
        className={className}
        aria-label={COMMON_LABELS.EXPORT}
        aria-haspopup="menu"
        aria-expanded={open}
        aria-busy={exporting}
        disabled={exporting}
        onClick={() => setOpen((previous) => !previous)}
      >
        {buttonLabel}
      </button>
      {open && (
        <div className={menuClassName} role="menu">
          {REPORTING_EXPORT_MENU_OPTIONS.map((option) => (
            <button
              key={option.format}
              type="button"
              role="menuitem"
              className={menuItemClassName}
              disabled={exporting}
              onClick={() => handleExport(option.format)}
            >
              {option.label}
            </button>
          ))}
        </div>
      )}
    </div>
  );
}
