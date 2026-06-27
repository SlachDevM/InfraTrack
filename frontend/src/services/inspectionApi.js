import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/inspections',
  DETAIL: (id) => `/api/inspections/${id}`,
  WORKERS: '/api/users/workers',
};

export const inspectionApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery(page, size)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  assign: (request) => apiClient.post(ENDPOINTS.LIST, request),
  complete: (id, request) => apiClient.post(`${ENDPOINTS.DETAIL(id)}/complete`, request),
  listWorkers: () => apiClient.get(ENDPOINTS.WORKERS),
};

export default inspectionApi;
