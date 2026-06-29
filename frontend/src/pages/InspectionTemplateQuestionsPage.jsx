import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import inspectionTemplateQuestionApi from '../services/inspectionTemplateQuestionApi';
import inspectionTemplateQuestionChoiceApi from '../services/inspectionTemplateQuestionChoiceApi';
import unitOfMeasureApi from '../services/unitOfMeasureApi';
import NotificationButton from '../components/NotificationButton';
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
import '../styles/ReferenceDataPage.css';

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

function isDraftTemplate(template) {
  return template?.status === 'DRAFT';
}

export default function InspectionTemplateQuestionsPage() {
  const { templateId } = useParams();
  const navigate = useNavigate();
  const { auth, logout } = useAuth();
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

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <div className="loading">Loading checklist questions...</div>;
  }

  return (
    <div className="reference-data-page">
      <header
        className="reference-header"
        style={{
          background: 'linear-gradient(135deg, #1a472a 0%, #2d6b4d 100%)',
          color: 'white',
        }}
      >
        <button
          type="button"
          className="back-btn"
          onClick={() => navigate('/inspection-templates')}
        >
          ← Back to Templates
        </button>
        <h1>Checklist Questions</h1>
        <div className="user-header-actions">
          <NotificationButton />
          <button type="button" className="logout-btn" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </header>

      <main className="reference-content">
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
                  {canMutate && <th>Actions</th>}
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
                    {canMutate && (
                      <td>
                        {question.active ? (
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
                          </>
                        ) : (
                          '-'
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
      </main>
    </div>
  );
}
