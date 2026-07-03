import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { runReportingExport } from '../services/reportingExportHandlers';
import { REPORTING_EXPORT_FORMATS } from '../constants/reportingExports';
import { COMMON_LABELS } from '../constants/uiLabels';
import { COMMON_MESSAGES } from '../constants/messages';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';

export default function ExportReportingButton({
  exportType,
  format = REPORTING_EXPORT_FORMATS.CSV,
  label,
  onError,
  className = 'export-csv-btn',
}) {
  const { auth } = useAuth();
  const [exporting, setExporting] = useState(false);
  const defaultLabel =
    format === REPORTING_EXPORT_FORMATS.XLSX
      ? COMMON_LABELS.EXPORT_XLSX
      : format === REPORTING_EXPORT_FORMATS.PDF
        ? COMMON_LABELS.EXPORT_PDF
        : COMMON_LABELS.EXPORT_CSV;
  const buttonLabel = exporting ? COMMON_LABELS.EXPORTING : (label ?? defaultLabel);

  const handleClick = async () => {
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

  return (
    <button
      type="button"
      className={className}
      onClick={handleClick}
      disabled={exporting}
      aria-label={label ?? defaultLabel}
      aria-busy={exporting}
    >
      {buttonLabel}
    </button>
  );
}
