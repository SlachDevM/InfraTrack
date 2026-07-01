import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import reportingExportApi from '../services/reportingExportApi';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';

const EXPORT_HANDLERS = {
  assets: (token, params) => reportingExportApi.exportAssets(token, params),
  inspections: (token, params) => reportingExportApi.exportInspections(token, params),
  issues: (token, params) => reportingExportApi.exportIssues(token, params),
  workOrders: (token, params) => reportingExportApi.exportWorkOrders(token, params),
  preventiveCandidates: (token, params) => reportingExportApi.exportPreventiveCandidates(token, params),
};

export default function ExportCsvButton({
  exportType,
  label = 'Export CSV',
  onError,
  className = 'export-csv-btn',
}) {
  const { auth } = useAuth();
  const [exporting, setExporting] = useState(false);

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
        ? 'You do not have permission to export operational data.'
        : getApiErrorMessage(err, 'Failed to export CSV.');
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
    >
      {exporting ? 'Exporting...' : label}
    </button>
  );
}
