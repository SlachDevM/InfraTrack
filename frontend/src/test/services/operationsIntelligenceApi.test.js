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
});
