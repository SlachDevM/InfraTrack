import downloadCsv from '../utils/downloadCsv';
import { REPORTING_EXPORT_TYPES, REPORTING_EXPORTS } from '../constants/reportingExports';

function getExportConfig(type) {
  return REPORTING_EXPORTS[type];
}

export const reportingExportApi = {
  exportAssets: (token, params) => {
    const config = getExportConfig(REPORTING_EXPORT_TYPES.ASSETS);
    return downloadCsv(config.endpoint, token, params, config.filename);
  },
  exportInspections: (token, params) => {
    const config = getExportConfig(REPORTING_EXPORT_TYPES.INSPECTIONS);
    return downloadCsv(config.endpoint, token, params, config.filename);
  },
  exportIssues: (token, params) => {
    const config = getExportConfig(REPORTING_EXPORT_TYPES.ISSUES);
    return downloadCsv(config.endpoint, token, params, config.filename);
  },
  exportWorkOrders: (token, params) => {
    const config = getExportConfig(REPORTING_EXPORT_TYPES.WORK_ORDERS);
    return downloadCsv(config.endpoint, token, params, config.filename);
  },
  exportPreventiveCandidates: (token, params) => {
    const config = getExportConfig(REPORTING_EXPORT_TYPES.PREVENTIVE_CANDIDATES);
    return downloadCsv(config.endpoint, token, params, config.filename);
  },
};

export default reportingExportApi;
