import { getBusinessTriggerTypeLabel } from '../../constants/businessTriggerTypes';
import { getInspectionPriorityLabel } from '../../constants/inspectionPriorities';
import { getPhysicalConditionLabel } from '../../constants/physicalConditions';
import RuleEvaluationReportPanel from './RuleEvaluationReportPanel';

export default function InspectionList({ inspections }) {
  const completedTemplatedInspections = inspections.filter(
    (inspection) =>
      inspection.status === 'COMPLETED' && inspection.inspectionTemplateId != null
  );

  return (
    <section className="inspection-list-section">
      <h2>Inspections</h2>
      {inspections.length === 0 ? (
        <p className="no-items">No inspections assigned yet.</p>
      ) : (
        <table className="reference-table inspections-table">
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
                  #{inspection.businessTriggerId} — {getBusinessTriggerTypeLabel(inspection.businessTriggerType)}
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
                  {inspection.createdAt
                    ? new Date(inspection.createdAt).toLocaleString()
                    : '-'}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}

      {completedTemplatedInspections.length > 0 && (
        <div className="rule-evaluation-reports-section">
          <h2>Rule Evaluation Reports</h2>
          {completedTemplatedInspections.map((inspection) => (
            <RuleEvaluationReportPanel
              key={inspection.id}
              inspectionId={inspection.id}
              assetName={inspection.assetName}
            />
          ))}
        </div>
      )}
    </section>
  );
}
