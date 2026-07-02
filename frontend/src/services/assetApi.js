import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/assets',
  DETAIL: (id) => `/api/assets/${id}`,
  HISTORY: (id) => `/api/assets/${id}/history`,
};

export const assetApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery(page, size)}`),
  listEligibleForOperationalDocumentUpload: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(
      `${ENDPOINTS.LIST}?${paginatedQuery(page, size)}&eligibleForOperationalDocumentUpload=true`
    ),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  getHistory: (id, page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.HISTORY(id)}?${paginatedQuery(page, size)}`),
  register: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default assetApi;
