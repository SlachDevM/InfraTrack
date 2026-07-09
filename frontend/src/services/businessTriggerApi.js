import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/business-triggers',
  DETAIL: (id) => `/api/business-triggers/${id}`,
};

export const businessTriggerApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE, options = {}) => {
    const params = new URLSearchParams(paginatedQuery(page, size));
    if (options.eligibleForInspection) {
      params.set('eligibleForInspection', 'true');
    }
    return apiClient.get(`${ENDPOINTS.LIST}?${params}`);
  },
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default businessTriggerApi;
