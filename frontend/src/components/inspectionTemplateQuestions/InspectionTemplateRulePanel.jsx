import {
  DECISION_RULE_ACTION_TYPES,
  getActionTypeLabel,
  getOperatorLabel,
} from '../../constants/decisionRules';
import { COMMON_MESSAGES } from '../../constants/messages';
import { TableEmptyRow } from '../PageFeedback';

export default function InspectionTemplateRulePanel({
  questionCode,
  canMutate,
  ruleForm,
  editingRuleId,
  ruleSubmitting,
  rulePayloadError,
  ruleOperatorOptions,
  showComparisonValue,
  ruleChoiceOptions,
  rules,
  onClose,
  onFormChange,
  onSubmit,
  onEditRule,
  onDeactivateRule,
  onCancelEdit,
}) {
  return (
    <section className="reference-form-section">
      <h2>Manage Decision Rules — {questionCode}</h2>
      {!canMutate && (
        <p className="read-only-note">
          Decision rules are read-only. Administrators can create, edit, and deactivate rules on
          draft templates.
        </p>
      )}
      <button type="button" className="btn-secondary" onClick={onClose}>
        Close
      </button>
      {canMutate && (
        <form className="reference-form" onSubmit={onSubmit}>
          <div className="form-row">
            <label htmlFor="ruleCode">Rule Code</label>
            <input
              id="ruleCode"
              name="ruleCode"
              type="text"
              value={ruleForm.ruleCode}
              onChange={onFormChange}
              required={!editingRuleId}
              disabled={ruleSubmitting || Boolean(editingRuleId)}
              readOnly={Boolean(editingRuleId)}
            />
            {editingRuleId && (
              <p className="field-hint">Rule codes are read-only after creation.</p>
            )}
          </div>
          <div className="form-row">
            <label htmlFor="ruleName">Rule Name</label>
            <input
              id="ruleName"
              name="ruleName"
              type="text"
              value={ruleForm.ruleName}
              onChange={onFormChange}
              required
              disabled={ruleSubmitting}
            />
          </div>
          <div className="form-row">
            <label htmlFor="ruleDescription">Description</label>
            <textarea
              id="ruleDescription"
              name="description"
              value={ruleForm.description}
              onChange={onFormChange}
              disabled={ruleSubmitting}
              rows={2}
            />
          </div>
          <div className="form-row">
            <label htmlFor="conditionType">Condition Type</label>
            <input
              id="conditionType"
              type="text"
              value={ruleForm.conditionType}
              readOnly
              disabled
            />
            <p className="field-hint">
              Condition type matches the question type and cannot be changed separately.
            </p>
          </div>
          <div className="form-row">
            <label htmlFor="operator">Operator</label>
            <select
              id="operator"
              name="operator"
              value={ruleForm.operator}
              onChange={onFormChange}
              required
              disabled={ruleSubmitting}
            >
              {ruleOperatorOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          {showComparisonValue && (
            <div className="form-row">
              <label htmlFor="comparisonValue">Comparison Value</label>
              {ruleForm.conditionType === 'CHOICE' ? (
                <select
                  id="comparisonValue"
                  name="comparisonValue"
                  value={ruleForm.comparisonValue}
                  onChange={onFormChange}
                  required
                  disabled={ruleSubmitting}
                >
                  <option value="">Select choice code</option>
                  {ruleChoiceOptions.map((choice) => (
                    <option key={choice.id} value={choice.code}>
                      {choice.code}
                      {' — '}
                      {choice.label}
                    </option>
                  ))}
                </select>
              ) : (
                <input
                  id="comparisonValue"
                  name="comparisonValue"
                  type={ruleForm.conditionType === 'NUMBER' ? 'number' : 'text'}
                  step={ruleForm.conditionType === 'NUMBER' ? 'any' : undefined}
                  value={ruleForm.comparisonValue}
                  onChange={onFormChange}
                  required
                  disabled={ruleSubmitting}
                />
              )}
            </div>
          )}
          <div className="form-row">
            <label htmlFor="actionType">Action Type</label>
            <select
              id="actionType"
              name="actionType"
              value={ruleForm.actionType}
              onChange={onFormChange}
              required
              disabled={ruleSubmitting}
            >
              {DECISION_RULE_ACTION_TYPES.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
          <div className="form-row">
            <label htmlFor="actionPayload">Action Payload (JSON)</label>
            <textarea
              id="actionPayload"
              name="actionPayload"
              value={ruleForm.actionPayload}
              onChange={onFormChange}
              disabled={ruleSubmitting}
              rows={4}
              placeholder='{"severity":"HIGH","message":"Temperature exceeds safe operating range."}'
            />
            {rulePayloadError && <p className="field-error">{rulePayloadError}</p>}
          </div>
          <div className="form-actions">
            <button type="submit" className="btn-primary" disabled={ruleSubmitting}>
              {ruleSubmitting ? 'Saving...' : editingRuleId ? 'Update Rule' : 'Add Rule'}
            </button>
            {editingRuleId && (
              <button
                type="button"
                className="btn-secondary"
                onClick={onCancelEdit}
                disabled={ruleSubmitting}
              >
                Cancel Edit
              </button>
            )}
          </div>
        </form>
      )}
      <div className="table-scroll">
        <table
          className="reference-table"
          aria-label={`Decision rules for question ${questionCode}`}
        >
          <thead>
            <tr>
              <th>Code</th>
              <th>Name</th>
              <th>Condition</th>
              <th>Operator</th>
              <th>Comparison</th>
              <th>Action</th>
              <th>Status</th>
              {canMutate && <th>Actions</th>}
            </tr>
          </thead>
          <tbody>
            {rules.length === 0 ? (
              <TableEmptyRow
                colSpan={canMutate ? 8 : 7}
                message={COMMON_MESSAGES.NO_TEMPLATE_RULES}
              />
            ) : (
              rules.map((rule) => (
                <tr key={rule.id}>
                  <td>{rule.ruleCode}</td>
                  <td>
                    {rule.ruleName}
                    {rule.description && <div className="help-text">{rule.description}</div>}
                  </td>
                  <td>{rule.conditionType}</td>
                  <td>{getOperatorLabel(rule.conditionType, rule.operator)}</td>
                  <td>{rule.comparisonValue ?? '—'}</td>
                  <td>{getActionTypeLabel(rule.actionType)}</td>
                  <td>{rule.active ? 'Active' : 'Inactive'}</td>
                  {canMutate && (
                    <td>
                      {rule.active ? (
                        <>
                          <button
                            type="button"
                            className="btn-link"
                            onClick={() => onEditRule(rule)}
                          >
                            Edit
                          </button>{' '}
                          <button
                            type="button"
                            className="btn-link"
                            onClick={() => onDeactivateRule(rule.id)}
                          >
                            Deactivate
                          </button>
                        </>
                      ) : (
                        '-'
                      )}
                    </td>
                  )}
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}
