import { getBusinessTriggerTypeLabel } from '../../constants/businessTriggerTypes';
import {
  getInspectionPriorityLabel,
  getInspectionStatusLabel,
} from '../../constants/inspectionPriorities';
import { INSPECTION_STATUS } from '../../constants/statuses';
import { getPhysicalConditionLabel } from '../../constants/physicalConditions';
import { COMMON_MESSAGES } from '../../constants/messages';
import RuleEvaluationReportPanel from './RuleEvaluationReportPanel';
import DecisionAssistantPanel from './DecisionAssistantPanel';
import { formatDateTime } from '../../utils/dateTime';

function formatAssignedUser(inspection) {
  if (inspection.assignedToUserName?.trim()) {
    return inspection.assignedToUserName.trim();
  }
  if (inspection.assignedToUserId != null) {
    return `User #${inspection.assignedToUserId}`;
  }
  return 'Unassigned';
}

export default function InspectionList({ inspections }) {
  const completedTemplatedInspections = inspections.filter(
    (inspection) =>
      inspection.status === INSPECTION_STATUS.COMPLETED && inspection.inspectionTemplateId != null
  );

  return (
    <section className="inspection-list-section" aria-labelledby="inspections-heading">
      <h2 id="inspections-heading">Inspections</h2>
      {inspections.length === 0 ? (
        <p className="empty-state no-items" role="status">
          {COMMON_MESSAGES.NO_INSPECTIONS}
        </p>
      ) : (
        <div className="table-scroll">
          <table className="reference-table inspections-table" aria-label="Inspections">
            <thead>
              <tr>
                <th scope="col">Asset</th>
                <th scope="col">Trigger</th>
                <th scope="col">Assigned To</th>
                <th scope="col">Priority</th>
                <th scope="col">Status</th>
                <th scope="col">Condition</th>
                <th scope="col">Issue</th>
                <th scope="col">Expected By</th>
                <th scope="col">Completed</th>
                <th scope="col">Created</th>
              </tr>
            </thead>
            <tbody>
              {inspections.map((inspection) => (
                <tr key={inspection.id}>
                  <td>{inspection.assetName}</td>
                  <td>
                    #{inspection.businessTriggerId} —{' '}
                    {getBusinessTriggerTypeLabel(inspection.businessTriggerType)}
                  </td>
                  <td>{formatAssignedUser(inspection)}</td>
                  <td>{getInspectionPriorityLabel(inspection.priority)}</td>
                  <td>
                    <span className={`status-badge status-${inspection.status?.toLowerCase()}`}>
                      {getInspectionStatusLabel(inspection.status)}
                    </span>
                  </td>
                  <td>
                    {inspection.observedCondition
                      ? getPhysicalConditionLabel(inspection.observedCondition)
                      : '—'}
                  </td>
                  <td>{inspection.issueIdentified ? 'Yes' : 'No'}</td>
                  <td>{inspection.expectedCompletionDate || '—'}</td>
                  <td>{formatDateTime(inspection.completedAt)}</td>
                  <td>{formatDateTime(inspection.createdAt)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {completedTemplatedInspections.length > 0 && (
        <div className="rule-evaluation-reports-section">
          <h2>Rule Evaluation Reports</h2>
          {completedTemplatedInspections.map((inspection) => (
            <div key={inspection.id} className="inspection-evaluation-block">
              <RuleEvaluationReportPanel
                inspectionId={inspection.id}
                assetName={inspection.assetName}
              />
              <DecisionAssistantPanel inspectionId={inspection.id} />
            </div>
          ))}
        </div>
      )}
    </section>
  );
}
