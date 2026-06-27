import apiClient from './apiClient';
import { paginatedQuery, unwrapPageContent } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/inspections',
  DETAIL: (id) => `/api/inspections/${id}`,
  WORKERS: '/api/users/workers',
};

export const inspectionApi = {
  list: () => apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery()}`).then(unwrapPageContent),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  assign: (request) => apiClient.post(ENDPOINTS.LIST, request),
  complete: (id, request) => apiClient.post(`${ENDPOINTS.DETAIL(id)}/complete`, request),
  listWorkers: () => apiClient.get(ENDPOINTS.WORKERS),
};

export default inspectionApi;
