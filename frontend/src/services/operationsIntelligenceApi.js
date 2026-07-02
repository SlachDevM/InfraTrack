import apiClient from './apiClient';

const ENDPOINTS = {
  KPIS: '/api/operations-intelligence/kpis',
  TRENDS: '/api/operations-intelligence/trends',
  RECENT_ACTIVITY: '/api/operations-intelligence/recent-activity',
};

function buildQuery(params = {}) {
  const searchParams = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value != null) {
      searchParams.set(key, String(value));
    }
  });
  const query = searchParams.toString();
  return query ? `?${query}` : '';
}

export const operationsIntelligenceApi = {
  getKpis: () => apiClient.get(ENDPOINTS.KPIS),

  getTrends: (params = {}) => apiClient.get(`${ENDPOINTS.TRENDS}${buildQuery(params)}`),

  getRecentActivity: (params = {}) =>
    apiClient.get(`${ENDPOINTS.RECENT_ACTIVITY}${buildQuery(params)}`),
};

export default operationsIntelligenceApi;
