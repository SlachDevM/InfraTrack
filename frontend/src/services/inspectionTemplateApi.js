import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  LIST: '/api/inspection-templates',
  DETAIL: (id) => `/api/inspection-templates/${id}`,
  ARCHIVE: (id) => `/api/inspection-templates/${id}/archive`,
  PUBLISH: (id) => `/api/inspection-templates/${id}/publish`,
};

function buildListQuery(page, size, filters = {}) {
  const params = new URLSearchParams(paginatedQuery(page, size));
  if (filters.assetCategoryId) {
    params.set('assetCategoryId', String(filters.assetCategoryId));
  }
  if (filters.status) {
    params.set('status', filters.status);
  }
  return params.toString();
}

export const inspectionTemplateApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE, filters = {}) =>
    apiClient.get(`${ENDPOINTS.LIST}?${buildListQuery(page, size, filters)}`),
  get: (id) => apiClient.get(ENDPOINTS.DETAIL(id)),
  create: (request) => apiClient.post(ENDPOINTS.LIST, request),
  update: (id, request) => apiClient.put(ENDPOINTS.DETAIL(id), request),
  archive: (id) => apiClient.post(ENDPOINTS.ARCHIVE(id), {}),
  publish: (id) => apiClient.post(ENDPOINTS.PUBLISH(id), {}),
};

export default inspectionTemplateApi;
