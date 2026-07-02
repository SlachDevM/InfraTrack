import { useEffect, useState } from 'react';
import ConfirmDialog from '../ConfirmDialog';
import {
  DASHBOARD_TREND_RANGE_LABELS,
  DASHBOARD_TREND_RANGES,
  DASHBOARD_WIDGET_LABELS,
} from '../../constants/dashboardPreferences';
import {
  countVisibleWidgets,
  moveWidget,
  preferencesToRequest,
} from '../../utils/dashboardPreferences';

export default function DashboardSettingsPanel({
  isOpen,
  preferences,
  onClose,
  onSave,
  onReset,
  saving = false,
  resetting = false,
}) {
  const [draft, setDraft] = useState(preferences);
  const [showResetConfirm, setShowResetConfirm] = useState(false);
  const [validationError, setValidationError] = useState(null);

  useEffect(() => {
    if (isOpen) {
      setDraft(preferences);
      setValidationError(null);
    }
  }, [isOpen, preferences]);

  if (!isOpen || !draft) {
    return null;
  }

  const updateVisibility = (field, checked) => {
    const next = { ...draft, [field]: checked };
    if (countVisibleWidgets(next) === 0) {
      setValidationError('At least one dashboard widget must remain visible.');
      return;
    }
    setValidationError(null);
    setDraft(next);
  };

  const handleMove = (widgetType, direction) => {
    setDraft({
      ...draft,
      widgetOrder: moveWidget(draft.widgetOrder, widgetType, direction),
    });
  };

  const handleSave = () => {
    if (countVisibleWidgets(draft) === 0) {
      setValidationError('At least one dashboard widget must remain visible.');
      return;
    }
    onSave(preferencesToRequest(draft));
  };

  return (
    <>
      <div className="modal-overlay" onClick={onClose}>
        <div
          className="dashboard-settings-panel"
          onClick={(event) => event.stopPropagation()}
          role="dialog"
          aria-labelledby="dashboard-settings-title"
        >
          <header className="dashboard-settings-header">
            <h2 id="dashboard-settings-title">Dashboard Settings</h2>
            <button type="button" className="dashboard-settings-close" onClick={onClose}>
              Close
            </button>
          </header>

          <section className="dashboard-settings-section">
            <h3>Widget visibility</h3>
            <label className="dashboard-settings-checkbox">
              <input
                type="checkbox"
                checked={draft.showOverviewWidget}
                onChange={(event) => updateVisibility('showOverviewWidget', event.target.checked)}
              />
              KPI Overview
            </label>
            <label className="dashboard-settings-checkbox">
              <input
                type="checkbox"
                checked={draft.showAttentionWidget}
                onChange={(event) => updateVisibility('showAttentionWidget', event.target.checked)}
              />
              Attention Required
            </label>
            <label className="dashboard-settings-checkbox">
              <input
                type="checkbox"
                checked={draft.showTrendWidget}
                onChange={(event) => updateVisibility('showTrendWidget', event.target.checked)}
              />
              Trends
            </label>
            <label className="dashboard-settings-checkbox">
              <input
                type="checkbox"
                checked={draft.showRecentActivityWidget}
                onChange={(event) =>
                  updateVisibility('showRecentActivityWidget', event.target.checked)
                }
              />
              Recent Activity
            </label>
            <label className="dashboard-settings-checkbox">
              <input
                type="checkbox"
                checked={draft.showQuickNavigationWidget}
                onChange={(event) =>
                  updateVisibility('showQuickNavigationWidget', event.target.checked)
                }
              />
              Quick Navigation
            </label>
          </section>

          <section className="dashboard-settings-section">
            <h3>Trend range</h3>
            <select
              className="dashboard-settings-select"
              value={draft.defaultTrendRange}
              onChange={(event) => setDraft({ ...draft, defaultTrendRange: event.target.value })}
            >
              {Object.values(DASHBOARD_TREND_RANGES).map((range) => (
                <option key={range} value={range}>
                  {DASHBOARD_TREND_RANGE_LABELS[range]}
                </option>
              ))}
            </select>
          </section>

          <section className="dashboard-settings-section">
            <h3>Widget order</h3>
            <ul className="dashboard-settings-order-list">
              {draft.widgetOrder.map((widgetType, index) => (
                <li key={widgetType} className="dashboard-settings-order-item">
                  <span>{DASHBOARD_WIDGET_LABELS[widgetType]}</span>
                  <div className="dashboard-settings-order-actions">
                    <button
                      type="button"
                      aria-label={`Move ${DASHBOARD_WIDGET_LABELS[widgetType]} up`}
                      disabled={index === 0}
                      onClick={() => handleMove(widgetType, 'up')}
                    >
                      ▲
                    </button>
                    <button
                      type="button"
                      aria-label={`Move ${DASHBOARD_WIDGET_LABELS[widgetType]} down`}
                      disabled={index === draft.widgetOrder.length - 1}
                      onClick={() => handleMove(widgetType, 'down')}
                    >
                      ▼
                    </button>
                  </div>
                </li>
              ))}
            </ul>
          </section>

          {validationError && <div className="dashboard-settings-error">{validationError}</div>}

          <footer className="dashboard-settings-footer">
            <button
              type="button"
              className="dashboard-settings-reset"
              onClick={() => setShowResetConfirm(true)}
              disabled={saving || resetting}
            >
              Reset Dashboard
            </button>
            <div className="dashboard-settings-footer-actions">
              <button type="button" className="btn-cancel" onClick={onClose} disabled={saving}>
                Cancel
              </button>
              <button
                type="button"
                className="btn-confirm"
                onClick={handleSave}
                disabled={saving || resetting}
              >
                {saving ? 'Saving...' : 'Save'}
              </button>
            </div>
          </footer>
        </div>
      </div>

      {showResetConfirm && (
        <ConfirmDialog
          title="Reset Dashboard"
          message="Restore default widget visibility, order, and trend range?"
          confirmLabel="Reset Dashboard"
          onConfirm={() => {
            setShowResetConfirm(false);
            onReset();
          }}
          onCancel={() => setShowResetConfirm(false)}
          isLoading={resetting}
        />
      )}
    </>
  );
}
