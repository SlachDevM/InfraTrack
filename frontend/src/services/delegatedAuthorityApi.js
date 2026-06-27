import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/delegated-authorities',
  DETAIL: (id) => `/api/delegated-authorities/${id}`,
  REVOKE: (id) => `/api/delegated-authorities/${id}/revoke`,
};

const delegatedAuthorityApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery(page, size)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
  revoke: (id) => apiClient.post(ENDPOINTS.REVOKE(id), {}),
};

export default delegatedAuthorityApi;
