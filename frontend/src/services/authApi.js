import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';

export const authApi = {
  activateAccount: (token, password) =>
    apiClient.post(API_ENDPOINTS.AUTH_ACTIVATE, { token, password }),
};

export default authApi;
