import reportingExportApi from './reportingExportApi';
import { REPORTING_EXPORT_TYPES } from '../constants/reportingExports';

export const REPORTING_EXPORT_HANDLERS = {
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

export async function runReportingExport(exportType, token, params, format) {
  const handler = REPORTING_EXPORT_HANDLERS[exportType];
  if (!handler) {
    return;
  }
  await handler(token, params, format);
}
