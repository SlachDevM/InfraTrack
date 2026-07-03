import { getPreventiveMaintenancePlanStatusLabel } from '../../constants/preventiveMaintenancePlanStatuses';
import { getPreventiveMaintenancePlanPriorityLabel } from '../../constants/preventiveMaintenancePlanPriorities';
import { getPlanTargetActionLabel } from '../../constants/planTargetActions';
import { formatTimestamp } from '../../pages/preventiveMaintenancePlans/constants';

function PlanEvaluationResult({ result }) {
  return (
    <div className="evaluation-result">
      <span
        className={
          result.eligible
            ? 'evaluation-badge evaluation-badge-eligible'
            : 'evaluation-badge evaluation-badge-not-eligible'
        }
      >
        {result.eligible ? 'Eligible' : 'Not eligible'}
      </span>
      <div className="evaluation-reason">{result.evaluationReason}</div>
      <div className="evaluation-timestamp">Evaluated: {formatTimestamp(result.evaluatedAt)}</div>
      {result.nextEligibleAt && (
        <div className="evaluation-timestamp">
          Next eligible: {formatTimestamp(result.nextEligibleAt)}
        </div>
      )}
    </div>
  );
}

export default function PreventivePlanList({
  plans,
  canManage,
  evaluationResults,
  evaluatingId,
  onEvaluate,
  onEdit,
  onArchive,
}) {
  return (
    <section>
      <h2>Plans</h2>
      {plans.length === 0 ? (
        <p className="no-items">No preventive maintenance plans found.</p>
      ) : (
        <table className="reference-table">
          <thead>
            <tr>
              <th>Plan Code</th>
              <th>Name</th>
              <th>Asset</th>
              <th>Trigger Summary</th>
              <th>Target Action</th>
              <th>Status</th>
              <th>Priority</th>
              <th>Updated</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {plans.map((plan) => (
              <tr key={plan.id}>
                <td>{plan.planCode}</td>
                <td>{plan.name}</td>
                <td>{plan.assetName}</td>
                <td title={plan.businessTrigger?.triggerSummary?.description}>
                  {plan.businessTrigger?.triggerSummary?.title || '-'}
                </td>
                <td>{getPlanTargetActionLabel(plan.targetAction)}</td>
                <td>{getPreventiveMaintenancePlanStatusLabel(plan.status)}</td>
                <td>{getPreventiveMaintenancePlanPriorityLabel(plan.priority)}</td>
                <td>{formatTimestamp(plan.updatedAt)}</td>
                <td>
                  <button
                    type="button"
                    className="btn-link"
                    onClick={() => onEvaluate(plan.id)}
                    disabled={evaluatingId === plan.id}
                  >
                    {evaluatingId === plan.id ? 'Evaluating...' : 'Evaluate'}
                  </button>
                  {evaluationResults[plan.id] && (
                    <PlanEvaluationResult result={evaluationResults[plan.id]} />
                  )}
                  {canManage && plan.status !== 'ARCHIVED' && (
                    <>
                      {' '}
                      <button type="button" className="btn-link" onClick={() => onEdit(plan)}>
                        Edit
                      </button>{' '}
                      <button type="button" className="btn-link" onClick={() => onArchive(plan.id)}>
                        Archive
                      </button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
