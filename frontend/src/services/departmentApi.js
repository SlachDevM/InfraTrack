import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/departments',
  DETAIL: (id) => `/api/departments/${id}`,
};

export const departmentApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
  update: (id, request) => apiClient.put(ENDPOINTS.DETAIL(id), request),
  delete: (id) => apiClient.delete(ENDPOINTS.DETAIL(id)),
};

export default departmentApi;
