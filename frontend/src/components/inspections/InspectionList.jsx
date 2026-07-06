import { getBusinessTriggerTypeLabel } from '../../constants/businessTriggerTypes';
import { getInspectionPriorityLabel } from '../../constants/inspectionPriorities';
import { INSPECTION_STATUS } from '../../constants/statuses';
import { getPhysicalConditionLabel } from '../../constants/physicalConditions';
import { COMMON_MESSAGES } from '../../constants/messages';
import RuleEvaluationReportPanel from './RuleEvaluationReportPanel';
import DecisionAssistantPanel from './DecisionAssistantPanel';

export default function InspectionList({ inspections }) {
  const completedTemplatedInspections = inspections.filter(
    (inspection) =>
      inspection.status === INSPECTION_STATUS.COMPLETED && inspection.inspectionTemplateId != null
  );

  return (
    <section className="inspection-list-section">
      <h2>Inspections</h2>
      {inspections.length === 0 ? (
        <p className="empty-state no-items">{COMMON_MESSAGES.NO_INSPECTIONS}</p>
      ) : (
        <div className="table-scroll">
          <table className="reference-table inspections-table" aria-label="Inspections">
            <thead>
              <tr>
                <th>Asset</th>
                <th>Trigger</th>
                <th>Assigned To</th>
                <th>Priority</th>
                <th>Status</th>
                <th>Condition</th>
                <th>Issue</th>
                <th>Expected By</th>
                <th>Completed</th>
                <th>Created</th>
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
                  <td>{inspection.assignedToUserName || inspection.assignedToUserId}</td>
                  <td>{getInspectionPriorityLabel(inspection.priority)}</td>
                  <td>{inspection.status}</td>
                  <td>
                    {inspection.observedCondition
                      ? getPhysicalConditionLabel(inspection.observedCondition)
                      : '-'}
                  </td>
                  <td>{inspection.issueIdentified ? 'Yes' : 'No'}</td>
                  <td>{inspection.expectedCompletionDate || '-'}</td>
                  <td>
                    {inspection.completedAt
                      ? new Date(inspection.completedAt).toLocaleString()
                      : '-'}
                  </td>
                  <td>
                    {inspection.createdAt ? new Date(inspection.createdAt).toLocaleString() : '-'}
                  </td>
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
