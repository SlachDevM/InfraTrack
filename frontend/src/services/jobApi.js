import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/jobConfig';

export const jobApi = {
  archiveJob: (jobId) => {
    return apiClient.put(`${API_ENDPOINTS.JOBS}/${jobId}/archive`, {});
  },
};

export default jobApi;
