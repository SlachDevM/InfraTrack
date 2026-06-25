import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/operational-decisions',
  DETAIL: (id) => `/api/operational-decisions/${id}`,
};

export const operationalDecisionApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default operationalDecisionApi;
