import { useEffect, useState } from 'react';
import { useAuth } from '../../context/AuthContext';
import suggestedActionApi from '../../services/suggestedActionApi';
import { canReviewSuggestedActions } from '../../constants/userRoles';
import { SUGGESTED_ACTION_STATUS } from '../../constants/statuses';
import { ISSUE_SEVERITIES } from '../../constants/issueSeverities';
import { getApiErrorMessage } from '../../utils/apiError';

function formatTimestamp(value) {
  if (!value) {
    return '-';
  }
  return new Date(value).toLocaleString();
}

function toDateTimeLocalValue(date = new Date()) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}

function mapSeverityToIssueSeverity(severity) {
  if (!severity) {
    return ISSUE_SEVERITIES.MEDIUM;
  }
  const normalized = String(severity).toUpperCase();
  if (Object.values(ISSUE_SEVERITIES).includes(normalized)) {
    return normalized;
  }
  return ISSUE_SEVERITIES.MEDIUM;
}

function SuggestionWhyPanel({ explanation }) {
  if (!explanation) {
    return null;
  }

  return (
    <div className="suggestion-why-panel" data-testid="suggestion-why-panel">
      <h5>Why was this suggestion generated?</h5>
      <dl>
        <div>
          <dt>Matched rule</dt>
          <dd>{explanation.matchedRuleCode}</dd>
        </div>
        <div>
          <dt>Condition</dt>
          <dd>{explanation.conditionDescription}</dd>
        </div>
        <div>
          <dt>Actual value</dt>
          <dd>{explanation.actualValue ?? '-'}</dd>
        </div>
        <div>
          <dt>Configured action</dt>
          <dd>{explanation.configuredActionDescription}</dd>
        </div>
      </dl>
    </div>
  );
}

function ApproveSuggestionDialog({ suggestion, onClose, onApproved, onError }) {
  const [formData, setFormData] = useState({
    title: suggestion.title || '',
    description: suggestion.message || '',
    severity: mapSeverityToIssueSeverity(suggestion.severity),
    rootCause: '',
    correctiveAction: '',
    preventiveAction: '',
    recordedAt: toDateTimeLocalValue(),
  });
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      setSubmitting(true);
      const response = await suggestedActionApi.approve(suggestion.id, {
        ...formData,
        recordedAt: `${formData.recordedAt}:00`,
      });
      onApproved(response.suggestedAction);
    } catch (err) {
      onError(getApiErrorMessage(err, 'Failed to approve suggested action.'));
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="decision-assistant-dialog-backdrop" data-testid="approve-suggestion-dialog">
      <form className="decision-assistant-dialog" onSubmit={handleSubmit}>
        <h5>Approve and Create Issue</h5>
        <label>
          Issue title
          <input name="title" value={formData.title} onChange={handleChange} required />
        </label>
        <label>
          Issue description
          <textarea
            name="description"
            value={formData.description}
            onChange={handleChange}
            required
            rows={4}
          />
        </label>
        <label>
          Severity
          <select name="severity" value={formData.severity} onChange={handleChange}>
            {Object.values(ISSUE_SEVERITIES).map((severity) => (
              <option key={severity} value={severity}>
                {severity}
              </option>
            ))}
          </select>
        </label>
        <label>
          Root cause
          <textarea name="rootCause" value={formData.rootCause} onChange={handleChange} rows={2} />
        </label>
        <label>
          Corrective action
          <textarea
            name="correctiveAction"
            value={formData.correctiveAction}
            onChange={handleChange}
            rows={2}
          />
        </label>
        <label>
          Preventive action
          <textarea
            name="preventiveAction"
            value={formData.preventiveAction}
            onChange={handleChange}
            rows={2}
          />
        </label>
        <label>
          Recorded at
          <input
            type="datetime-local"
            name="recordedAt"
            value={formData.recordedAt}
            onChange={handleChange}
            required
          />
        </label>
        <div className="decision-assistant-dialog-actions">
          <button type="button" onClick={onClose} disabled={submitting}>
            Cancel
          </button>
          <button type="submit" disabled={submitting}>
            {submitting ? 'Creating Issue...' : 'Create Issue'}
          </button>
        </div>
      </form>
    </div>
  );
}

function SuggestionReviewCard({ summary, canReview, onRefresh, onError }) {
  const [detail, setDetail] = useState(null);
  const [loadingDetail, setLoadingDetail] = useState(false);
  const [expanded, setExpanded] = useState(false);
  const [showApprove, setShowApprove] = useState(false);
  const [rejectReason, setRejectReason] = useState('');
  const [dismissComment, setDismissComment] = useState('');
  const [acting, setActing] = useState(false);

  const display = detail || summary;
  const isPending = (detail?.status ?? summary.status) === SUGGESTED_ACTION_STATUS.PENDING;

  useEffect(() => {
    if (!expanded || detail || !canReview) {
      return;
    }
    let cancelled = false;
    async function loadDetail() {
      try {
        setLoadingDetail(true);
        const loaded = await suggestedActionApi.getDetail(summary.id);
        if (!cancelled) {
          setDetail(loaded);
        }
      } catch (err) {
        if (!cancelled) {
          onError(getApiErrorMessage(err, 'Failed to load suggestion detail.'));
        }
      } finally {
        if (!cancelled) {
          setLoadingDetail(false);
        }
      }
    }
    loadDetail();
    return () => {
      cancelled = true;
    };
  }, [expanded, detail, canReview, summary.id, onError]);

  const handleReject = async () => {
    try {
      setActing(true);
      const updated = await suggestedActionApi.reject(summary.id, { reason: rejectReason });
      setDetail(updated);
      onRefresh();
    } catch (err) {
      onError(getApiErrorMessage(err, 'Failed to reject suggested action.'));
    } finally {
      setActing(false);
    }
  };

  const handleDismiss = async () => {
    try {
      setActing(true);
      const updated = await suggestedActionApi.dismiss(summary.id, { comment: dismissComment });
      setDetail(updated);
      onRefresh();
    } catch (err) {
      onError(getApiErrorMessage(err, 'Failed to dismiss suggested action.'));
    } finally {
      setActing(false);
    }
  };

  return (
    <li data-testid={`suggested-action-${summary.id}`}>
      <div className="suggested-action-header">
        <strong>{display.title}</strong>
        <span className="suggested-action-status">{display.status}</span>
      </div>
      <div className="suggestion-confidence-badge" data-testid={`confidence-${summary.id}`}>
        Confidence: {display.confidence || summary.confidence || 'LOW'}
      </div>
      <dl className="suggested-action-details">
        <div>
          <dt>Action type</dt>
          <dd>{display.actionType}</dd>
        </div>
        {display.severity && (
          <div>
            <dt>Severity</dt>
            <dd>{display.severity}</dd>
          </div>
        )}
        <div>
          <dt>Source rules</dt>
          <dd>{display.sourceRuleCodes}</dd>
        </div>
        <div>
          <dt>Created</dt>
          <dd>{formatTimestamp(display.createdAt)}</dd>
        </div>
      </dl>
      {display.message && <p className="suggested-action-message">{display.message}</p>}

      {canReview && (
        <>
          <button
            type="button"
            className="decision-assistant-expand-btn"
            onClick={() => setExpanded((prev) => !prev)}
          >
            {expanded ? 'Hide Decision Assistant' : 'Open Decision Assistant'}
          </button>
          {expanded && (
            <div
              className="decision-assistant-review"
              data-testid={`decision-assistant-${summary.id}`}
            >
              {loadingDetail && <p>Loading explanation...</p>}
              {!loadingDetail && detail?.explanation && (
                <SuggestionWhyPanel explanation={detail.explanation} />
              )}
              {detail && (
                <dl className="suggested-action-details">
                  <div>
                    <dt>Template version</dt>
                    <dd>{detail.evaluationReportTemplateVersion ?? '-'}</dd>
                  </div>
                  <div>
                    <dt>Evaluated at</dt>
                    <dd>{formatTimestamp(detail.evaluationReportEvaluatedAt)}</dd>
                  </div>
                  <div>
                    <dt>Report status</dt>
                    <dd>{detail.evaluationReportStatus}</dd>
                  </div>
                </dl>
              )}
              {isPending && (
                <div className="decision-assistant-actions">
                  <button
                    type="button"
                    onClick={() => setShowApprove(true)}
                    disabled={acting}
                    data-testid={`approve-btn-${summary.id}`}
                  >
                    Approve
                  </button>
                  <div className="decision-assistant-inline-form">
                    <input
                      placeholder="Rejection reason (optional)"
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                    />
                    <button
                      type="button"
                      onClick={handleReject}
                      disabled={acting}
                      data-testid={`reject-btn-${summary.id}`}
                    >
                      Reject
                    </button>
                  </div>
                  <div className="decision-assistant-inline-form">
                    <input
                      placeholder="Dismiss comment (optional)"
                      value={dismissComment}
                      onChange={(e) => setDismissComment(e.target.value)}
                    />
                    <button
                      type="button"
                      onClick={handleDismiss}
                      disabled={acting}
                      data-testid={`dismiss-btn-${summary.id}`}
                    >
                      Dismiss
                    </button>
                  </div>
                </div>
              )}
              {display.createdIssueId && (
                <p className="read-only-note">Linked Issue #{display.createdIssueId}</p>
              )}
            </div>
          )}
          {showApprove && (
            <ApproveSuggestionDialog
              suggestion={display}
              onClose={() => setShowApprove(false)}
              onApproved={(updated) => {
                setDetail(updated);
                setShowApprove(false);
                onRefresh();
              }}
              onError={onError}
            />
          )}
        </>
      )}
    </li>
  );
}

export default function DecisionAssistantPanel({ inspectionId }) {
  const { auth } = useAuth();
  const canReview = canReviewSuggestedActions(auth?.user?.role);
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const loadSuggestions = async () => {
    try {
      setLoading(true);
      setError(null);
      const items = await suggestedActionApi.list(inspectionId);
      setSuggestions(items);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load suggested actions.'));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSuggestions();
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
      className="suggested-actions-panel decision-assistant-panel"
      data-testid={`suggested-actions-panel-${inspectionId}`}
      aria-label={`Suggested actions for inspection ${inspectionId}`}
    >
      <h4>{canReview ? 'Decision Assistant' : 'Suggested Actions'}</h4>
      {suggestions.length === 0 ? (
        <p className="read-only-note" data-testid={`suggested-actions-empty-${inspectionId}`}>
          No suggested actions were generated.
        </p>
      ) : (
        <ul className="suggested-actions-list">
          {suggestions.map((suggestion) => (
            <SuggestionReviewCard
              key={suggestion.id}
              summary={suggestion}
              canReview={canReview}
              onRefresh={loadSuggestions}
              onError={setError}
            />
          ))}
        </ul>
      )}
    </section>
  );
}
