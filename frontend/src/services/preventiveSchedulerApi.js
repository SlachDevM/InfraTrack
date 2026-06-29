import apiClient from './apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

const ENDPOINTS = {
  STATUS: '/api/preventive-scheduler/status',
  RUN: '/api/preventive-scheduler/run',
  RUNS: '/api/preventive-scheduler/runs',
  RUN_DETAIL: (id) => `/api/preventive-scheduler/runs/${id}`,
};

export const preventiveSchedulerApi = {
  getStatus: () => apiClient.get(ENDPOINTS.STATUS),
  run: () => apiClient.post(ENDPOINTS.RUN, {}),
  listRuns: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${ENDPOINTS.RUNS}?${paginatedQuery(page, size)}`),
  getRun: (id) => apiClient.get(ENDPOINTS.RUN_DETAIL(id)),
};

export default preventiveSchedulerApi;
