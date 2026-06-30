import apiClient from './apiClient';

const ENDPOINTS = {
  KPIS: '/api/operations-intelligence/kpis',
};

export const operationsIntelligenceApi = {
  getKpis: () => apiClient.get(ENDPOINTS.KPIS),
};

export default operationsIntelligenceApi;
