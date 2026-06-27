import apiClient from './apiClient';
import { paginatedQuery, unwrapPageContent } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/assets',
  DETAIL: (id) => `/api/assets/${id}`,
  HISTORY: (id) => `/api/assets/${id}/history`,
};

export const assetApi = {
  list: () => apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery()}`).then(unwrapPageContent),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  getHistory: (id) => apiClient.get(`${ENDPOINTS.HISTORY(id)}?${paginatedQuery()}`).then(unwrapPageContent),
  register: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default assetApi;
