import { getInspectionTemplateQuestionTypeLabel } from '../../constants/inspectionTemplateQuestionTypes';
import { RULE_SUPPORTED_QUESTION_TYPES } from '../../constants/decisionRules';

export default function InspectionTemplateQuestionList({
  questions,
  activeQuestions,
  canMutate,
  canView,
  reordering,
  onEdit,
  onDeactivate,
  onMove,
  onManageChoices,
  onManageRules,
}) {
  return (
    <section>
      <h2>Questions</h2>
      {questions.length === 0 ? (
        <p className="no-items">No checklist questions defined yet.</p>
      ) : (
        <table className="reference-table">
          <thead>
            <tr>
              <th>Order</th>
              <th>Code</th>
              <th>Question</th>
              <th>Type</th>
              <th>Required</th>
              <th>Status</th>
              {(canMutate || canView) && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {questions.map((question) => (
              <tr key={question.id}>
                <td>{question.displayOrder}</td>
                <td>{question.code}</td>
                <td>
                  {question.questionText}
                  {question.helpText && <div className="help-text">{question.helpText}</div>}
                  {question.questionType === 'NUMBER' && (
                    <div className="help-text">
                      {question.unitSymbol || question.unit
                        ? `Unit: ${question.unitSymbol || question.unit}`
                        : 'No unit'}
                      {question.minValue != null || question.maxValue != null
                        ? ` · Range: ${question.minValue ?? '—'} to ${question.maxValue ?? '—'}`
                        : ''}
                      {question.decimalPlaces != null
                        ? ` · Decimals: ${question.decimalPlaces}`
                        : ''}
                    </div>
                  )}
                </td>
                <td>{getInspectionTemplateQuestionTypeLabel(question.questionType)}</td>
                <td>{question.required ? 'Yes' : 'No'}</td>
                <td>{question.active ? 'Active' : 'Inactive'}</td>
                {(canMutate || canView) && (
                  <td>
                    {canMutate && question.active ? (
                      <>
                        <button type="button" className="btn-link" onClick={() => onEdit(question)}>
                          Edit
                        </button>{' '}
                        <button
                          type="button"
                          className="btn-link"
                          onClick={() => onDeactivate(question.id)}
                        >
                          Deactivate
                        </button>{' '}
                        <button
                          type="button"
                          className="btn-link"
                          disabled={reordering || activeQuestions[0]?.id === question.id}
                          onClick={() => onMove(question.id, 'up')}
                        >
                          Move Up
                        </button>{' '}
                        <button
                          type="button"
                          className="btn-link"
                          disabled={
                            reordering ||
                            activeQuestions[activeQuestions.length - 1]?.id === question.id
                          }
                          onClick={() => onMove(question.id, 'down')}
                        >
                          Move Down
                        </button>
                        {question.questionType === 'CHOICE' && (
                          <>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              onClick={() => onManageChoices(question)}
                            >
                              Manage Choices
                            </button>
                          </>
                        )}
                        {RULE_SUPPORTED_QUESTION_TYPES.has(question.questionType) && (
                          <>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              onClick={() => onManageRules(question)}
                            >
                              Manage Rules
                            </button>
                          </>
                        )}
                      </>
                    ) : (
                      <>
                        {RULE_SUPPORTED_QUESTION_TYPES.has(question.questionType) ? (
                          <button
                            type="button"
                            className="btn-link"
                            onClick={() => onManageRules(question)}
                          >
                            Manage Rules
                          </button>
                        ) : (
                          '-'
                        )}
                      </>
                    )}
                  </td>
                )}
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </section>
  );
}
