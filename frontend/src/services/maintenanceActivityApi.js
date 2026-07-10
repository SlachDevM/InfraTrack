import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/maintenance-activities',
  COMPLETION_REVIEW: (id) => `/api/maintenance-activities/${id}/completion-review`,
};

export const maintenanceActivityApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.LIST}?${paginatedQuery(page, size)}`),
  listEligibleForCompletionReview: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(
      `${ENDPOINTS.LIST}?${paginatedQuery(page, size)}&eligibleForCompletionReview=true`
    ),
  recordCompletionReview: (id, request) => apiClient.post(ENDPOINTS.COMPLETION_REVIEW(id), request),
};

export default maintenanceActivityApi;
