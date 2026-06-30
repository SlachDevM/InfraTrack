import { describe, it, expect, vi } from 'vitest';
import dashboardPreferencesApi from '../../services/dashboardPreferencesApi';
import apiClient from '../../services/apiClient';

vi.mock('../../services/apiClient', () => ({
  default: {
    get: vi.fn(),
    put: vi.fn(),
    post: vi.fn(),
  },
}));

describe('dashboardPreferencesApi', () => {
  it('loads preferences', async () => {
    apiClient.get.mockResolvedValue({ defaultTrendRange: 'LAST_30_DAYS' });

    const response = await dashboardPreferencesApi.getPreferences();

    expect(apiClient.get).toHaveBeenCalledWith('/api/dashboard/preferences');
    expect(response.defaultTrendRange).toBe('LAST_30_DAYS');
  });

  it('saves preferences', async () => {
    apiClient.put.mockResolvedValue({ defaultTrendRange: 'LAST_90_DAYS' });

    await dashboardPreferencesApi.savePreferences({ defaultTrendRange: 'LAST_90_DAYS' });

    expect(apiClient.put).toHaveBeenCalledWith(
      '/api/dashboard/preferences',
      { defaultTrendRange: 'LAST_90_DAYS' },
    );
  });

  it('resets preferences', async () => {
    apiClient.post.mockResolvedValue({ defaultTrendRange: 'LAST_30_DAYS' });

    await dashboardPreferencesApi.resetPreferences();

    expect(apiClient.post).toHaveBeenCalledWith('/api/dashboard/preferences/reset', {});
  });
});
