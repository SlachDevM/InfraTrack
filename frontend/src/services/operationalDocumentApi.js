import apiClient from './apiClient';
import API_CONFIG from '../config/apiConfig';
import { paginatedQuery, unwrapPageContent } from '../utils/pagination';

const ENDPOINTS = {
  LIST: (assetId) => `/api/assets/${assetId}/documents`,
  UPLOAD: (assetId) => `/api/assets/${assetId}/documents`,
  DOWNLOAD: (id) => `/api/operational-documents/${id}/download`,
};

export const operationalDocumentApi = {
  list: (assetId) => apiClient.get(`${ENDPOINTS.LIST(assetId)}?${paginatedQuery()}`).then(unwrapPageContent),
  upload: (assetId, formData) => {
    if (!(formData instanceof FormData)) {
      return Promise.reject(new TypeError('upload requires FormData'));
    }
    return apiClient.postMultipart(ENDPOINTS.UPLOAD(assetId), formData);
  },
  download: async (id, token) => {
    const response = await fetch(`${API_CONFIG.BASE_URL}${ENDPOINTS.DOWNLOAD(id)}`, {
      headers: token ? { Authorization: `Bearer ${token}` } : {},
    });
    if (!response.ok) {
      const text = await response.text();
      const error = new Error(text || `Download failed: ${response.status}`);
      error.status = response.status;
      throw error;
    }
    const blob = await response.blob();
    const disposition = response.headers.get('Content-Disposition') || '';
    const match = disposition.match(/filename="(.+)"/);
    const filename = match ? match[1] : `document-${id}`;
    return { blob, filename };
  },
};

export default operationalDocumentApi;
