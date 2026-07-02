import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import reportingExportApi from '../services/reportingExportApi';
import { REPORTING_EXPORT_TYPES } from '../constants/reportingExports';
import { COMMON_LABELS } from '../constants/uiLabels';
import { COMMON_MESSAGES } from '../constants/messages';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';

const EXPORT_HANDLERS = {
  [REPORTING_EXPORT_TYPES.ASSETS]: (token, params) =>
    reportingExportApi.exportAssets(token, params),
  [REPORTING_EXPORT_TYPES.INSPECTIONS]: (token, params) =>
    reportingExportApi.exportInspections(token, params),
  [REPORTING_EXPORT_TYPES.ISSUES]: (token, params) =>
    reportingExportApi.exportIssues(token, params),
  [REPORTING_EXPORT_TYPES.WORK_ORDERS]: (token, params) =>
    reportingExportApi.exportWorkOrders(token, params),
  [REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES]: (token, params) =>
    reportingExportApi.exportPreventiveCandidates(token, params),
};

export default function ExportCsvButton({
  exportType,
  label = COMMON_LABELS.EXPORT_CSV,
  onError,
  className = 'export-csv-btn',
}) {
  const { auth } = useAuth();
  const [exporting, setExporting] = useState(false);
  const buttonLabel = exporting ? COMMON_LABELS.EXPORTING : label;

  const handleClick = async () => {
    const handler = EXPORT_HANDLERS[exportType];
    if (!handler) {
      return;
    }

    try {
      setExporting(true);
      await handler(auth?.token);
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
      aria-label={label}
      aria-busy={exporting}
    >
      {buttonLabel}
    </button>
  );
}
