import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/preventive-execution-reports',
  DETAIL: (id) => `/api/preventive-execution-reports/${id}`,
  BY_CANDIDATE: (candidateId) => `/api/preventive-execution-candidates/${candidateId}/report`,
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
  if (filters.decisionSource) {
    params.set('decisionSource', filters.decisionSource);
  }
  return params.toString();
}

export const preventiveExecutionReportApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE, filters = {}) =>
    apiClient.get(`${ENDPOINTS.LIST}?${buildListQuery(page, size, filters)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  getByCandidate: (candidateId) => apiClient.get(ENDPOINTS.BY_CANDIDATE(candidateId)),
};

export default preventiveExecutionReportApi;
