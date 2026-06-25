import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: '/api/maintenance-activities',
  COMPLETION_REVIEW: (id) => `/api/maintenance-activities/${id}/completion-review`,
};

export const maintenanceActivityApi = {
  list: () => apiClient.get(ENDPOINTS.LIST),
  recordCompletionReview: (id, request) => apiClient.post(ENDPOINTS.COMPLETION_REVIEW(id), request),
};

export default maintenanceActivityApi;
