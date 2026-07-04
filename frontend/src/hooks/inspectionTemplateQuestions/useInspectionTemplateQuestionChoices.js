import { useState } from 'react';
import inspectionTemplateQuestionChoiceApi from '../../services/inspectionTemplateQuestionChoiceApi';
import { getApiErrorMessage } from '../../utils/apiError';
import { isValidQuestionCode } from '../../utils/suggestQuestionCode';
import { EMPTY_CHOICE_FORM } from '../../pages/inspectionTemplateQuestions/constants';

export function useInspectionTemplateQuestionChoices({
  templateId,
  canMutate,
  questions,
  setError,
  setSuccess,
  loadPageData,
}) {
  const [managingChoicesQuestionId, setManagingChoicesQuestionId] = useState(null);
  const [choiceForm, setChoiceForm] = useState(EMPTY_CHOICE_FORM);
  const [editingChoiceId, setEditingChoiceId] = useState(null);
  const [choiceSubmitting, setChoiceSubmitting] = useState(false);

  const managingChoicesQuestion = questions.find(
    (question) => question.id === managingChoicesQuestionId
  );

  const handleManageChoices = (question) => {
    if (!canMutate || question.questionType !== 'CHOICE') return;
    setManagingChoicesQuestionId(question.id);
    setChoiceForm(EMPTY_CHOICE_FORM);
    setEditingChoiceId(null);
  };

  const closeChoicePanel = () => {
    setManagingChoicesQuestionId(null);
    setChoiceForm(EMPTY_CHOICE_FORM);
    setEditingChoiceId(null);
  };

  const clearManagingChoicesQuestionId = () => {
    setManagingChoicesQuestionId(null);
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
      setError(
        'Choice code must be uppercase, start with a letter, and contain only letters, digits, and underscores.'
      );
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

  const cancelChoiceEdit = () => {
    setEditingChoiceId(null);
    setChoiceForm(EMPTY_CHOICE_FORM);
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

  return {
    managingChoicesQuestionId,
    managingChoicesQuestion,
    choiceForm,
    editingChoiceId,
    choiceSubmitting,
    clearManagingChoicesQuestionId,
    handleManageChoices,
    closeChoicePanel,
    handleChoiceFormChange,
    handleChoiceSubmit,
    handleEditChoice,
    cancelChoiceEdit,
    handleDeactivateChoice,
    handleMoveChoice,
  };
}
