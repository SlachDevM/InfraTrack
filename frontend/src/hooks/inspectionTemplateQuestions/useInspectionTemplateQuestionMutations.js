import { useState } from 'react';
import inspectionTemplateQuestionApi from '../../services/inspectionTemplateQuestionApi';
import { getApiErrorMessage, isForbidden } from '../../utils/apiError';
import { isValidQuestionCode, suggestQuestionCode } from '../../utils/suggestQuestionCode';
import { EMPTY_QUESTION_FORM } from '../../pages/inspectionTemplateQuestions/constants';

export function useInspectionTemplateQuestionMutations({
  templateId,
  canMutate,
  setQuestions,
  activeQuestions,
  setError,
  setSuccess,
  loadPageData,
}) {
  const [formData, setFormData] = useState(EMPTY_QUESTION_FORM);
  const [editingId, setEditingId] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [reordering, setReordering] = useState(false);
  const [codeManuallyEdited, setCodeManuallyEdited] = useState(false);

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
    setFormData(EMPTY_QUESTION_FORM);
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
        payload.decimalPlaces =
          formData.decimalPlaces === '' ? undefined : Number(formData.decimalPlaces);
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

  return {
    formData,
    editingId,
    submitting,
    reordering,
    handleFormChange,
    handleSubmit,
    resetForm,
    handleEdit,
    handleDeactivate,
    handleMove,
  };
}
