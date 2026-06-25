import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/asset-categories',
  DETAIL: (id) => `/api/asset-categories/${id}`,
};

export const assetCategoryApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
  update: (id, request) => apiClient.put(ENDPOINTS.DETAIL(id), request),
  delete: (id) => apiClient.delete(ENDPOINTS.DETAIL(id)),
};

export default assetCategoryApi;
