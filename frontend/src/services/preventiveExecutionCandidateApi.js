import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/preventive-execution-candidates',
  DETAIL: (id) => `/api/preventive-execution-candidates/${id}`,
  GENERATE: '/api/preventive-execution-candidates/generate',
  APPROVE: (id) => `/api/preventive-execution-candidates/${id}/approve`,
  REJECT: (id) => `/api/preventive-execution-candidates/${id}/reject`,
  DISMISS: (id) => `/api/preventive-execution-candidates/${id}/dismiss`,
  REPORT: (id) => `/api/preventive-execution-candidates/${id}/report`,
};

function buildListQuery(page, size, filters = {}) {
  const params = new URLSearchParams(paginatedQuery(page, size));
  if (filters.status) {
    params.set('status', filters.status);
  }
  if (filters.assetId) {
    params.set('assetId', String(filters.assetId));
  }
  if (filters.planId) {
    params.set('planId', String(filters.planId));
  }
  return params.toString();
}

export const preventiveExecutionCandidateApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE, filters = {}) =>
    apiClient.get(`${ENDPOINTS.LIST}?${buildListQuery(page, size, filters)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  generate: () => apiClient.post(ENDPOINTS.GENERATE, {}),
  approve: (id, request) => apiClient.post(ENDPOINTS.APPROVE(id), request),
  reject: (id, request) => apiClient.post(ENDPOINTS.REJECT(id), request),
  dismiss: (id, request) => apiClient.post(ENDPOINTS.DISMISS(id), request),
  getReport: (id) => apiClient.get(ENDPOINTS.REPORT(id)),
};

export default preventiveExecutionCandidateApi;
