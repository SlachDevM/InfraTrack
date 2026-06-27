import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/issues',
  DETAIL: (id) => `/api/issues/${id}`,
};

export const issueApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery(page, size)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  record: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default issueApi;
