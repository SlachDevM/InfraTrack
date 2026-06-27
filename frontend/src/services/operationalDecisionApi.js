import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/operational-decisions',
  DETAIL: (id) => `/api/operational-decisions/${id}`,
};

export const operationalDecisionApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery(page, size)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
};

export default operationalDecisionApi;
