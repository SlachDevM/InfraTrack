import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/preventive-maintenance-plans',
  DETAIL: (id) => `/api/preventive-maintenance-plans/${id}`,
  EVALUATION: (id) => `/api/preventive-maintenance-plans/${id}/evaluation`,
  EVALUATE_ALL: '/api/preventive-maintenance-plans/evaluation',
};

function buildListQuery(page, size, filters = {}) {
  const params = new URLSearchParams(paginatedQuery(page, size));
  if (filters.assetId) {
    params.set('assetId', String(filters.assetId));
  }
  if (filters.status) {
    params.set('status', filters.status);
  }
  if (filters.triggerType) {
    params.set('triggerType', filters.triggerType);
  }
  return params.toString();
}

export const preventiveMaintenancePlanApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE, filters = {}) =>
    apiClient.get(`${ENDPOINTS.LIST}?${buildListQuery(page, size, filters)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
  update: (id, request) => apiClient.put(ENDPOINTS.DETAIL(id), request),
  archive: (id) => apiClient.post(ENDPOINTS.ARCHIVE(id), {}),
  evaluate: (id) => apiClient.get(ENDPOINTS.EVALUATION(id)),
  evaluateAll: () => apiClient.get(ENDPOINTS.EVALUATE_ALL),
};

export default preventiveMaintenancePlanApi;
