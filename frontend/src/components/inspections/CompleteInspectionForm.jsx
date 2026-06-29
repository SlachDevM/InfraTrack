import { getBusinessTriggerTypeLabel } from '../../constants/businessTriggerTypes';
import { PHYSICAL_CONDITION_OPTIONS } from '../../constants/physicalConditions';
import {
  getActiveChoices,
  getNumberConstraintHint,
  getNumberInputStep,
  getQuestionTypeLabel,
  getQuestionUnitSymbol,
  getUnsupportedQuestionTypeMessage,
  isSupportedInspectionAnswerType,
} from '../../utils/inspectionAnswers';

export default function CompleteInspectionForm({
  inspection,
  completeFormData,
  templateQuestions,
  answerValues,
  completingId,
  onChange,
  onAnswerChange,
  onSubmit,
}) {
  const hasTemplate = Boolean(inspection.inspectionTemplateId);

  return (
    <form
      className="inspection-form complete-form"
      onSubmit={onSubmit}
    >
      <div className="linked-asset-info">
        <strong>Asset:</strong> {inspection.assetName}
        <br />
        <strong>Trigger:</strong> #{inspection.businessTriggerId} — {getBusinessTriggerTypeLabel(inspection.businessTriggerType)}
        <br />
        <strong>Reason:</strong> {inspection.businessTriggerReason}
        {hasTemplate && (
          <>
            <br />
            <strong>Template:</strong> {inspection.inspectionTemplateName}
          </>
        )}
      </div>

      {hasTemplate && (
        <section className="inspection-checklist-section">
          <h3>Checklist Questions</h3>
          {templateQuestions.length === 0 ? (
            <p className="read-only-note">No active checklist questions are defined for this template.</p>
          ) : (
            templateQuestions.map((question) => (
              <div key={question.id} className="form-row checklist-question-row">
                <label htmlFor={`question-${inspection.id}-${question.id}`}>
                  {question.questionText}
                  {question.required ? ' *' : ''}
                  <span className="question-code"> ({question.code})</span>
                </label>
                {question.helpText && <p className="field-hint">{question.helpText}</p>}
                {!isSupportedInspectionAnswerType(question.questionType) ? (
                  <p className="read-only-note">{getUnsupportedQuestionTypeMessage()}</p>
                ) : question.questionType === 'BOOLEAN' ? (
                  <select
                    id={`question-${inspection.id}-${question.id}`}
                    value={answerValues[question.id] ?? ''}
                    onChange={(event) => onAnswerChange(question.id, event.target.value)}
                    required={question.required}
                    disabled={completingId === inspection.id}
                  >
                    <option value="">Select answer</option>
                    <option value="true">Yes</option>
                    <option value="false">No</option>
                  </select>
                ) : question.questionType === 'TEXT' ? (
                  <textarea
                    id={`question-${inspection.id}-${question.id}`}
                    value={answerValues[question.id] ?? ''}
                    onChange={(event) => onAnswerChange(question.id, event.target.value)}
                    required={question.required}
                    disabled={completingId === inspection.id}
                    rows={2}
                  />
                ) : question.questionType === 'CHOICE' ? (
                  <select
                    id={`question-${inspection.id}-${question.id}`}
                    value={answerValues[question.id] ?? ''}
                    onChange={(event) => onAnswerChange(question.id, event.target.value)}
                    required={question.required}
                    disabled={completingId === inspection.id}
                  >
                    <option value="">Select option</option>
                    {getActiveChoices(question).map((choice) => (
                      <option key={choice.id} value={choice.code}>
                        {choice.label}
                      </option>
                    ))}
                  </select>
                ) : (
                  <div className="number-answer-input">
                    <input
                      id={`question-${inspection.id}-${question.id}`}
                      type="number"
                      step={getNumberInputStep(question.decimalPlaces)}
                      min={question.minValue ?? undefined}
                      max={question.maxValue ?? undefined}
                      value={answerValues[question.id] ?? ''}
                      onChange={(event) => onAnswerChange(question.id, event.target.value)}
                      required={question.required}
                      disabled={completingId === inspection.id}
                    />
                    {getQuestionUnitSymbol(question) && (
                      <span className="number-unit-label">{getQuestionUnitSymbol(question)}</span>
                    )}
                  </div>
                )}
                {question.questionType === 'NUMBER' && getNumberConstraintHint(question) && (
                  <p className="field-hint">{getNumberConstraintHint(question)}</p>
                )}
                <p className="field-hint">{getQuestionTypeLabel(question.questionType)}</p>
              </div>
            ))
          )}
        </section>
      )}

      <div className="form-row">
        <label htmlFor={`observedCondition-${inspection.id}`}>Observed Condition</label>
        <select
          id={`observedCondition-${inspection.id}`}
          name="observedCondition"
          value={completeFormData.observedCondition}
          onChange={onChange}
          required
          disabled={completingId === inspection.id}
        >
          {PHYSICAL_CONDITION_OPTIONS.map((option) => (
            <option key={option.value} value={option.value}>
              {option.label}
            </option>
          ))}
        </select>
      </div>

      <div className="form-row">
        <label htmlFor={`observations-${inspection.id}`}>Observations</label>
        <textarea
          id={`observations-${inspection.id}`}
          name="observations"
          value={completeFormData.observations}
          onChange={onChange}
          required
          disabled={completingId === inspection.id}
          rows={3}
        />
      </div>

      <div className="form-row checkbox-row">
        <label htmlFor={`issueIdentified-${inspection.id}`}>
          <input
            id={`issueIdentified-${inspection.id}`}
            name="issueIdentified"
            type="checkbox"
            checked={completeFormData.issueIdentified}
            onChange={onChange}
            disabled={completingId === inspection.id}
          />
          Issue identified (record only — Issue creation is handled separately)
        </label>
      </div>

      <div className="form-row">
        <label htmlFor={`completedAt-${inspection.id}`}>Completion Date & Time</label>
        <input
          id={`completedAt-${inspection.id}`}
          name="completedAt"
          type="datetime-local"
          value={completeFormData.completedAt}
          onChange={onChange}
          required
          disabled={completingId === inspection.id}
        />
      </div>

      <button
        type="submit"
        className="btn-primary"
        disabled={completingId === inspection.id}
      >
        {completingId === inspection.id ? 'Completing...' : 'Complete Inspection'}
      </button>
    </form>
  );
}
