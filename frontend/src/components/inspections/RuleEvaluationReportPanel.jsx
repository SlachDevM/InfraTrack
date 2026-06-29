import { useEffect, useState } from 'react';
import ruleEvaluationReportApi from '../../services/ruleEvaluationReportApi';
import { getApiErrorMessage } from '../../utils/apiError';

function formatTimestamp(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
}

export default function RuleEvaluationReportPanel({ inspectionId, assetName }) {
  const [report, setReport] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function loadReport() {
      try {
        setLoading(true);
        setError(null);
        const latestReport = await ruleEvaluationReportApi.getLatest(inspectionId);
        if (!cancelled) {
          setReport(latestReport);
        }
      } catch (err) {
        if (!cancelled) {
          if (err?.response?.status === 404) {
            setReport(null);
          } else {
            setError(getApiErrorMessage(err, 'Failed to load rule evaluation report.'));
          }
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadReport();
    return () => {
      cancelled = true;
    };
  }, [inspectionId]);

  if (loading) {
    return (
      <div className="rule-evaluation-panel loading" data-testid={`rule-report-loading-${inspectionId}`}>
        Loading rule evaluation report...
      </div>
    );
  }

  if (error) {
    return (
      <div className="rule-evaluation-panel error" data-testid={`rule-report-error-${inspectionId}`}>
        {error}
      </div>
    );
  }

  if (!report) {
    return null;
  }

  return (
    <section
      className="rule-evaluation-panel"
      data-testid={`rule-report-panel-${inspectionId}`}
      aria-label={`Rule evaluation report for inspection ${inspectionId}`}
    >
      <h3>
        Rule Evaluation Report
        {assetName ? ` — ${assetName}` : ''}
      </h3>
      <dl className="rule-evaluation-summary">
        <div>
          <dt>Evaluated at</dt>
          <dd>{formatTimestamp(report.evaluatedAt)}</dd>
        </div>
        <div>
          <dt>Engine version</dt>
          <dd>{report.engineVersion}</dd>
        </div>
        <div>
          <dt>Rules evaluated</dt>
          <dd>{report.resultCount}</dd>
        </div>
        <div>
          <dt>Matched</dt>
          <dd>{report.matchedCount}</dd>
        </div>
        <div>
          <dt>Duration (ms)</dt>
          <dd>{report.evaluationDurationMs}</dd>
        </div>
      </dl>
      {report.results?.length > 0 ? (
        <table className="reference-table rule-evaluation-results-table">
          <thead>
            <tr>
              <th>Rule code</th>
              <th>Rule name</th>
              <th>Actual value</th>
              <th>Operator</th>
              <th>Comparison value</th>
              <th>Matched</th>
              <th>Action type</th>
              <th>Priority</th>
            </tr>
          </thead>
          <tbody>
            {report.results.map((result) => (
              <tr key={result.id} data-testid={`rule-result-${result.ruleCodeSnapshot}`}>
                <td>{result.ruleCodeSnapshot}</td>
                <td>{result.ruleNameSnapshot}</td>
                <td>{result.actualValueSnapshot ?? '-'}</td>
                <td>{result.operatorSnapshot}</td>
                <td>{result.comparisonValueSnapshot ?? '-'}</td>
                <td>{result.matched ? 'Yes' : 'No'}</td>
                <td>{result.actionTypeSnapshot}</td>
                <td>{result.prioritySnapshot}</td>
              </tr>
            ))}
          </tbody>
        </table>
      ) : (
        <p className="read-only-note">No active rules were evaluated for this inspection.</p>
      )}
    </section>
  );
}
