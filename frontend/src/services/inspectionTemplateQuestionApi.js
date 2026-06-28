import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: (templateId) => `/api/inspection-templates/${templateId}/questions`,
  DETAIL: (templateId, questionId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}`,
  DEACTIVATE: (templateId, questionId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/deactivate`,
  REORDER: (templateId) => `/api/inspection-templates/${templateId}/questions/reorder`,
};

export const inspectionTemplateQuestionApi = {
  list: (templateId) => apiClient.get(ENDPOINTS.LIST(templateId)),
  create: (templateId, request) => apiClient.post(ENDPOINTS.LIST(templateId), request),
  update: (templateId, questionId, request) =>
    apiClient.put(ENDPOINTS.DETAIL(templateId, questionId), request),
  deactivate: (templateId, questionId) =>
    apiClient.post(ENDPOINTS.DEACTIVATE(templateId, questionId), {}),
  reorder: (templateId, orderedQuestionIds) =>
    apiClient.post(ENDPOINTS.REORDER(templateId), { orderedQuestionIds }),
};

export default inspectionTemplateQuestionApi;
