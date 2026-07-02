import { describe, it, expect, vi } from 'vitest';
import operationsIntelligenceApi from '../../services/operationsIntelligenceApi';
import apiClient from '../../services/apiClient';

vi.mock('../../services/apiClient', () => ({
  default: {
    get: vi.fn(),
  },
}));

describe('operationsIntelligenceApi', () => {
  it('calls KPI endpoint', async () => {
    apiClient.get.mockResolvedValue({ assets: { totalAssets: 1 } });

    const response = await operationsIntelligenceApi.getKpis();

    expect(apiClient.get).toHaveBeenCalledWith('/api/operations-intelligence/kpis');
    expect(response.assets.totalAssets).toBe(1);
  });

  it('calls trends endpoint with default query', async () => {
    apiClient.get.mockResolvedValue({ bucket: 'DAY', series: {} });

    await operationsIntelligenceApi.getTrends();

    expect(apiClient.get).toHaveBeenCalledWith('/api/operations-intelligence/trends');
  });

  it('calls trends endpoint with query parameters', async () => {
    apiClient.get.mockResolvedValue({ bucket: 'WEEK', series: {} });

    await operationsIntelligenceApi.getTrends({ from: 1000, to: 2000, bucket: 'WEEK' });

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/operations-intelligence/trends?from=1000&to=2000&bucket=WEEK'
    );
  });

  it('calls recent activity endpoint with query parameters', async () => {
    apiClient.get.mockResolvedValue({ items: [] });

    await operationsIntelligenceApi.getRecentActivity({ limit: 50 });

    expect(apiClient.get).toHaveBeenCalledWith(
      '/api/operations-intelligence/recent-activity?limit=50'
    );
  });
});
