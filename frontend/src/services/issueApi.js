import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/issues',
  DETAIL: (id) => `/api/issues/${id}`,
};

export const issueApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  record: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default issueApi;
