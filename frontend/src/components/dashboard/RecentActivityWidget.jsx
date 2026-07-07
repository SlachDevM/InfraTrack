import DashboardWidget from './DashboardWidget';
import { formatActivityTime, formatActivityTypeLabel } from '../../utils/activityFeed';

export default function RecentActivityWidget({ items, loading, error, onNavigate }) {
  const safeItems = Array.isArray(items) ? items : [];
  const isEmpty = safeItems.length === 0;

  return (
    <DashboardWidget
      title="Recent activity"
      ariaLabel="Recent activity"
      loading={loading}
      loadingMessage="Loading recent activity..."
      error={!loading ? error : null}
      empty={!loading && !error && isEmpty}
      emptyMessage="No recent operational activity."
    >
      <ul className="dashboard-activity-list">
        {safeItems.map((item, index) => (
          <li key={`${item.type}-${item.occurredAt}-${index}`}>
            <button
              type="button"
              className="dashboard-activity-item"
              onClick={() => item.route && onNavigate(item.route)}
              disabled={!item.route}
              aria-label={
                item.route
                  ? `${formatActivityTypeLabel(item.type)}: ${item.title}`
                  : `${formatActivityTypeLabel(item.type)}: ${item.title} (no link)`
              }
            >
              <div className="dashboard-activity-item-header">
                <span className="dashboard-activity-type">
                  {formatActivityTypeLabel(item.type)}
                </span>
                <time
                  className="dashboard-activity-time"
                  dateTime={new Date(item.occurredAt).toISOString()}
                >
                  {formatActivityTime(item.occurredAt)}
                </time>
              </div>
              <div className="dashboard-activity-title">{item.title}</div>
              <div className="dashboard-activity-description">
                {item.description || item.assetName}
              </div>
            </button>
          </li>
        ))}
      </ul>
    </DashboardWidget>
  );
}
