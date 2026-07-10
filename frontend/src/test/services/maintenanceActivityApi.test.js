import { describe, it, expect, vi, beforeEach } from 'vitest';
import maintenanceActivityApi from '../../services/maintenanceActivityApi';
import apiClient from '../../services/apiClient';
import { DEFAULT_PAGE, DEFAULT_SIZE, MAX_PAGE_SIZE } from '../../constants/pagination';

vi.mock('../../services/apiClient', () => ({
  default: {
    get: vi.fn(),
    post: vi.fn(),
  },
}));

describe('maintenanceActivityApi', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('list requests paginated maintenance activities', async () => {
    apiClient.get.mockResolvedValue({ content: [], number: 0, totalPages: 0 });

    await maintenanceActivityApi.list(DEFAULT_PAGE, DEFAULT_SIZE);

    expect(apiClient.get).toHaveBeenCalledWith('/api/maintenance-activities?page=0&size=20');
  });

  it('listEligibleForCompletionReview requests paginated eligible activities', async () => {
    apiClient.get.mockResolvedValue({ content: [], number: 0, totalPages: 0 });

    await maintenanceActivityApi.listEligibleForCompletionReview(DEFAULT_PAGE, MAX_PAGE_SIZE);

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/maintenance-activities?page=0&size=100&eligibleForCompletionReview=true'
    );
  });
});
