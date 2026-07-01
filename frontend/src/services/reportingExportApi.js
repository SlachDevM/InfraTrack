import downloadCsv from '../utils/downloadCsv';

const ENDPOINTS = {
  assets: '/api/reporting/exports/assets.csv',
  inspections: '/api/reporting/exports/inspections.csv',
  issues: '/api/reporting/exports/issues.csv',
  workOrders: '/api/reporting/exports/work-orders.csv',
  preventiveCandidates: '/api/reporting/exports/preventive-candidates.csv',
};

const DEFAULT_FILENAMES = {
  assets: 'assets-export.csv',
  inspections: 'inspections-export.csv',
  issues: 'issues-export.csv',
  workOrders: 'work-orders-export.csv',
  preventiveCandidates: 'preventive-candidates-export.csv',
};

export const reportingExportApi = {
  exportAssets: (token, params) =>
    downloadCsv(ENDPOINTS.assets, token, params, DEFAULT_FILENAMES.assets),
  exportInspections: (token, params) =>
    downloadCsv(ENDPOINTS.inspections, token, params, DEFAULT_FILENAMES.inspections),
  exportIssues: (token, params) =>
    downloadCsv(ENDPOINTS.issues, token, params, DEFAULT_FILENAMES.issues),
  exportWorkOrders: (token, params) =>
    downloadCsv(ENDPOINTS.workOrders, token, params, DEFAULT_FILENAMES.workOrders),
  exportPreventiveCandidates: (token, params) =>
    downloadCsv(ENDPOINTS.preventiveCandidates, token, params, DEFAULT_FILENAMES.preventiveCandidates),
};

export default reportingExportApi;
