import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: (inspectionId) => `/api/inspections/${inspectionId}/rule-evaluation/reports`,
  LATEST: (inspectionId) => `/api/inspections/${inspectionId}/rule-evaluation/reports/latest`,
  DETAIL: (inspectionId, reportId) =>
    `/api/inspections/${inspectionId}/rule-evaluation/reports/${reportId}`,
};

export const ruleEvaluationReportApi = {
  list: (inspectionId) => apiClient.get(ENDPOINTS.LIST(inspectionId)),
  getLatest: (inspectionId) => apiClient.get(ENDPOINTS.LATEST(inspectionId)),
  get: (inspectionId, reportId) => apiClient.get(ENDPOINTS.DETAIL(inspectionId, reportId)),
};

export default ruleEvaluationReportApi;
