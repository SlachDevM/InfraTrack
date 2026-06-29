import apiClient from './apiClient';

const ENDPOINTS = {
  LIST: (templateId, questionId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/rules`,
  DETAIL: (templateId, questionId, ruleId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/rules/${ruleId}`,
  DEACTIVATE: (templateId, questionId, ruleId) =>
    `/api/inspection-templates/${templateId}/questions/${questionId}/rules/${ruleId}/deactivate`,
};

export const inspectionTemplateQuestionRuleApi = {
  list: (templateId, questionId) => apiClient.get(ENDPOINTS.LIST(templateId, questionId)),
  create: (templateId, questionId, request) =>
    apiClient.post(ENDPOINTS.LIST(templateId, questionId), request),
  update: (templateId, questionId, ruleId, request) =>
    apiClient.put(ENDPOINTS.DETAIL(templateId, questionId, ruleId), request),
  deactivate: (templateId, questionId, ruleId) =>
    apiClient.post(ENDPOINTS.DEACTIVATE(templateId, questionId, ruleId), {}),
};

export default inspectionTemplateQuestionRuleApi;
