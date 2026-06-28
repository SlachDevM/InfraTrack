import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/maintenance-activities',
  COMPLETION_REVIEW: (id) => `/api/maintenance-activities/${id}/completion-review`,
};

export const maintenanceActivityApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  listEligibleForCompletionReview: () => apiClient.get(`${ENDPOINTS.LIST}?eligibleForCompletionReview=true`),
  recordCompletionReview: (id, request) => apiClient.post(ENDPOINTS.COMPLETION_REVIEW(id), request),
};

export default maintenanceActivityApi;
