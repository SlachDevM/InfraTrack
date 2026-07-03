import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import reportingExportApi from '../services/reportingExportApi';
import { REPORTING_EXPORT_FORMATS, REPORTING_EXPORT_TYPES } from '../constants/reportingExports';
import { COMMON_LABELS } from '../constants/uiLabels';
import { COMMON_MESSAGES } from '../constants/messages';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';

const EXPORT_HANDLERS = {
  [REPORTING_EXPORT_TYPES.ASSETS]: (token, params, format) =>
    reportingExportApi.exportAssets(token, params, format),
  [REPORTING_EXPORT_TYPES.INSPECTIONS]: (token, params, format) =>
    reportingExportApi.exportInspections(token, params, format),
  [REPORTING_EXPORT_TYPES.ISSUES]: (token, params, format) =>
    reportingExportApi.exportIssues(token, params, format),
  [REPORTING_EXPORT_TYPES.WORK_ORDERS]: (token, params, format) =>
    reportingExportApi.exportWorkOrders(token, params, format),
  [REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES]: (token, params, format) =>
    reportingExportApi.exportPreventiveCandidates(token, params, format),
};

export default function ExportReportingButton({
  exportType,
  format = REPORTING_EXPORT_FORMATS.CSV,
  label,
  onError,
  className = 'export-reporting-btn',
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
    const handler = EXPORT_HANDLERS[exportType];
    if (!handler) {
      return;
    }

    try {
      setExporting(true);
      await handler(auth?.token, undefined, format);
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
