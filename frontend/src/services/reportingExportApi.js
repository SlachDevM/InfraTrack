import downloadAttachment from '../utils/downloadAttachment';
import {
  REPORTING_EXPORT_FORMATS,
  REPORTING_EXPORT_TYPES,
  getReportingExportConfig,
} from '../constants/reportingExports';

function exportByType(type, format, token, params) {
  const config = getReportingExportConfig(type, format);
  if (!config) {
    return Promise.reject(new Error('Unknown export type'));
  }
  return downloadAttachment(config.endpoint, token, params, config.filename);
}

export const reportingExportApi = {
  exportAssets: (token, params, format = REPORTING_EXPORT_FORMATS.CSV) =>
    exportByType(REPORTING_EXPORT_TYPES.ASSETS, format, token, params),
  exportInspections: (token, params, format = REPORTING_EXPORT_FORMATS.CSV) =>
    exportByType(REPORTING_EXPORT_TYPES.INSPECTIONS, format, token, params),
  exportIssues: (token, params, format = REPORTING_EXPORT_FORMATS.CSV) =>
    exportByType(REPORTING_EXPORT_TYPES.ISSUES, format, token, params),
  exportWorkOrders: (token, params, format = REPORTING_EXPORT_FORMATS.CSV) =>
    exportByType(REPORTING_EXPORT_TYPES.WORK_ORDERS, format, token, params),
  exportPreventiveCandidates: (token, params, format = REPORTING_EXPORT_FORMATS.CSV) =>
    exportByType(REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES, format, token, params),
};

export default reportingExportApi;
