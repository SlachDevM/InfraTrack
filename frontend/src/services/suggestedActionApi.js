import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: (inspectionId) => `/api/inspections/${inspectionId}/suggested-actions`,
  DETAIL: (inspectionId, suggestedActionId) =>
    `/api/inspections/${inspectionId}/suggested-actions/${suggestedActionId}`,
  GLOBAL_DETAIL: (id) => `/api/suggested-actions/${id}`,
  APPROVE: (id) => `/api/suggested-actions/${id}/approve`,
  REJECT: (id) => `/api/suggested-actions/${id}/reject`,
  DISMISS: (id) => `/api/suggested-actions/${id}/dismiss`,
};

export const suggestedActionApi = {
  list: (inspectionId, filters = {}) => {
    const params = new URLSearchParams();
    if (filters.status) {
      params.set('status', filters.status);
    }
    if (filters.actionType) {
      params.set('actionType', filters.actionType);
    }
    const query = params.toString();
    const url = query
      ? `${ENDPOINTS.LIST(inspectionId)}?${query}`
      : ENDPOINTS.LIST(inspectionId);
    return apiClient.get(url);
  },
  get: (inspectionId, suggestedActionId) =>
    apiClient.get(ENDPOINTS.DETAIL(inspectionId, suggestedActionId)),
  getDetail: (id) => apiClient.get(ENDPOINTS.GLOBAL_DETAIL(id)),
  approve: (id, request) => apiClient.post(ENDPOINTS.APPROVE(id), request),
  reject: (id, request) => apiClient.post(ENDPOINTS.REJECT(id), request),
  dismiss: (id, request) => apiClient.post(ENDPOINTS.DISMISS(id), request),
};

export default suggestedActionApi;
