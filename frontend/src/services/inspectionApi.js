import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/inspections',
  DETAIL: (id) => `/api/inspections/${id}`,
  WORKERS: '/api/users/workers',
};

export const inspectionApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  assign: (request) => apiClient.post(ENDPOINTS.LIST, request),
  listWorkers: () => apiClient.get(ENDPOINTS.WORKERS),
};

export default inspectionApi;
