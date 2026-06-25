import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/work-orders',
  DETAIL: (id) => `/api/work-orders/${id}`,
};

export const workOrderApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default workOrderApi;
