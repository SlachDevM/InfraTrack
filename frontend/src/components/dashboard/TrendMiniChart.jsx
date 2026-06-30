import { getTrendMaxCount, isTrendSeriesEmpty } from '../../utils/trendChart';

export default function TrendMiniChart({ title, dataPoints }) {
  const empty = isTrendSeriesEmpty(dataPoints);
  const maxCount = getTrendMaxCount(dataPoints);

  return (
    <article className="dashboard-card dashboard-trend-card">
      <h3>{title}</h3>
      {empty ? (
        <p className="dashboard-trend-empty">No activity in this period.</p>
      ) : (
        <div className="dashboard-trend-chart" role="img" aria-label={`${title} trend`}>
          {dataPoints.map((point) => {
            const heightPercent = maxCount > 0 ? (point.count / maxCount) * 100 : 0;
            return (
              <div key={point.period} className="dashboard-trend-bar-wrapper" title={`${point.period}: ${point.count}`}>
                <div
                  className="dashboard-trend-bar"
                  style={{ height: `${Math.max(heightPercent, point.count > 0 ? 8 : 0)}%` }}
                />
                <span className="dashboard-trend-bar-label">{point.period.slice(5)}</span>
              </div>
            );
          })}
        </div>
      )}
      {!empty && (
        <p className="dashboard-trend-total">
          Total:
          {' '}
          <strong>{dataPoints.reduce((sum, point) => sum + (point.count ?? 0), 0)}</strong>
        </p>
      )}
    </article>
  );
}
