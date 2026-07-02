import { describe, it, expect } from 'vitest';
import {
  DEFAULT_DASHBOARD_PREFERENCES,
  DASHBOARD_WIDGET_TYPES,
} from '../../constants/dashboardPreferences';
import { DASHBOARD } from '../../constants/dashboard';
import {
  buildTrendQueryFromRange,
  countVisibleWidgets,
  isWidgetVisible,
  moveWidget,
  normalizeDashboardPreferences,
} from '../../utils/dashboardPreferences';

describe('dashboardPreferences utils', () => {
  it('normalizes unknown widget order values', () => {
    const normalized = normalizeDashboardPreferences({
      ...DEFAULT_DASHBOARD_PREFERENCES,
      widgetOrder: ['TRENDS', 'UNKNOWN', 'OVERVIEW'],
    });

    expect(normalized.widgetOrder[0]).toBe('TRENDS');
    expect(normalized.widgetOrder).toContain('OVERVIEW');
    expect(normalized.widgetOrder).not.toContain('UNKNOWN');
  });

  it('moves widgets up and down', () => {
    const order = ['OVERVIEW', 'TRENDS', 'ATTENTION'];
    expect(moveWidget(order, 'TRENDS', 'up')).toEqual(['TRENDS', 'OVERVIEW', 'ATTENTION']);
    expect(moveWidget(order, 'TRENDS', 'down')).toEqual(['OVERVIEW', 'ATTENTION', 'TRENDS']);
  });

  it('counts visible widgets', () => {
    const preferences = {
      ...DEFAULT_DASHBOARD_PREFERENCES,
      showRecentActivityWidget: false,
    };
    expect(countVisibleWidgets(preferences)).toBe(4);
    expect(isWidgetVisible(preferences, DASHBOARD_WIDGET_TYPES.RECENT_ACTIVITY)).toBe(false);
  });

  it('builds trend query for saved range', () => {
    const query = buildTrendQueryFromRange('LAST_7_DAYS');
    expect(query.bucket).toBe(DASHBOARD.DEFAULT_TREND_BUCKET);
    expect(query.to).toBeGreaterThan(query.from);
  });
});
