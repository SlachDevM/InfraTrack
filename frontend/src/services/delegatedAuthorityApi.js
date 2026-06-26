import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/delegated-authorities',
  DETAIL: (id) => `/api/delegated-authorities/${id}`,
  REVOKE: (id) => `/api/delegated-authorities/${id}/revoke`,
};

const delegatedAuthorityApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
  revoke: (id) => apiClient.post(ENDPOINTS.REVOKE(id), {}),
};

export default delegatedAuthorityApi;
