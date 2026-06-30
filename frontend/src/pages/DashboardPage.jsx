import { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import operationsIntelligenceApi from '../services/operationsIntelligenceApi';
import dashboardPreferencesApi from '../services/dashboardPreferencesApi';
import AppNavbar from '../components/navigation/AppNavbar';
import KpiOverviewWidget from '../components/dashboard/KpiOverviewWidget';
import AttentionRequiredWidget from '../components/dashboard/AttentionRequiredWidget';
import TrendWidget from '../components/dashboard/TrendWidget';
import QuickNavigationWidget from '../components/dashboard/QuickNavigationWidget';
import RecentActivityWidget from '../components/dashboard/RecentActivityWidget';
import DashboardSettingsPanel from '../components/dashboard/DashboardSettingsPanel';
import { canViewOperationsDashboard } from '../constants/userRoles';
import { canAccessRoute } from '../constants/navigation';
import { DASHBOARD_WIDGET_TYPES } from '../constants/dashboardPreferences';
import { getApiErrorMessage } from '../utils/apiError';
import { buildAttentionAlerts } from '../utils/operationsDashboardAlerts';
import {
  buildTrendQueryFromRange,
  isWidgetVisible,
  normalizeDashboardPreferences,
} from '../utils/dashboardPreferences';
import { APP_VERSION } from '../config/appVersion';
import '../styles/PlatformShell.css';
import '../styles/DashboardPage.css';

const QUICK_NAVIGATION_LINKS = [
  { path: '/assets', label: 'Assets' },
  { path: '/inspections', label: 'Inspections' },
  { path: '/issues', label: 'Issues' },
  { path: '/work-orders', label: 'Work Orders' },
  { path: '/preventive-execution-candidates', label: 'Preventive Candidates' },
  { path: '/preventive-scheduler', label: 'Scheduler' },
];

function getDisplayName(user) {
  if (!user) {
    return 'there';
  }
  if (user.name?.trim()) {
    return user.name.trim();
  }
  if (user.email) {
    return user.email.split('@')[0];
  }
  return 'there';
}

export default function DashboardPage() {
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
  const [preferences, setPreferences] = useState(null);
  const [preferencesLoading, setPreferencesLoading] = useState(true);
  const [preferencesError, setPreferencesError] = useState(null);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [savingPreferences, setSavingPreferences] = useState(false);
  const [resettingPreferences, setResettingPreferences] = useState(false);
  const [kpis, setKpis] = useState(null);
  const [trends, setTrends] = useState(null);
  const [recentActivity, setRecentActivity] = useState(null);
  const [loading, setLoading] = useState(true);
  const [trendsLoading, setTrendsLoading] = useState(true);
  const [activityLoading, setActivityLoading] = useState(true);
  const [error, setError] = useState(null);
  const [trendsError, setTrendsError] = useState(null);
  const [activityError, setActivityError] = useState(null);

  const role = auth?.user?.role;
  const normalizedPreferences = useMemo(
    () => normalizeDashboardPreferences(preferences),
    [preferences],
  );

  const loadTrends = useCallback(async (trendRange) => {
    try {
      setTrendsLoading(true);
      setTrendsError(null);
      const data = await operationsIntelligenceApi.getTrends(buildTrendQueryFromRange(trendRange));
      setTrends(data);
    } catch (err) {
      setTrendsError(getApiErrorMessage(err, 'Failed to load operational trends.'));
    } finally {
      setTrendsLoading(false);
    }
  }, []);

  const loadDashboardData = useCallback(async (loadedPreferences) => {
    const prefs = normalizeDashboardPreferences(loadedPreferences);

    const loadKpis = async () => {
      try {
        setLoading(true);
        setError(null);
        const data = await operationsIntelligenceApi.getKpis();
        setKpis(data);
      } catch (err) {
        setError(getApiErrorMessage(err, 'Failed to load operational KPIs.'));
      } finally {
        setLoading(false);
      }
    };

    const loadRecentActivity = async () => {
      try {
        setActivityLoading(true);
        setActivityError(null);
        const data = await operationsIntelligenceApi.getRecentActivity({ limit: 20 });
        setRecentActivity(data);
      } catch (err) {
        setActivityError(getApiErrorMessage(err, 'Failed to load recent activity.'));
      } finally {
        setActivityLoading(false);
      }
    };

    await Promise.all([
      loadKpis(),
      loadTrends(prefs.defaultTrendRange),
      loadRecentActivity(),
    ]);
  }, [loadTrends]);

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    if (!canViewOperationsDashboard(role)) {
      navigate('/');
      return;
    }

    apiClient.setToken(auth.token);

    const loadPreferences = async () => {
      let prefsToUse;
      try {
        setPreferencesLoading(true);
        setPreferencesError(null);
        const data = await dashboardPreferencesApi.getPreferences();
        prefsToUse = normalizeDashboardPreferences(data);
        setPreferences(prefsToUse);
      } catch (err) {
        setPreferencesError(getApiErrorMessage(err, 'Failed to load dashboard preferences.'));
        prefsToUse = normalizeDashboardPreferences(null);
        setPreferences(prefsToUse);
      } finally {
        setPreferencesLoading(false);
      }

      await loadDashboardData(prefsToUse);
    };

    loadPreferences();
  }, [auth, navigate, role, loadDashboardData]);

  const attentionAlerts = useMemo(() => buildAttentionAlerts(kpis), [kpis]);

  const quickLinks = useMemo(
    () => QUICK_NAVIGATION_LINKS.filter((link) => canAccessRoute(role, link.path)),
    [role],
  );

  const handleSavePreferences = async (request) => {
    try {
      setSavingPreferences(true);
      const saved = await dashboardPreferencesApi.savePreferences(request);
      const normalized = normalizeDashboardPreferences(saved);
      setPreferences(normalized);
      setSettingsOpen(false);
      await loadTrends(normalized.defaultTrendRange);
    } catch (err) {
      setPreferencesError(getApiErrorMessage(err, 'Failed to save dashboard preferences.'));
    } finally {
      setSavingPreferences(false);
    }
  };

  const handleResetPreferences = async () => {
    try {
      setResettingPreferences(true);
      const reset = await dashboardPreferencesApi.resetPreferences();
      const normalized = normalizeDashboardPreferences(reset);
      setPreferences(normalized);
      setSettingsOpen(false);
      await loadTrends(normalized.defaultTrendRange);
    } catch (err) {
      setPreferencesError(getApiErrorMessage(err, 'Failed to reset dashboard preferences.'));
    } finally {
      setResettingPreferences(false);
    }
  };

  const renderWidget = (widgetType) => {
    if (!isWidgetVisible(normalizedPreferences, widgetType)) {
      return null;
    }

    switch (widgetType) {
      case DASHBOARD_WIDGET_TYPES.OVERVIEW:
        if (loading || error || !kpis) {
          return null;
        }
        return <KpiOverviewWidget key={widgetType} kpis={kpis} />;
      case DASHBOARD_WIDGET_TYPES.ATTENTION:
        if (loading || error || !kpis) {
          return null;
        }
        return <AttentionRequiredWidget key={widgetType} alerts={attentionAlerts} />;
      case DASHBOARD_WIDGET_TYPES.QUICK_NAVIGATION:
        if (loading || error || !kpis) {
          return null;
        }
        return (
          <QuickNavigationWidget
            key={widgetType}
            links={quickLinks}
            onNavigate={navigate}
          />
        );
      case DASHBOARD_WIDGET_TYPES.TRENDS:
        return (
          <TrendWidget
            key={widgetType}
            trends={trends}
            loading={trendsLoading}
            error={trendsError}
          />
        );
      case DASHBOARD_WIDGET_TYPES.RECENT_ACTIVITY:
        return (
          <RecentActivityWidget
            key={widgetType}
            items={recentActivity?.items}
            loading={activityLoading}
            error={activityError}
            onNavigate={navigate}
          />
        );
      default:
        return null;
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!auth || !canViewOperationsDashboard(role)) {
    return null;
  }

  return (
    <div className="dashboard-page">
      <AppNavbar onNavigate={navigate} onLogout={handleLogout} />

      <main className="dashboard-content">
        <header className="dashboard-greeting">
          <div className="dashboard-greeting-row">
            <div>
              <h1>
                Good morning,
                {' '}
                {getDisplayName(auth.user)}
              </h1>
              <p>Operational overview</p>
            </div>
            <button
              type="button"
              className="dashboard-settings-button"
              onClick={() => setSettingsOpen(true)}
            >
              Dashboard Settings
            </button>
          </div>
        </header>

        {preferencesLoading && (
          <div className="dashboard-state-message">Loading dashboard preferences...</div>
        )}

        {!preferencesLoading && preferencesError && (
          <div className="dashboard-state-message error">{preferencesError}</div>
        )}

        {loading && (
          <div className="dashboard-state-message">Loading operational KPIs...</div>
        )}

        {!loading && error && (
          <div className="dashboard-state-message error">{error}</div>
        )}

        {!preferencesLoading && normalizedPreferences.widgetOrder.map(renderWidget)}
      </main>

      <DashboardSettingsPanel
        isOpen={settingsOpen}
        preferences={normalizedPreferences}
        onClose={() => setSettingsOpen(false)}
        onSave={handleSavePreferences}
        onReset={handleResetPreferences}
        saving={savingPreferences}
        resetting={resettingPreferences}
      />

      <footer className="platform-footer">
        InfraTrack v
        {APP_VERSION}
      </footer>
    </div>
  );
}
