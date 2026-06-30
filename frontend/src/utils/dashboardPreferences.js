import {
  DEFAULT_DASHBOARD_PREFERENCES,
  DEFAULT_WIDGET_ORDER,
  DASHBOARD_WIDGET_TYPES,
} from '../constants/dashboardPreferences';

const TREND_RANGE_DAYS = {
  LAST_7_DAYS: 7,
  LAST_30_DAYS: 30,
  LAST_90_DAYS: 90,
};

export function normalizeDashboardPreferences(preferences) {
  if (!preferences) {
    return { ...DEFAULT_DASHBOARD_PREFERENCES, widgetOrder: [...DEFAULT_WIDGET_ORDER] };
  }

  const knownOrder = new Set(DEFAULT_WIDGET_ORDER);
  const widgetOrder = [];
  (preferences.widgetOrder ?? []).forEach((widget) => {
    if (knownOrder.has(widget) && !widgetOrder.includes(widget)) {
      widgetOrder.push(widget);
    }
  });
  DEFAULT_WIDGET_ORDER.forEach((widget) => {
    if (!widgetOrder.includes(widget)) {
      widgetOrder.push(widget);
    }
  });

  return {
    showOverviewWidget: preferences.showOverviewWidget ?? true,
    showAttentionWidget: preferences.showAttentionWidget ?? true,
    showTrendWidget: preferences.showTrendWidget ?? true,
    showRecentActivityWidget: preferences.showRecentActivityWidget ?? true,
    showQuickNavigationWidget: preferences.showQuickNavigationWidget ?? true,
    defaultTrendRange: preferences.defaultTrendRange ?? DEFAULT_DASHBOARD_PREFERENCES.defaultTrendRange,
    widgetOrder,
  };
}

export function isWidgetVisible(preferences, widgetType) {
  switch (widgetType) {
    case DASHBOARD_WIDGET_TYPES.OVERVIEW:
      return preferences.showOverviewWidget;
    case DASHBOARD_WIDGET_TYPES.ATTENTION:
      return preferences.showAttentionWidget;
    case DASHBOARD_WIDGET_TYPES.TRENDS:
      return preferences.showTrendWidget;
    case DASHBOARD_WIDGET_TYPES.RECENT_ACTIVITY:
      return preferences.showRecentActivityWidget;
    case DASHBOARD_WIDGET_TYPES.QUICK_NAVIGATION:
      return preferences.showQuickNavigationWidget;
    default:
      return false;
  }
}

export function countVisibleWidgets(preferences) {
  return preferences.widgetOrder.filter((widget) => isWidgetVisible(preferences, widget)).length;
}

export function moveWidget(widgetOrder, widgetType, direction) {
  const order = [...widgetOrder];
  const index = order.indexOf(widgetType);
  if (index < 0) {
    return order;
  }
  const targetIndex = direction === 'up' ? index - 1 : index + 1;
  if (targetIndex < 0 || targetIndex >= order.length) {
    return order;
  }
  const [item] = order.splice(index, 1);
  order.splice(targetIndex, 0, item);
  return order;
}

export function buildTrendQueryFromRange(defaultTrendRange) {
  const days = TREND_RANGE_DAYS[defaultTrendRange] ?? TREND_RANGE_DAYS.LAST_30_DAYS;
  const to = Date.now();
  const fromDate = new Date(to);
  fromDate.setHours(0, 0, 0, 0);
  fromDate.setDate(fromDate.getDate() - days);
  return {
    from: fromDate.getTime(),
    to,
    bucket: 'DAY',
  };
}

export function preferencesToRequest(preferences) {
  return {
    showOverviewWidget: preferences.showOverviewWidget,
    showAttentionWidget: preferences.showAttentionWidget,
    showTrendWidget: preferences.showTrendWidget,
    showRecentActivityWidget: preferences.showRecentActivityWidget,
    showQuickNavigationWidget: preferences.showQuickNavigationWidget,
    defaultTrendRange: preferences.defaultTrendRange,
    widgetOrder: preferences.widgetOrder,
  };
}
