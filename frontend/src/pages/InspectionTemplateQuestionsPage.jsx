import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import inspectionTemplateQuestionApi from '../services/inspectionTemplateQuestionApi';
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
      const [templateData, questionList] = await Promise.all([
        inspectionTemplateApi.get(templateId),
        inspectionTemplateQuestionApi.list(templateId),
      ]);
      setTemplate(templateData);
      setQuestions(questionList);
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
    });
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
      </main>
    </div>
  );
}
