export const DECISION_RULE_CONDITION_TYPES = ['BOOLEAN', 'NUMBER', 'CHOICE', 'TEXT'];

export const DECISION_RULE_ACTION_TYPES = [
  { value: 'SUGGEST_ISSUE', label: 'Suggest Issue' },
  { value: 'SUGGEST_SEVERITY', label: 'Suggest Severity' },
  { value: 'SUGGEST_OPERATIONAL_DECISION', label: 'Suggest Operational Decision' },
  { value: 'FLAG_FOR_REVIEW', label: 'Flag for Review' },
];

export const DECISION_RULE_OPERATORS_BY_CONDITION_TYPE = {
  BOOLEAN: [
    { value: 'IS_TRUE', label: 'Is true' },
    { value: 'IS_FALSE', label: 'Is false' },
  ],
  NUMBER: [
    { value: 'GREATER_THAN', label: 'Greater than' },
    { value: 'GREATER_THAN_OR_EQUAL', label: 'Greater than or equal' },
    { value: 'LESS_THAN', label: 'Less than' },
    { value: 'LESS_THAN_OR_EQUAL', label: 'Less than or equal' },
    { value: 'EQUALS', label: 'Equals' },
    { value: 'NOT_EQUALS', label: 'Not equals' },
  ],
  CHOICE: [
    { value: 'EQUALS', label: 'Equals' },
    { value: 'NOT_EQUALS', label: 'Not equals' },
  ],
  TEXT: [
    { value: 'EQUALS', label: 'Equals' },
    { value: 'NOT_EQUALS', label: 'Not equals' },
    { value: 'CONTAINS', label: 'Contains' },
    { value: 'STARTS_WITH', label: 'Starts with' },
    { value: 'ENDS_WITH', label: 'Ends with' },
  ],
};

export const RULE_SUPPORTED_QUESTION_TYPES = new Set(['BOOLEAN', 'NUMBER', 'CHOICE', 'TEXT']);

export function getOperatorsForConditionType(conditionType) {
  return DECISION_RULE_OPERATORS_BY_CONDITION_TYPE[conditionType] || [];
}

export function conditionTypeForQuestionType(questionType) {
  if (RULE_SUPPORTED_QUESTION_TYPES.has(questionType)) {
    return questionType;
  }
  return 'NUMBER';
}

export function comparisonValueRequired(conditionType) {
  return conditionType !== 'BOOLEAN';
}

export function validateActionPayloadJson(actionPayload) {
  if (!actionPayload || !actionPayload.trim()) {
    return null;
  }
  try {
    JSON.parse(actionPayload);
    return null;
  } catch {
    return 'Action payload must be valid JSON.';
  }
}

export function getActionTypeLabel(actionType) {
  const option = DECISION_RULE_ACTION_TYPES.find((item) => item.value === actionType);
  return option ? option.label : actionType;
}

export function getOperatorLabel(conditionType, operator) {
  const option = getOperatorsForConditionType(conditionType).find((item) => item.value === operator);
  return option ? option.label : operator;
}
