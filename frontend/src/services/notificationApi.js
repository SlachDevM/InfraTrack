import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import { DEFAULT_PAGE, DEFAULT_SIZE, paginatedQuery } from '../utils/pagination';

export const notificationApi = {
  list: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${API_ENDPOINTS.NOTIFICATIONS}?${paginatedQuery(page, size)}`),
  listUnread: (page = DEFAULT_PAGE, size = DEFAULT_SIZE) =>
    apiClient.get(`${API_ENDPOINTS.NOTIFICATIONS}/unread?${paginatedQuery(page, size)}`),
  getUnreadCount: () => apiClient.get(`${API_ENDPOINTS.NOTIFICATIONS}/unread-count`),
  markAsRead: (id) => apiClient.put(`${API_ENDPOINTS.NOTIFICATIONS}/${id}/read`, {}),
  markAllAsRead: () => apiClient.put(`${API_ENDPOINTS.NOTIFICATIONS}/read-all`, {}),
};

export default notificationApi;
