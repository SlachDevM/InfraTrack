import { describe, it, expect } from 'vitest';
import { getTrendMaxCount, isTrendSeriesEmpty } from '../../utils/trendChart';

describe('trendChart utils', () => {
  it('detects empty trend series', () => {
    expect(isTrendSeriesEmpty([])).toBe(true);
    expect(isTrendSeriesEmpty([{ period: '2026-06-01', count: 0 }])).toBe(true);
    expect(isTrendSeriesEmpty([{ period: '2026-06-01', count: 2 }])).toBe(false);
  });

  it('returns max count for chart scaling', () => {
    expect(getTrendMaxCount([{ count: 1 }, { count: 5 }, { count: 3 }])).toBe(5);
    expect(getTrendMaxCount([])).toBe(0);
  });
});
