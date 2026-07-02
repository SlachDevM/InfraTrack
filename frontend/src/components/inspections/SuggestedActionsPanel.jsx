import { useEffect, useState } from 'react';
import suggestedActionApi from '../../services/suggestedActionApi';
import { getApiErrorMessage } from '../../utils/apiError';

function formatTimestamp(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
}

export default function SuggestedActionsPanel({ inspectionId }) {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    let cancelled = false;

    async function loadSuggestions() {
      try {
        setLoading(true);
        setError(null);
        const items = await suggestedActionApi.list(inspectionId);
        if (!cancelled) {
          setSuggestions(items);
        }
      } catch (err) {
        if (!cancelled) {
          setError(getApiErrorMessage(err, 'Failed to load suggested actions.'));
        }
      } finally {
        if (!cancelled) {
          setLoading(false);
        }
      }
    }

    loadSuggestions();
    return () => {
      cancelled = true;
    };
  }, [inspectionId]);

  if (loading) {
    return (
      <div
        className="suggested-actions-panel loading"
        data-testid={`suggested-actions-loading-${inspectionId}`}
      >
        Loading suggested actions...
      </div>
    );
  }

  if (error) {
    return (
      <div
        className="suggested-actions-panel error"
        data-testid={`suggested-actions-error-${inspectionId}`}
      >
        {error}
      </div>
    );
  }

  return (
    <section
      className="suggested-actions-panel"
      data-testid={`suggested-actions-panel-${inspectionId}`}
      aria-label={`Suggested actions for inspection ${inspectionId}`}
    >
      <h4>Suggested Actions</h4>
      {suggestions.length === 0 ? (
        <p className="read-only-note" data-testid={`suggested-actions-empty-${inspectionId}`}>
          No suggested actions were generated.
        </p>
      ) : (
        <ul className="suggested-actions-list">
          {suggestions.map((suggestion) => (
            <li key={suggestion.id} data-testid={`suggested-action-${suggestion.id}`}>
              <div className="suggested-action-header">
                <strong>{suggestion.title}</strong>
                <span className="suggested-action-status">{suggestion.status}</span>
              </div>
              <dl className="suggested-action-details">
                <div>
                  <dt>Action type</dt>
                  <dd>{suggestion.actionType}</dd>
                </div>
                {suggestion.severity && (
                  <div>
                    <dt>Severity</dt>
                    <dd>{suggestion.severity}</dd>
                  </div>
                )}
                <div>
                  <dt>Source rules</dt>
                  <dd>{suggestion.sourceRuleCodes}</dd>
                </div>
                <div>
                  <dt>Matched rules</dt>
                  <dd>{suggestion.matchedRuleCount}</dd>
                </div>
                <div>
                  <dt>Created</dt>
                  <dd>{formatTimestamp(suggestion.createdAt)}</dd>
                </div>
              </dl>
              {suggestion.message && (
                <p className="suggested-action-message">{suggestion.message}</p>
              )}
            </li>
          ))}
        </ul>
      )}
    </section>
  );
}
