import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import inspectionTemplateQuestionApi from '../services/inspectionTemplateQuestionApi';
import inspectionTemplateQuestionChoiceApi from '../services/inspectionTemplateQuestionChoiceApi';
import inspectionTemplateQuestionRuleApi from '../services/inspectionTemplateQuestionRuleApi';
import unitOfMeasureApi from '../services/unitOfMeasureApi';
import ReferenceDataLayout from '../components/layout/ReferenceDataLayout';
import {
  canManageInspectionTemplates,
  canViewInspectionTemplates,
} from '../constants/userRoles';
import {
  INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS,
  getInspectionTemplateQuestionTypeLabel,
} from '../constants/inspectionTemplateQuestionTypes';
import { getInspectionTemplateStatusLabel } from '../constants/inspectionTemplateStatuses';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import { isValidQuestionCode, suggestQuestionCode } from '../utils/suggestQuestionCode';
import {
  DECISION_RULE_ACTION_TYPES,
  RULE_SUPPORTED_QUESTION_TYPES,
  comparisonValueRequired,
  conditionTypeForQuestionType,
  getActionTypeLabel,
  getOperatorLabel,
  getOperatorsForConditionType,
  validateActionPayloadJson,
} from '../constants/decisionRules';
import { getActiveChoices } from '../utils/inspectionAnswers';

const EMPTY_FORM = {
  code: '',
  questionText: '',
  helpText: '',
  questionType: 'BOOLEAN',
  required: false,
  unitOfMeasureId: '',
  minValue: '',
  maxValue: '',
  decimalPlaces: '',
};

const EMPTY_CHOICE_FORM = {
  code: '',
  label: '',
};

const EMPTY_RULE_FORM = {
  ruleCode: '',
  ruleName: '',
  description: '',
  conditionType: 'NUMBER',
  operator: 'GREATER_THAN',
  comparisonValue: '',
  actionType: 'SUGGEST_ISSUE',
  actionPayload: '',
};

function isDraftTemplate(template) {
  return template?.status === 'DRAFT';
}

export default function InspectionTemplateQuestionsPage() {
  const { templateId } = useParams();
  const navigate = useNavigate();
  const { auth } = useAuth();
  const [template, setTemplate] = useState(null);
  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [reordering, setReordering] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);
  const [formData, setFormData] = useState(EMPTY_FORM);
  const [editingId, setEditingId] = useState(null);
  const [codeManuallyEdited, setCodeManuallyEdited] = useState(false);
  const [managingChoicesQuestionId, setManagingChoicesQuestionId] = useState(null);
  const [choiceForm, setChoiceForm] = useState(EMPTY_CHOICE_FORM);
  const [editingChoiceId, setEditingChoiceId] = useState(null);
  const [choiceSubmitting, setChoiceSubmitting] = useState(false);
  const [unitsOfMeasure, setUnitsOfMeasure] = useState([]);
  const [managingRulesQuestionId, setManagingRulesQuestionId] = useState(null);
  const [rules, setRules] = useState([]);
  const [ruleForm, setRuleForm] = useState(EMPTY_RULE_FORM);
  const [editingRuleId, setEditingRuleId] = useState(null);
  const [ruleSubmitting, setRuleSubmitting] = useState(false);
  const [rulePayloadError, setRulePayloadError] = useState(null);

  const canView = canViewInspectionTemplates(auth?.user?.role);
  const canManage = canManageInspectionTemplates(auth?.user?.role);
  const canMutate = canManage && isDraftTemplate(template);

  useEffect(() => {
    if (!auth) {
      navigate('/login');
      return;
    }
    if (!canView) {
      navigate('/');
      return;
    }
    apiClient.setToken(auth.token);
    loadPageData();
  }, [auth, canView, navigate, templateId]);

  const loadPageData = async () => {
    try {
      setLoading(true);
      setError(null);
      const [templateData, questionList, unitList] = await Promise.all([
        inspectionTemplateApi.get(templateId),
        inspectionTemplateQuestionApi.list(templateId),
        unitOfMeasureApi.list({ active: true }),
      ]);
      setTemplate(templateData);
      setQuestions(questionList);
      setUnitsOfMeasure(unitList);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load checklist questions.'));
    } finally {
      setLoading(false);
    }
  };

  const handleFormChange = (e) => {
    const { name, value, type, checked } = e.target;
    const nextValue = type === 'checkbox' ? checked : value;

    setFormData((prev) => {
      const next = { ...prev, [name]: nextValue };
      if (name === 'questionText' && !editingId && !codeManuallyEdited) {
        next.code = suggestQuestionCode(nextValue);
      }
      return next;
    });

    if (name === 'code') {
      setCodeManuallyEdited(true);
    }
  };

  const resetForm = () => {
    setFormData(EMPTY_FORM);
    setEditingId(null);
    setCodeManuallyEdited(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!canMutate) return;

    const normalizedCode = formData.code.trim().toUpperCase();
    if (!editingId && !isValidQuestionCode(normalizedCode)) {
      setError(
        'Question code must be uppercase, start with a letter, and contain only letters, digits, and underscores.'
      );
      return;
    }

    try {
      setSubmitting(true);
      setError(null);
      setSuccess(null);
      const payload = {
        questionText: formData.questionText.trim(),
        helpText: formData.helpText.trim() || undefined,
        questionType: formData.questionType,
        required: formData.required,
      };
      if (formData.questionType === 'NUMBER') {
        payload.unitOfMeasureId = formData.unitOfMeasureId
          ? Number(formData.unitOfMeasureId)
          : undefined;
        payload.minValue = formData.minValue === '' ? undefined : Number(formData.minValue);
        payload.maxValue = formData.maxValue === '' ? undefined : Number(formData.maxValue);
        payload.decimalPlaces = formData.decimalPlaces === '' ? undefined : Number(formData.decimalPlaces);
      }
      if (editingId) {
        await inspectionTemplateQuestionApi.update(templateId, editingId, payload);
        setSuccess('Question updated successfully.');
      } else {
        await inspectionTemplateQuestionApi.create(templateId, {
          ...payload,
          code: normalizedCode,
        });
        setSuccess('Question created successfully.');
      }
      resetForm();
      await loadPageData();
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to manage checklist questions.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to save question.'));
      }
    } finally {
      setSubmitting(false);
    }
  };

  const handleEdit = (question) => {
    if (!canMutate || !question.active) return;
    setEditingId(question.id);
    setCodeManuallyEdited(true);
    setFormData({
      code: question.code,
      questionText: question.questionText,
      helpText: question.helpText || '',
      questionType: question.questionType,
      required: question.required,
      unitOfMeasureId: question.unitOfMeasureId ? String(question.unitOfMeasureId) : '',
      minValue: question.minValue ?? '',
      maxValue: question.maxValue ?? '',
      decimalPlaces: question.decimalPlaces ?? '',
    });
  };

  const handleManageChoices = (question) => {
    if (!canMutate || question.questionType !== 'CHOICE') return;
    setManagingChoicesQuestionId(question.id);
    setChoiceForm(EMPTY_CHOICE_FORM);
    setEditingChoiceId(null);
  };

  const handleChoiceFormChange = (event) => {
    const { name, value } = event.target;
    setChoiceForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleChoiceSubmit = async (event) => {
    event.preventDefault();
    if (!canMutate || managingChoicesQuestionId == null) return;

    const normalizedCode = choiceForm.code.trim().toUpperCase();
    if (!editingChoiceId && !isValidQuestionCode(normalizedCode)) {
      setError('Choice code must be uppercase, start with a letter, and contain only letters, digits, and underscores.');
      return;
    }

    try {
      setChoiceSubmitting(true);
      setError(null);
      setSuccess(null);
      if (editingChoiceId) {
        await inspectionTemplateQuestionChoiceApi.update(
          templateId,
          managingChoicesQuestionId,
          editingChoiceId,
          { label: choiceForm.label.trim() }
        );
        setSuccess('Choice updated successfully.');
      } else {
        await inspectionTemplateQuestionChoiceApi.create(templateId, managingChoicesQuestionId, {
          code: normalizedCode,
          label: choiceForm.label.trim(),
        });
        setSuccess('Choice created successfully.');
      }
      setChoiceForm(EMPTY_CHOICE_FORM);
      setEditingChoiceId(null);
      await loadPageData();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to save choice.'));
    } finally {
      setChoiceSubmitting(false);
    }
  };

  const handleEditChoice = (choice) => {
    setEditingChoiceId(choice.id);
    setChoiceForm({ code: choice.code, label: choice.label });
  };

  const handleDeactivateChoice = async (choiceId) => {
    if (!window.confirm('Deactivate this choice?')) return;
    try {
      setError(null);
      setSuccess(null);
      await inspectionTemplateQuestionChoiceApi.deactivate(
        templateId,
        managingChoicesQuestionId,
        choiceId
      );
      setSuccess('Choice deactivated successfully.');
      if (editingChoiceId === choiceId) {
        setEditingChoiceId(null);
        setChoiceForm(EMPTY_CHOICE_FORM);
      }
      await loadPageData();
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to deactivate choice.'));
    }
  };

  const handleMoveChoice = async (choiceId, direction) => {
    const question = questions.find((item) => item.id === managingChoicesQuestionId);
    if (!question) return;
    const activeChoices = (question.choices || []).filter((choice) => choice.active);
    const index = activeChoices.findIndex((choice) => choice.id === choiceId);
    if (index < 0) return;
    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= activeChoices.length) return;

    const reordered = [...activeChoices];
    const [moved] = reordered.splice(index, 1);
    reordered.splice(targetIndex, 0, moved);

    try {
      setChoiceSubmitting(true);
      setError(null);
      await inspectionTemplateQuestionChoiceApi.reorder(
        templateId,
        managingChoicesQuestionId,
        reordered.map((choice) => choice.id)
      );
      await loadPageData();
      setSuccess('Choices reordered successfully.');
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to reorder choices.'));
    } finally {
      setChoiceSubmitting(false);
    }
  };

  const handleDeactivate = async (questionId) => {
    if (!window.confirm('Deactivate this question?')) return;

    try {
      setError(null);
      setSuccess(null);
      await inspectionTemplateQuestionApi.deactivate(templateId, questionId);
      setSuccess('Question deactivated successfully.');
      if (editingId === questionId) {
        resetForm();
      }
      await loadPageData();
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to deactivate questions.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to deactivate question.'));
      }
    }
  };

  const activeQuestions = questions.filter((question) => question.active);

  const handleMove = async (questionId, direction) => {
    const index = activeQuestions.findIndex((question) => question.id === questionId);
    if (index < 0) return;

    const targetIndex = direction === 'up' ? index - 1 : index + 1;
    if (targetIndex < 0 || targetIndex >= activeQuestions.length) return;

    const reordered = [...activeQuestions];
    const [moved] = reordered.splice(index, 1);
    reordered.splice(targetIndex, 0, moved);

    try {
      setReordering(true);
      setError(null);
      setSuccess(null);
      const updated = await inspectionTemplateQuestionApi.reorder(
        templateId,
        reordered.map((question) => question.id)
      );
      setQuestions(updated);
      setSuccess('Questions reordered successfully.');
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to reorder questions.'));
    } finally {
      setReordering(false);
    }
  };

  const defaultRuleFormForQuestion = (question) => {
    const conditionType = conditionTypeForQuestionType(question.questionType);
    const operators = getOperatorsForConditionType(conditionType);
    return {
      ...EMPTY_RULE_FORM,
      conditionType,
      operator: operators[0]?.value || 'EQUALS',
      comparisonValue: '',
    };
  };

  const loadRulesForQuestion = async (questionId) => {
    const ruleList = await inspectionTemplateQuestionRuleApi.list(templateId, questionId);
    setRules(Array.isArray(ruleList) ? ruleList : []);
  };

  const handleManageRules = async (question) => {
    if (!canView || !RULE_SUPPORTED_QUESTION_TYPES.has(question.questionType)) return;
    setManagingRulesQuestionId(question.id);
    setManagingChoicesQuestionId(null);
    setRuleForm(defaultRuleFormForQuestion(question));
    setEditingRuleId(null);
    setRulePayloadError(null);
    try {
      setError(null);
      await loadRulesForQuestion(question.id);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to load decision rules.'));
      setRules([]);
    }
  };

  const handleCloseRules = () => {
    setManagingRulesQuestionId(null);
    setRules([]);
    setRuleForm(EMPTY_RULE_FORM);
    setEditingRuleId(null);
    setRulePayloadError(null);
  };

  const handleRuleFormChange = (event) => {
    const { name, value } = event.target;
    setRuleForm((prev) => {
      const next = { ...prev, [name]: value };
      if (name === 'conditionType') {
        const operators = getOperatorsForConditionType(value);
        if (!operators.some((option) => option.value === next.operator)) {
          next.operator = operators[0]?.value || '';
        }
        if (!comparisonValueRequired(value)) {
          next.comparisonValue = '';
        }
      }
      if (name === 'actionPayload') {
        setRulePayloadError(validateActionPayloadJson(value));
      }
      return next;
    });
  };

  const handleRuleSubmit = async (event) => {
    event.preventDefault();
    if (!canMutate || managingRulesQuestionId == null) return;

    const normalizedCode = ruleForm.ruleCode.trim().toUpperCase();
    if (!editingRuleId && !isValidQuestionCode(normalizedCode)) {
      setError(
        'Rule code must be uppercase, start with a letter, and contain only letters, digits, and underscores.'
      );
      return;
    }

    const payloadError = validateActionPayloadJson(ruleForm.actionPayload);
    if (payloadError) {
      setRulePayloadError(payloadError);
      setError(payloadError);
      return;
    }

    const payload = {
      ruleName: ruleForm.ruleName.trim(),
      description: ruleForm.description.trim() || undefined,
      conditionType: ruleForm.conditionType,
      operator: ruleForm.operator,
      actionType: ruleForm.actionType,
      actionPayload: ruleForm.actionPayload.trim() || undefined,
    };
    if (comparisonValueRequired(ruleForm.conditionType)) {
      payload.comparisonValue = ruleForm.comparisonValue.trim();
    }

    try {
      setRuleSubmitting(true);
      setError(null);
      setSuccess(null);
      if (editingRuleId) {
        await inspectionTemplateQuestionRuleApi.update(
          templateId,
          managingRulesQuestionId,
          editingRuleId,
          payload
        );
        setSuccess('Decision rule updated successfully.');
      } else {
        await inspectionTemplateQuestionRuleApi.create(templateId, managingRulesQuestionId, {
          ...payload,
          ruleCode: normalizedCode,
        });
        setSuccess('Decision rule created successfully.');
      }
      const managingQuestion = questions.find((item) => item.id === managingRulesQuestionId);
      setRuleForm(managingQuestion ? defaultRuleFormForQuestion(managingQuestion) : EMPTY_RULE_FORM);
      setEditingRuleId(null);
      setRulePayloadError(null);
      await loadRulesForQuestion(managingRulesQuestionId);
    } catch (err) {
      if (isForbidden(err)) {
        setError('You do not have permission to manage decision rules.');
      } else {
        setError(getApiErrorMessage(err, 'Failed to save decision rule.'));
      }
    } finally {
      setRuleSubmitting(false);
    }
  };

  const handleEditRule = (rule) => {
    if (!canMutate) return;
    setEditingRuleId(rule.id);
    setRuleForm({
      ruleCode: rule.ruleCode,
      ruleName: rule.ruleName,
      description: rule.description || '',
      conditionType: rule.conditionType,
      operator: rule.operator,
      comparisonValue: rule.comparisonValue || '',
      actionType: rule.actionType,
      actionPayload: rule.actionPayload || '',
    });
    setRulePayloadError(validateActionPayloadJson(rule.actionPayload || ''));
  };

  const handleDeactivateRule = async (ruleId) => {
    if (!canMutate || managingRulesQuestionId == null) return;
    if (!window.confirm('Deactivate this decision rule?')) return;

    try {
      setError(null);
      setSuccess(null);
      await inspectionTemplateQuestionRuleApi.deactivate(
        templateId,
        managingRulesQuestionId,
        ruleId
      );
      setSuccess('Decision rule deactivated successfully.');
      if (editingRuleId === ruleId) {
        setEditingRuleId(null);
        const managingQuestion = questions.find((item) => item.id === managingRulesQuestionId);
        setRuleForm(managingQuestion ? defaultRuleFormForQuestion(managingQuestion) : EMPTY_RULE_FORM);
        setRulePayloadError(null);
      }
      await loadRulesForQuestion(managingRulesQuestionId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to deactivate decision rule.'));
    }
  };

  const managingRulesQuestion = questions.find((item) => item.id === managingRulesQuestionId);
  const ruleOperatorOptions = getOperatorsForConditionType(ruleForm.conditionType);
  const showComparisonValue = comparisonValueRequired(ruleForm.conditionType);
  const ruleChoiceOptions = managingRulesQuestion
    ? getActiveChoices(managingRulesQuestion)
    : [];

  if (loading) {
    return <div className="loading">Loading checklist questions...</div>;
  }

  return (
    <ReferenceDataLayout
      title="Checklist Questions"
      backLabel="← Back to Templates"
      onBack={() => navigate('/inspection-templates')}
    >
        {error && <div className="error-message">{error}</div>}
        {success && <div className="success-message">{success}</div>}

        {template && (
          <section className="reference-form-section">
            <h2>{template.name}</h2>
            <p>
              Asset Category: {template.assetCategoryName}
              {' · '}
              Version: {template.version}
              {' · '}
              Status: {getInspectionTemplateStatusLabel(template.status)}
            </p>
            {!canManage && (
              <p className="read-only-note">
                Checklist questions are read-only. Administrators can create, edit, deactivate, and reorder questions on draft templates.
              </p>
            )}
            {canManage && !isDraftTemplate(template) && (
              <p className="read-only-note">
                This template is {getInspectionTemplateStatusLabel(template.status).toLowerCase()}.
                Checklist questions cannot be modified. Create a new template version in a future release to change published templates.
              </p>
            )}
          </section>
        )}

        {canMutate && (
          <section className="reference-form-section">
            <h2>{editingId ? 'Edit Question' : 'Create Question'}</h2>
            <form className="reference-form" onSubmit={handleSubmit}>
              <div className="form-row">
                <label htmlFor="code">Question Code</label>
                <input
                  id="code"
                  name="code"
                  type="text"
                  value={formData.code}
                  onChange={handleFormChange}
                  required={!editingId}
                  disabled={submitting || Boolean(editingId)}
                  readOnly={Boolean(editingId)}
                />
                {!editingId && (
                  <p className="field-hint">
                    Suggested automatically from question text. You can edit it before saving.
                  </p>
                )}
                {editingId && (
                  <p className="field-hint">
                    Business codes are read-only after creation to protect downstream integrations.
                  </p>
                )}
              </div>
              <div className="form-row">
                <label htmlFor="questionText">Question Text</label>
                <input
                  id="questionText"
                  name="questionText"
                  type="text"
                  value={formData.questionText}
                  onChange={handleFormChange}
                  required
                  disabled={submitting}
                />
              </div>
              <div className="form-row">
                <label htmlFor="helpText">Help Text</label>
                <textarea
                  id="helpText"
                  name="helpText"
                  value={formData.helpText}
                  onChange={handleFormChange}
                  disabled={submitting}
                  rows={2}
                />
              </div>
              <div className="form-row">
                <label htmlFor="questionType">Question Type</label>
                <select
                  id="questionType"
                  name="questionType"
                  value={formData.questionType}
                  onChange={handleFormChange}
                  required
                  disabled={submitting}
                >
                  {INSPECTION_TEMPLATE_QUESTION_TYPE_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
              </div>
              <div className="form-row">
                <label htmlFor="required">
                  <input
                    id="required"
                    name="required"
                    type="checkbox"
                    checked={formData.required}
                    onChange={handleFormChange}
                    disabled={submitting}
                  />
                  {' '}
                  Required
                </label>
              </div>
              {formData.questionType === 'NUMBER' && (
                <>
                  <div className="form-row">
                    <label htmlFor="unitOfMeasureId">Unit of Measure</label>
                    <select
                      id="unitOfMeasureId"
                      name="unitOfMeasureId"
                      value={formData.unitOfMeasureId}
                      onChange={handleFormChange}
                      disabled={submitting}
                    >
                      <option value="">No unit</option>
                      {unitsOfMeasure.map((unit) => (
                        <option key={unit.id} value={unit.id}>
                          {unit.symbol} — {unit.name} ({unit.quantityType})
                        </option>
                      ))}
                    </select>
                  </div>
                  <div className="form-row">
                    <label htmlFor="minValue">Minimum Value</label>
                    <input
                      id="minValue"
                      name="minValue"
                      type="number"
                      step="any"
                      value={formData.minValue}
                      onChange={handleFormChange}
                      disabled={submitting}
                    />
                  </div>
                  <div className="form-row">
                    <label htmlFor="maxValue">Maximum Value</label>
                    <input
                      id="maxValue"
                      name="maxValue"
                      type="number"
                      step="any"
                      value={formData.maxValue}
                      onChange={handleFormChange}
                      disabled={submitting}
                    />
                  </div>
                  <div className="form-row">
                    <label htmlFor="decimalPlaces">Decimal Places</label>
                    <input
                      id="decimalPlaces"
                      name="decimalPlaces"
                      type="number"
                      min="0"
                      max="6"
                      value={formData.decimalPlaces}
                      onChange={handleFormChange}
                      disabled={submitting}
                    />
                  </div>
                </>
              )}
              <div className="form-actions">
                <button type="submit" className="btn-primary" disabled={submitting}>
                  {submitting ? 'Saving...' : editingId ? 'Update Question' : 'Create Question'}
                </button>
                {editingId && (
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={resetForm}
                    disabled={submitting}
                  >
                    Cancel Edit
                  </button>
                )}
              </div>
            </form>
          </section>
        )}

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
                      {question.helpText && (
                        <div className="help-text">{question.helpText}</div>
                      )}
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
                            <button
                              type="button"
                              className="btn-link"
                              onClick={() => handleEdit(question)}
                            >
                              Edit
                            </button>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              onClick={() => handleDeactivate(question.id)}
                            >
                              Deactivate
                            </button>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              disabled={reordering || activeQuestions[0]?.id === question.id}
                              onClick={() => handleMove(question.id, 'up')}
                            >
                              Move Up
                            </button>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              disabled={
                                reordering
                                || activeQuestions[activeQuestions.length - 1]?.id === question.id
                              }
                              onClick={() => handleMove(question.id, 'down')}
                            >
                              Move Down
                            </button>
                            {question.questionType === 'CHOICE' && (
                              <>
                                {' '}
                                <button
                                  type="button"
                                  className="btn-link"
                                  onClick={() => handleManageChoices(question)}
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
                                  onClick={() => handleManageRules(question)}
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
                                onClick={() => handleManageRules(question)}
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

        {canMutate && managingChoicesQuestionId != null && (
          <section className="reference-form-section">
            <h2>
              Manage Choices —
              {' '}
              {questions.find((question) => question.id === managingChoicesQuestionId)?.code}
            </h2>
            <button
              type="button"
              className="btn-secondary"
              onClick={() => {
                setManagingChoicesQuestionId(null);
                setChoiceForm(EMPTY_CHOICE_FORM);
                setEditingChoiceId(null);
              }}
            >
              Close
            </button>
            <form className="reference-form" onSubmit={handleChoiceSubmit}>
              <div className="form-row">
                <label htmlFor="choiceCode">Choice Code</label>
                <input
                  id="choiceCode"
                  name="code"
                  type="text"
                  value={choiceForm.code}
                  onChange={handleChoiceFormChange}
                  required={!editingChoiceId}
                  disabled={choiceSubmitting || Boolean(editingChoiceId)}
                  readOnly={Boolean(editingChoiceId)}
                />
              </div>
              <div className="form-row">
                <label htmlFor="choiceLabel">Label</label>
                <input
                  id="choiceLabel"
                  name="label"
                  type="text"
                  value={choiceForm.label}
                  onChange={handleChoiceFormChange}
                  required
                  disabled={choiceSubmitting}
                />
              </div>
              <div className="form-actions">
                <button type="submit" className="btn-primary" disabled={choiceSubmitting}>
                  {choiceSubmitting ? 'Saving...' : editingChoiceId ? 'Update Choice' : 'Add Choice'}
                </button>
                {editingChoiceId && (
                  <button
                    type="button"
                    className="btn-secondary"
                    onClick={() => {
                      setEditingChoiceId(null);
                      setChoiceForm(EMPTY_CHOICE_FORM);
                    }}
                    disabled={choiceSubmitting}
                  >
                    Cancel Edit
                  </button>
                )}
              </div>
            </form>
            <table className="reference-table">
              <thead>
                <tr>
                  <th>Order</th>
                  <th>Code</th>
                  <th>Label</th>
                  <th>Status</th>
                  <th>Actions</th>
                </tr>
              </thead>
              <tbody>
                {(questions.find((question) => question.id === managingChoicesQuestionId)?.choices || [])
                  .map((choice) => (
                    <tr key={choice.id}>
                      <td>{choice.displayOrder}</td>
                      <td>{choice.code}</td>
                      <td>{choice.label}</td>
                      <td>{choice.active ? 'Active' : 'Inactive'}</td>
                      <td>
                        {choice.active ? (
                          <>
                            <button
                              type="button"
                              className="btn-link"
                              onClick={() => handleEditChoice(choice)}
                            >
                              Edit
                            </button>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              onClick={() => handleDeactivateChoice(choice.id)}
                            >
                              Deactivate
                            </button>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              disabled={choiceSubmitting}
                              onClick={() => handleMoveChoice(choice.id, 'up')}
                            >
                              Move Up
                            </button>
                            {' '}
                            <button
                              type="button"
                              className="btn-link"
                              disabled={choiceSubmitting}
                              onClick={() => handleMoveChoice(choice.id, 'down')}
                            >
                              Move Down
                            </button>
                          </>
                        ) : (
                          '-'
                        )}
                      </td>
                    </tr>
                  ))}
              </tbody>
            </table>
          </section>
        )}

        {managingRulesQuestionId != null && (
          <section className="reference-form-section">
            <h2>
              Manage Decision Rules —
              {' '}
              {managingRulesQuestion?.code}
            </h2>
            {!canMutate && (
              <p className="read-only-note">
                Decision rules are read-only. Administrators can create, edit, and deactivate rules on draft templates.
              </p>
            )}
            <button
              type="button"
              className="btn-secondary"
              onClick={handleCloseRules}
            >
              Close
            </button>
            {canMutate && (
              <form className="reference-form" onSubmit={handleRuleSubmit}>
                <div className="form-row">
                  <label htmlFor="ruleCode">Rule Code</label>
                  <input
                    id="ruleCode"
                    name="ruleCode"
                    type="text"
                    value={ruleForm.ruleCode}
                    onChange={handleRuleFormChange}
                    required={!editingRuleId}
                    disabled={ruleSubmitting || Boolean(editingRuleId)}
                    readOnly={Boolean(editingRuleId)}
                  />
                  {editingRuleId && (
                    <p className="field-hint">
                      Rule codes are read-only after creation.
                    </p>
                  )}
                </div>
                <div className="form-row">
                  <label htmlFor="ruleName">Rule Name</label>
                  <input
                    id="ruleName"
                    name="ruleName"
                    type="text"
                    value={ruleForm.ruleName}
                    onChange={handleRuleFormChange}
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
                    onChange={handleRuleFormChange}
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
                    onChange={handleRuleFormChange}
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
                        onChange={handleRuleFormChange}
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
                        onChange={handleRuleFormChange}
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
                    onChange={handleRuleFormChange}
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
                    onChange={handleRuleFormChange}
                    disabled={ruleSubmitting}
                    rows={4}
                    placeholder='{"severity":"HIGH","message":"Temperature exceeds safe operating range."}'
                  />
                  {rulePayloadError && (
                    <p className="field-error">{rulePayloadError}</p>
                  )}
                </div>
                <div className="form-actions">
                  <button type="submit" className="btn-primary" disabled={ruleSubmitting}>
                    {ruleSubmitting ? 'Saving...' : editingRuleId ? 'Update Rule' : 'Add Rule'}
                  </button>
                  {editingRuleId && (
                    <button
                      type="button"
                      className="btn-secondary"
                      onClick={() => {
                        setEditingRuleId(null);
                        setRuleForm(
                          managingRulesQuestion
                            ? defaultRuleFormForQuestion(managingRulesQuestion)
                            : EMPTY_RULE_FORM
                        );
                        setRulePayloadError(null);
                      }}
                      disabled={ruleSubmitting}
                    >
                      Cancel Edit
                    </button>
                  )}
                </div>
              </form>
            )}
            <table className="reference-table">
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
                  <tr>
                    <td colSpan={canMutate ? 8 : 7}>No decision rules defined yet.</td>
                  </tr>
                ) : (
                  rules.map((rule) => (
                    <tr key={rule.id}>
                      <td>{rule.ruleCode}</td>
                      <td>
                        {rule.ruleName}
                        {rule.description && (
                          <div className="help-text">{rule.description}</div>
                        )}
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
                                onClick={() => handleEditRule(rule)}
                              >
                                Edit
                              </button>
                              {' '}
                              <button
                                type="button"
                                className="btn-link"
                                onClick={() => handleDeactivateRule(rule.id)}
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
          </section>
        )}
    </ReferenceDataLayout>
  );
}
