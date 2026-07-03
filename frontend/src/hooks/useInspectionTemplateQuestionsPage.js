import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import apiClient from '../services/apiClient';
import inspectionTemplateApi from '../services/inspectionTemplateApi';
import inspectionTemplateQuestionApi from '../services/inspectionTemplateQuestionApi';
import inspectionTemplateQuestionChoiceApi from '../services/inspectionTemplateQuestionChoiceApi';
import inspectionTemplateQuestionRuleApi from '../services/inspectionTemplateQuestionRuleApi';
import unitOfMeasureApi from '../services/unitOfMeasureApi';
import { canManageInspectionTemplates, canViewInspectionTemplates } from '../constants/userRoles';
import { ROUTES } from '../constants/routes';
import { getApiErrorMessage, isForbidden } from '../utils/apiError';
import { isValidQuestionCode, suggestQuestionCode } from '../utils/suggestQuestionCode';
import {
  RULE_SUPPORTED_QUESTION_TYPES,
  comparisonValueRequired,
  getOperatorsForConditionType,
  validateActionPayloadJson,
} from '../constants/decisionRules';
import { getActiveChoices } from '../utils/inspectionAnswers';
import {
  EMPTY_CHOICE_FORM,
  EMPTY_QUESTION_FORM,
  EMPTY_RULE_FORM,
  defaultRuleFormForQuestion,
  isDraftTemplate,
} from '../pages/inspectionTemplateQuestions/constants';

export function useInspectionTemplateQuestionsPage() {
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
  const [formData, setFormData] = useState(EMPTY_QUESTION_FORM);
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
  const activeQuestions = useMemo(
    () => questions.filter((question) => question.active),
    [questions]
  );
  const managingRulesQuestion = questions.find((item) => item.id === managingRulesQuestionId);
  const ruleOperatorOptions = getOperatorsForConditionType(ruleForm.conditionType);
  const showComparisonValue = comparisonValueRequired(ruleForm.conditionType);
  const ruleChoiceOptions = managingRulesQuestion ? getActiveChoices(managingRulesQuestion) : [];
  const managingChoicesQuestion = questions.find(
    (question) => question.id === managingChoicesQuestionId
  );

  useEffect(() => {
    if (!auth) {
      navigate(ROUTES.LOGIN);
      return;
    }
    if (!canView) {
      navigate(ROUTES.HOME);
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
      setRuleForm(
        managingQuestion ? defaultRuleFormForQuestion(managingQuestion) : EMPTY_RULE_FORM
      );
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

  const cancelRuleEdit = () => {
    setEditingRuleId(null);
    setRuleForm(
      managingRulesQuestion ? defaultRuleFormForQuestion(managingRulesQuestion) : EMPTY_RULE_FORM
    );
    setRulePayloadError(null);
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
        setRuleForm(
          managingRulesQuestion
            ? defaultRuleFormForQuestion(managingRulesQuestion)
            : EMPTY_RULE_FORM
        );
        setRulePayloadError(null);
      }
      await loadRulesForQuestion(managingRulesQuestionId);
    } catch (err) {
      setError(getApiErrorMessage(err, 'Failed to deactivate decision rule.'));
    }
  };

  return {
    template,
    questions,
    loading,
    error,
    success,
    canManage,
    canMutate,
    canView,
    formData,
    editingId,
    submitting,
    unitsOfMeasure,
    reordering,
    activeQuestions,
    managingChoicesQuestionId,
    managingChoicesQuestion,
    choiceForm,
    editingChoiceId,
    choiceSubmitting,
    managingRulesQuestionId,
    managingRulesQuestion,
    rules,
    ruleForm,
    editingRuleId,
    ruleSubmitting,
    rulePayloadError,
    ruleOperatorOptions,
    showComparisonValue,
    ruleChoiceOptions,
    handleFormChange,
    handleSubmit,
    resetForm,
    handleEdit,
    handleDeactivate,
    handleMove,
    handleManageChoices,
    closeChoicePanel,
    handleChoiceFormChange,
    handleChoiceSubmit,
    handleEditChoice,
    cancelChoiceEdit,
    handleDeactivateChoice,
    handleMoveChoice,
    handleManageRules,
    handleCloseRules,
    handleRuleFormChange,
    handleRuleSubmit,
    handleEditRule,
    cancelRuleEdit,
    handleDeactivateRule,
    isDraft: isDraftTemplate(template),
  };
}
