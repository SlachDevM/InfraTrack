import apiClient from './apiClient';
import { API_ENDPOINTS } from '../constants/apiEndpoints';
import { paginatedQuery, unwrapPageContent } from '../utils/pagination';

export const notificationApi = {
  list: () => apiClient.get(`${API_ENDPOINTS.NOTIFICATIONS}?${paginatedQuery()}`).then(unwrapPageContent),
  listUnread: () => apiClient.get(`${API_ENDPOINTS.NOTIFICATIONS}/unread?${paginatedQuery()}`).then(unwrapPageContent),
  getUnreadCount: () => apiClient.get(`${API_ENDPOINTS.NOTIFICATIONS}/unread-count`),
  markAsRead: (id) => apiClient.put(`${API_ENDPOINTS.NOTIFICATIONS}/${id}/read`, {}),
  markAllAsRead: () => apiClient.put(`${API_ENDPOINTS.NOTIFICATIONS}/read-all`, {}),
};

export default notificationApi;
