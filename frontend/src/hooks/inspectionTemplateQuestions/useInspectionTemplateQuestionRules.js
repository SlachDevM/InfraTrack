import { useState } from 'react';
import inspectionTemplateQuestionRuleApi from '../../services/inspectionTemplateQuestionRuleApi';
import { getApiErrorMessage, isForbidden } from '../../utils/apiError';
import { isValidQuestionCode } from '../../utils/suggestQuestionCode';
import {
  RULE_SUPPORTED_QUESTION_TYPES,
  comparisonValueRequired,
  getOperatorsForConditionType,
  validateActionPayloadJson,
} from '../../constants/decisionRules';
import { getActiveChoices } from '../../utils/inspectionAnswers';
import {
  EMPTY_RULE_FORM,
  defaultRuleFormForQuestion,
} from '../../pages/inspectionTemplateQuestions/constants';

export function useInspectionTemplateQuestionRules({
  templateId,
  canMutate,
  canView,
  questions,
  setError,
  setSuccess,
  clearManagingChoicesQuestionId,
}) {
  const [managingRulesQuestionId, setManagingRulesQuestionId] = useState(null);
  const [rules, setRules] = useState([]);
  const [ruleForm, setRuleForm] = useState(EMPTY_RULE_FORM);
  const [editingRuleId, setEditingRuleId] = useState(null);
  const [ruleSubmitting, setRuleSubmitting] = useState(false);
  const [rulePayloadError, setRulePayloadError] = useState(null);

  const managingRulesQuestion = questions.find((item) => item.id === managingRulesQuestionId);
  const ruleOperatorOptions = getOperatorsForConditionType(ruleForm.conditionType);
  const showComparisonValue = comparisonValueRequired(ruleForm.conditionType);
  const ruleChoiceOptions = managingRulesQuestion ? getActiveChoices(managingRulesQuestion) : [];

  const loadRulesForQuestion = async (questionId) => {
    const ruleList = await inspectionTemplateQuestionRuleApi.list(templateId, questionId);
    setRules(Array.isArray(ruleList) ? ruleList : []);
  };

  const handleManageRules = async (question) => {
    if (!canView || !RULE_SUPPORTED_QUESTION_TYPES.has(question.questionType)) return;
    setManagingRulesQuestionId(question.id);
    clearManagingChoicesQuestionId();
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
    handleManageRules,
    handleCloseRules,
    handleRuleFormChange,
    handleRuleSubmit,
    handleEditRule,
    cancelRuleEdit,
    handleDeactivateRule,
  };
}
