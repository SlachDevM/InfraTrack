import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: (templateId, questionId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/choices`,
  DETAIL: (templateId, questionId, choiceId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/choices/${choiceId}`,
  DEACTIVATE: (templateId, questionId, choiceId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/choices/${choiceId}/deactivate`,
  REORDER: (templateId, questionId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/choices/reorder`,
};

export const inspectionTemplateQuestionChoiceApi = {
  list: (templateId, questionId) => apiClient.get(ENDPOINTS.LIST(templateId, questionId)),
  create: (templateId, questionId, request) =>
    apiClient.post(ENDPOINTS.LIST(templateId, questionId), request),
  update: (templateId, questionId, choiceId, request) =>
    apiClient.put(ENDPOINTS.DETAIL(templateId, questionId, choiceId), request),
  deactivate: (templateId, questionId, choiceId) =>
    apiClient.post(ENDPOINTS.DEACTIVATE(templateId, questionId, choiceId), {}),
  reorder: (templateId, questionId, orderedChoiceIds) =>
    apiClient.post(ENDPOINTS.REORDER(templateId, questionId), { orderedChoiceIds }),
};

export default inspectionTemplateQuestionChoiceApi;
