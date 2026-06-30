import DashboardWidget from './DashboardWidget';
import TrendMiniChart from './TrendMiniChart';

const TREND_SERIES = [
  { key: 'inspectionsCompleted', title: 'Inspections completed' },
  { key: 'issuesCreated', title: 'Issues created' },
  { key: 'workOrdersCompleted', title: 'Work orders completed' },
  { key: 'preventiveCandidatesGenerated', title: 'Preventive candidates generated' },
  { key: 'suggestedActionsAccepted', title: 'Suggested actions accepted' },
];

export default function TrendWidget({ trends, loading, error }) {
  const hasSeries = Boolean(
    trends?.series && TREND_SERIES.some(({ key }) => trends.series[key]?.length),
  );

  return (
    <DashboardWidget
      title="Operational trends (last 30 days)"
      ariaLabel="Operational trends"
      loading={loading}
      loadingMessage="Loading operational trends..."
      error={!loading ? error : null}
      empty={!loading && !error && !hasSeries}
      emptyMessage="No trend data available."
    >
      {hasSeries && (
        <div className="dashboard-trend-grid">
          {TREND_SERIES.map(({ key, title }) => (
            <TrendMiniChart
              key={key}
              title={title}
              dataPoints={trends.series[key]}
            />
          ))}
        </div>
      )}
    </DashboardWidget>
  );
}
