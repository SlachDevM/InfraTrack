import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/assets',
  DETAIL: (id) => `/api/assets/${id}`,
};

export const assetApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  register: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default assetApi;
