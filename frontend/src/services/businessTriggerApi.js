import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/business-triggers',
  DETAIL: (id) => `/api/business-triggers/${id}`,
};

export const businessTriggerApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default businessTriggerApi;
