import apiClient from './apiClient';

const ENDPOINTS = {
  PREFERENCES: '/api/dashboard/preferences',
  RESET: '/api/dashboard/preferences/reset',
};

export const dashboardPreferencesApi = {
  getPreferences: () => apiClient.get(ENDPOINTS.PREFERENCES),

  savePreferences: (preferences) => apiClient.put(ENDPOINTS.PREFERENCES, preferences),

  resetPreferences: () => apiClient.post(ENDPOINTS.RESET, {}),
};

export default dashboardPreferencesApi;
