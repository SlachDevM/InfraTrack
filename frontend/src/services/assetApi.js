import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/assets',
  DETAIL: (id) => `/api/assets/${id}`,
  HISTORY: (id) => `/api/assets/${id}/history`,
};

export const assetApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  getHistory: (id) => apiClient.get(ENDPOINTS.HISTORY(id)),
  register: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default assetApi;
