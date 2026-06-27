import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/work-orders',
  DETAIL: (id) => `/api/work-orders/${id}`,
  ASSIGN: (id) => `/api/work-orders/${id}/assign`,
  COMPLETE_MAINTENANCE: (id) => `/api/work-orders/${id}/maintenance-activity`,
  WORKERS: '/api/users/workers',
};

export const workOrderApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery(page, size)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
  assign: (id, request) => apiClient.post(ENDPOINTS.ASSIGN(id), request),
  completeMaintenance: (id, request) => apiClient.post(ENDPOINTS.COMPLETE_MAINTENANCE(id), request),
  listWorkers: () => apiClient.get(ENDPOINTS.WORKERS),
  listEligibleWorkers: (departmentId, role) =>
    apiClient.get(`${ENDPOINTS.WORKERS}?departmentId=${departmentId}&role=${role}`),
};

export default workOrderApi;
